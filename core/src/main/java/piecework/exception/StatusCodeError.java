/*
 * Copyright 2010 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.exception;

import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * This is an exception class to wrap status code exceptions. These are exceptions
 * that should be translated into a particular status code response to the client.
 * 
 * @author James Renfro
 */
public class StatusCodeError extends Exception {

	private static final long serialVersionUID = 1L;
	
	private int statusCode;
	private String resourceKey;
	private Object[] messageArguments;
	private Object entity;
	private boolean suppressErrorCode = false;
	
	public StatusCodeError() {
		super();
	}
	
	public StatusCodeError(int statusCode) {
		this();
		this.statusCode = statusCode;
	}
	
	public StatusCodeError(Throwable cause) {
		super(cause);
	}
	
	public StatusCodeError(Throwable cause, int statusCode) {
		super(cause);
		this.statusCode = statusCode;
	}
	
	public StatusCodeError(int statusCode, String resourceKey, Object... messageArguments) {
		super();
		this.statusCode = statusCode;
		this.resourceKey = resourceKey;
		this.messageArguments = messageArguments;
	}
	
	public String getLocalizedMessage() {
        String message = "Error";
        Response.Status status = Response.Status.fromStatusCode(statusCode);
        if (status != null)
            message = status.getReasonPhrase();
        return new StringBuilder().append(message).toString();
    }

	public String getMessageDetail() {
		String exceptionText = null;
		
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(StatusCodeError.class.getName());
			
			if (resourceKey != null) {	
				String localizedMessage = resourceBundle.getString(resourceKey);
				
				if (messageArguments == null || messageArguments.length == 0)
					return localizedMessage;
				
				MessageFormat formatter = new MessageFormat(localizedMessage);
			    exceptionText = formatter.format(messageArguments);
			} else {
				
				String localizedMessage = null;
				
				try {
					localizedMessage = resourceBundle.getString(String.valueOf(statusCode));
				} catch (MissingResourceException e) {
					// Ignore this
				}
				if (localizedMessage == null)
					exceptionText = "The system encountered a problem resolving this request.";
				else 
					exceptionText = localizedMessage;
				
			}
		} catch (MissingResourceException e) {
			exceptionText = "The system has encountered a problem handling this request but it is not currently configured to provide detailed messages on what went wrong";
		}
		
        return exceptionText;
	}
	
	/**
	 * @return the resourceKey
	 */
	public String getResourceKey() {
		return this.resourceKey;
	}

	/**
	 * @param resourceKey the resourceKey to set
	 */
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return this.statusCode;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public boolean isSuppressErrorCode() {
		return suppressErrorCode;
	}

	public void setSuppressErrorCode(boolean suppressErrorCode) {
		this.suppressErrorCode = suppressErrorCode;
	}
	
}
