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
package piecework.authorization;


/**
 * @author James Renfro
 */
public final class AuthorizationRole { //implements Comparable<AuthorizationRole> {

//	public static final String ANONYMOUS = "anonymous";
	public static final String CREATOR = "ROLE_CREATOR";
	public static final String OWNER = "ROLE_OWNER";
	public static final String USER = "ROLE_USER";
	public static final String INITIATOR = "ROLE_INITIATOR";
	public static final String APPROVER = "ROLE_APPROVER";
	public static final String WATCHER = "ROLE_WATCHER";
	public static final String OVERSEER = "ROLE_OVERSEER";
	public static final String SUPERUSER = "ROLE_SUPERUSER";
	public static final String SYSTEM = "ROLE_SYSTEM";
	
}
