/*
 * Copyright 2013 University of Washington
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

/**
 * @author James Renfro
 */
public class PieceworkException extends Exception {

    public PieceworkException() {
        super();
    }

    public PieceworkException(String message) {
        super(message);
    }

    public PieceworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public PieceworkException(Throwable cause) {
        super(cause);
    }

    protected PieceworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
