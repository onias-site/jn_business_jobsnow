package com.jn.business.login.solve.token;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.email.CcpEmailSender;

class CopyEmailInClipBoard implements CcpInstanceProvider<CcpEmailSender>, CcpEmailSender  {
	
	public final static CopyEmailInClipBoard INSTANCE = new CopyEmailInClipBoard();
	
	private CopyEmailInClipBoard() {}
	
	static enum Fields implements CcpJsonFieldName{
		message, subject
	}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.copyToClipBoard(json, Fields.message);
		this.copyToClipBoard(json, Fields.subject);
		return json;
	}

	private void copyToClipBoard(CcpJsonRepresentation json, CcpJsonFieldName field) {
		String message = json.getAsString(field);
		Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = defaultToolkit.getSystemClipboard();
		clipboard.setContents(new StringSelection(message), null);
	}

	public CcpEmailSender getInstance() {
		return this;
	}


}
