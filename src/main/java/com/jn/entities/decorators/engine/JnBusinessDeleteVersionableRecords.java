package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.query.CcpQueryBool;
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryMust;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.query.CcpQueryShould;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.entities.JnEntityVersionable;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnBusinessSendToMensageria;

/**
 * Tarefa assíncrona que expurga um registro de entidade versionável: apaga o documento da própria
 * tabela (e da gêmea, quando existe) e todas as linhas de histórico que {@code JnVersionableEntity}
 * gravou para ele em {@code JnEntityVersionable}.
 *
 * <p>A exclusão é feita por query, e não pela API de entidade, por dois motivos. O primeiro é o
 * volume: um único registro acumula uma linha de histórico por operação já realizada sobre ele, e a
 * chave primária dessas linhas inclui o {@code timestamp} da operação, então não há um conjunto de
 * ids conhecido a apagar, e sim um conjunto identificado pelo par ({@code entity}, {@code id}) —
 * exatamente o par que {@code JnVersionableEntity} grava em cada linha. O segundo é evitar o efeito
 * colateral do próprio decorator: apagar pela entidade passaria de novo pelo {@code toBulkItems}
 * versionável e gravaria mais uma linha de histórico justamente no expurgo que deveria zerá-lo.
 *
 * <p>Os dois alvos são apagados numa única ida ao banco. Como {@code _delete_by_query} aceita vários
 * índices na mesma chamada, basta enviar a união das tabelas e uma condição {@code should} com os
 * dois critérios: o registro sai por {@code _id}, o histórico sai pelo par ({@code entity},
 * {@code id}). Cada critério é amarrado ao seu {@code _index} para que continue valendo apenas sobre
 * as tabelas a que se destina, preservando o significado que as duas queries separadas tinham.
 */
public class JnBusinessDeleteVersionableRecords implements JnBusinessSendToMensageria {

	public static final JnBusinessDeleteVersionableRecords INSTANCE = new JnBusinessDeleteVersionableRecords();

	private JnBusinessDeleteVersionableRecords() {}

	public static enum JsonFieldNames implements CcpJsonFieldName {
		entitiesToDelete, documentId, deleted
	}

	private static final int AT_LEAST_ONE_CRITERIA = 1;

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String[] entitiesToDelete = json.getAsStringArray(JsonFieldNames.entitiesToDelete);

		CcpEntityMetaData versionableMetaData = JnEntityVersionable.ENTITY.getEntityMetaData();

		String versionableEntityName = versionableMetaData.entityName;

		CcpQueryOptions request = this.getRequestToDeleteEverything(json, entitiesToDelete, versionableEntityName);

		String[] allEntities = this.getAllEntities(entitiesToDelete, versionableEntityName);

		CcpQueryExecutorDecorator everythingToDelete = request.selectFrom(allEntities);

		CcpJsonRepresentation response = everythingToDelete.delete();

		Object noneWasDeleted = 0;

		Object deleted = response.getValueFromPath(noneWasDeleted, JsonFieldNames.deleted);

		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.deleted, deleted);

		return result;
	}

	/**
	 * Monta a única query do expurgo: um {@code should} com os dois critérios, o do registro e o do
	 * histórico, exigindo que ao menos um deles seja satisfeito.
	 */
	private CcpQueryOptions getRequestToDeleteEverything(CcpJsonRepresentation json, String[] entitiesToDelete, String versionableEntityName) {

		var queryToDeleteEverything = CcpQueryOptions.INSTANCE
				.startQuery();
				var boolToDeleteEverything = queryToDeleteEverything
				.startBool();
				var shouldToDeleteEverything = boolToDeleteEverything
				.startShould(AT_LEAST_ONE_CRITERIA);
				var shouldWithTheRecord = this.addTheRecordItSelf(shouldToDeleteEverything, json, entitiesToDelete);
				var shouldWithTheVersions = this.addTheVersionsOfTheRecord(shouldWithTheRecord, json, versionableEntityName);
				var boolWithBothCriterias = shouldWithTheVersions
				.endShouldAndBackToBool();
				var queryWithBothCriterias = boolWithBothCriterias
				.endBoolAndBackToQuery();

		CcpQueryOptions requestToDeleteEverything = queryWithBothCriterias
				.endQueryAndBackToRequest();

		return requestToDeleteEverything;
	}

	/**
	 * Acrescenta ao {@code should} o critério que localiza o documento nas tabelas informadas em
	 * {@code entitiesToDelete}. O id do documento é o mesmo na tabela principal e na gêmea, porque ele
	 * é calculado a partir da chave primária.
	 */
	private CcpQueryShould addTheRecordItSelf(CcpQueryShould should, CcpJsonRepresentation json, String[] entitiesToDelete) {

		String documentId = json.getAsString(JsonFieldNames.documentId);

		List<String> entitiesOfTheRecord = Arrays.asList(entitiesToDelete);

		CcpQueryBool boolToDeleteTheRecord = should
				.startBool();
				CcpQueryMust mustWithTheEntitiesOfTheRecord = boolToDeleteTheRecord
				.startMust()
				.terms(CcpJsonCommonsFields._index, entitiesOfTheRecord);
				CcpQueryMust mustWithTheDocumentId = mustWithTheEntitiesOfTheRecord
				.term(CcpJsonCommonsFields._id, documentId);
				CcpQueryBool boolWithTheDocumentId = mustWithTheDocumentId
				.endMustAndBackToBool();

		CcpQueryShould shouldWithTheRecord = boolWithTheDocumentId
				.endBoolAndBackToShould();

		return shouldWithTheRecord;
	}

	/**
	 * Acrescenta ao {@code should} o critério que localiza todo o histórico do registro em
	 * {@code JnEntityVersionable}. Cada linha de histórico guarda o nome da entidade de origem em
	 * {@code entity} e a chave primária do registro em {@code id}, então é esse par que identifica o
	 * conjunto a expurgar.
	 */
	private CcpQueryShould addTheVersionsOfTheRecord(CcpQueryShould should, CcpJsonRepresentation json, String versionableEntityName) {

		String entityName = json.getAsString(JnJsonCommonsFields.entity);
		String versionableRecordId = json.getAsString(JnJsonCommonsFields.id);

		CcpQueryBool boolToDeleteTheVersions = should
				.startBool();
				CcpQueryMust mustWithTheHistoryTable = boolToDeleteTheVersions
				.startMust()
				.term(CcpJsonCommonsFields._index, versionableEntityName);
				CcpQueryMust mustWithTheEntityName = mustWithTheHistoryTable
				.term(JnJsonCommonsFields.entity, entityName);
				CcpQueryMust mustWithTheRecordId = mustWithTheEntityName
				.term(JnJsonCommonsFields.id, versionableRecordId);
				CcpQueryBool boolWithTheRecordId = mustWithTheRecordId
				.endMustAndBackToBool();

		CcpQueryShould shouldWithTheVersions = boolWithTheRecordId
				.endBoolAndBackToShould();

		return shouldWithTheVersions;
	}

	/**
	 * Junta as tabelas de onde o registro em si sai com a tabela de histórico: é essa a lista de índices
	 * enviada na chamada.
	 */
	private String[] getAllEntities(String[] entitiesToDelete, String versionableEntityName) {

		List<String> entitiesOfTheRecord = Arrays.asList(entitiesToDelete);

		List<String> allEntities = new ArrayList<>(entitiesOfTheRecord);
		allEntities.add(versionableEntityName);

		int size = allEntities.size();

		String[] array = allEntities.toArray(new String[size]);

		return array;
	}
}
