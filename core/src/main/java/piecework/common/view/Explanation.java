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
package piecework.common.view;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a representation of the explanation of an exception case.
 * 
 * @author James Renfro
 */
@XmlRootElement
public class Explanation {

	private static final long serialVersionUID = -5144317536325535290L;
	
	private String message;
	private String messageDetail;

	/**
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the messageDetail
	 */
	public String getMessageDetail() {
		return messageDetail;
	}

	/**
	 * @param messageDetail the messageDetail to set
	 */
	public void setMessageDetail(String messageDetail) {
		this.messageDetail = messageDetail;
	}
	
}
