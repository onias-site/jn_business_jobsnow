package com.jn.business.login.solve.token;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;

/**
 * Reseta (exclui de todos os índices) o token de login de um usuário. Útil para
 * forçar a geração de um novo token, limpando o estado anterior.
 */
public class JnBusinessResetLoginToken implements CcpBusiness{
	
	enum JsonFieldNames implements CcpJsonFieldName{
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,
	}
	
	private JnBusinessResetLoginToken(){}
	
	public static final JnBusinessResetLoginToken INSTANCE = new JnBusinessResetLoginToken();
	
	/**
	 * Apaga, numa única ida ao banco, o token da entidade principal, o token da gêmea (onde ele fica
	 * quando está bloqueado) e o registro que marca o email do token como já enviado — este último
	 * porque ele recusaria como repetição o email do token novo.
	 *
	 * <p>As três exclusões vão juntas num bulk em vez de virarem três chamadas: o
	 * {@code JnExecuteBulkOperation} monta os itens de cada entidade a partir do mesmo json e os envia
	 * de uma vez. Apagar um registro que não está lá não atrapalha — o banco devolve o item como não
	 * encontrado, sem erro, e é o caso normal aqui, já que o token ou está na principal ou está na
	 * gêmea, nunca nas duas.
	 *
	 * <p>O json passa antes pelo transformador do email porque é ele que calcula o hash que compõe a
	 * chave primária das duas entidades. A montagem dos itens de bulk não aplica transformador de campo
	 * nenhum — isso fica a cargo de quem chama —, e sem o hash as chaves não bateriam com as dos
	 * registros gravados.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String tokenEmail = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		CcpJsonRepresentation plainJson = CcpOtherConstants.EMPTY_JSON.redoJson(json);
		CcpJsonRepresentation withTheHashedEmail = plainJson.getTransformedJson(JnJsonTransformersFieldsEntityDefault.email);
		CcpJsonRepresentation recordsToDelete = withTheHashedEmail.put(JnJsonCommonsFields.subjectType, tokenEmail);

		CcpEntity lockedToken = JnEntityLoginToken.ENTITY.getTwinEntity();

		JnExecuteBulkOperation.INSTANCE.executeBulk(
				recordsToDelete,
				CcpBulkEntityOperationType.delete,
				JnDeleteKeysFromCache.INSTANCE,
				JnEntityLoginToken.ENTITY,
				lockedToken,
				JnEntityEmailMessageSent.ENTITY
				);

	//TODO LANGUAGE DO USUARIO DENTRO DE ANSWERS
		CcpJsonRepresentation put = json.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		return put;
	}


	/**
	 * Retorna JsonFieldNames.class.
	 */
	public Class<?> getJsonValidationClass() {
		return JsonFieldNames.class;
	}
}
