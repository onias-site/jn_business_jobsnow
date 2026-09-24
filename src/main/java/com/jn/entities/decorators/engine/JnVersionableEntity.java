package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityVersionable;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Decorador que adiciona versionamento/auditoria a entidades marcadas com {@code @CcpEntityVersionable}.
 * A cada operação bulk, gera automaticamente um registro de histórico em {@code JnEntityVersionable}
 * com o estado anterior do JSON, a operação realizada, data e hora.
 */
public class JnVersionableEntity extends CcpDefaultEntityDelegator<Object>{
	
	public JnVersionableEntity(CcpEntity entity) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
	}

	private final CcpBulkItem getVersionableToBulkOperationToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation versionable = this.getVersionableRecord(json, operation);
		String calculateId = JnEntityVersionable.ENTITY.calculateId(versionable);
		CcpBulkItem ccpBulkItem = new CcpBulkItem(versionable, CcpBulkEntityOperationType.create, JnEntityVersionable.ENTITY, calculateId);
				
		return ccpBulkItem;
	}

	/**
	 * Devolve o registro que será retratado na linha de histórico: o que está gravado no banco quando
	 * ele existe, ou os campos da entidade presentes no json quando ainda não existe.
	 */
	private CcpJsonRepresentation getRecordToAudit(CcpJsonRepresentation json) {

		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();

		CcpBusiness ifNotFound = x ->

		{
			CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
			CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(handledJson);
			return onlyExistingFields;
		};

		CcpJsonRepresentation oneById = entityDetails.getOneByIdOrHandleItIfThisIdWasNotFound(json, ifNotFound);

		return oneById;
	}

	/**
	 * Calcula o valor gravado no campo {@code id} da linha de histórico: a chave primária do registro
	 * serializada. É por ele, junto com o nome da entidade, que o histórico de um registro é localizado.
	 */
	private String getVersionableRecordId(CcpJsonRepresentation recordToAudit) {

		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();

		Supplier<CcpJsonRepresentation> jsonSupplier = recordToAudit.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(jsonSupplier);

		String id = primaryKeyValues.asUgglyJson();

		return id;
	}

	private CcpJsonRepresentation getVersionableRecord(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {

		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();

		CcpJsonRepresentation oneById = this.getRecordToAudit(json);

		String id = this.getVersionableRecordId(oneById);
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();

		String formattedDateTime = ccpTimeDecorator.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS");
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.id, id);
				String valorMais = "" + oneById;
				CcpJsonRepresentation put2 = put
				.put(JnJsonCommonsFields.json, valorMais);
				CcpJsonRepresentation put3 = put2
				.put(JnJsonCommonsFields.operation, operation);
				CcpJsonRepresentation put4 = put3
				.put(JnJsonCommonsFields.date, formattedDateTime);
				CcpJsonRepresentation put5 = put4
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);
				long currentTimeMillis = System.currentTimeMillis();

		CcpJsonRepresentation audit = 
				put5
				.put(JnJsonCommonsFields.timestamp, currentTimeMillis)
		;
		return audit;
	}



	/**
	 * Enfileira o expurgo do registro: a exclusão do documento na própria tabela e a de todas as linhas
	 * de histórico gravadas para ele em {@code JnEntityVersionable}. O trabalho é assíncrono porque o
	 * histórico cresce uma linha por operação já realizada sobre o registro, e apagá-lo em linha
	 * penalizaria quem só pediu para remover um documento.
	 *
	 * <p>O retorno mantém o significado que {@code CcpEntity.deleteAnyWhere} dá ao booleano — o registro
	 * existia antes da remoção — e não o de {@code JnAsyncWriterEntity}, que informa apenas que a
	 * mensagem foi aceita. Isso é possível porque a existência é consultada aqui, antes do enfileiramento.
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {

		boolean existedBeforeTheDeletion = this.exists(json);

		CcpJsonRepresentation deletionRequest = this.getDeletionRequest(json);

		JnBusinessDeleteVersionableRecords.INSTANCE.sendToMensageria(deletionRequest);

		return existedBeforeTheDeletion;
	}

	/**
	 * Monta a mensagem consumida por {@code JnBusinessDeleteVersionableRecords}: o par
	 * ({@code entity}, {@code id}) que localiza o histórico do registro, as tabelas de onde o próprio
	 * registro deve sair e o id do documento nelas.
	 */
	private CcpJsonRepresentation getDeletionRequest(CcpJsonRepresentation json) {

		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();

		CcpJsonRepresentation recordToDelete = this.getRecordToAudit(json);

		String versionableRecordId = this.getVersionableRecordId(recordToDelete);

		String[] entitiesToDelete = this.getEntitiesToDelete(entityDetails);

		String documentId = this.calculateId(json);

		CcpJsonRepresentation withEntityName = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);
				CcpJsonRepresentation withRecordId = withEntityName
				.put(JnJsonCommonsFields.id, versionableRecordId);
				CcpJsonRepresentation withEntitiesToDelete = withRecordId
				.put(JnBusinessDeleteVersionableRecords.JsonFieldNames.entitiesToDelete, entitiesToDelete);

		CcpJsonRepresentation deletionRequest = withEntitiesToDelete
				.put(JnBusinessDeleteVersionableRecords.JsonFieldNames.documentId, documentId);

		return deletionRequest;
	}

	/**
	 * Lista as tabelas de onde o registro em si deve ser apagado: a principal e a gêmea, quando existe.
	 * {@code JnEntityVersionable} é retirada da lista porque suas linhas não são localizadas pelo id do
	 * documento, e sim pelo par ({@code entity}, {@code id}) — quem as apaga é o outro critério da query
	 * do expurgo, que o próprio {@code JnBusinessDeleteVersionableRecords} acrescenta.
	 */
	private String[] getEntitiesToDelete(CcpEntityMetaData entityDetails) {

		CcpEntityMetaData versionableMetaData = JnEntityVersionable.ENTITY.getEntityMetaData();

		String[] entitiesToSelect = entityDetails.getEntitiesToSelect();
		List<String> allEntities = Arrays.asList(entitiesToSelect);
		Stream<String> stream = allEntities.stream();
		var withoutTheHistory = stream.filter(x -> false == x.equals(versionableMetaData.entityName));

		List<String> entitiesToDelete = withoutTheHistory.collect(Collectors.toList());
		int size = entitiesToDelete.size();

		String[] array = entitiesToDelete.toArray(new String[size]);

		return array;
	}
	
	public List<CcpEntity> getAssociatedEntities() {
		List<CcpEntity> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity> result = new ArrayList<CcpEntity>(associatedEntities);
		result.add(JnEntityVersionable.ENTITY);
		return result;
	}
	
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		Object throwException = this.throwException();
		return (CcpJsonRepresentation)throwException;
	}
	

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> asList = new ArrayList<>(bulkItems);
		
		CcpBulkItem versionableToBulkOperation = this.getVersionableToBulkOperationToBulkOperation(json, operation);
		asList.add(versionableToBulkOperation);
		return asList;
	}

	
}
