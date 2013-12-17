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
package piecework.common;

/**
 * @author James Renfro
 */
public class OperationResult {

    private final String operationDescription;
    private final String defaultApplicationStatus;
    private final String processStatus;
    private final String applicationStatusExplanation;
    private final String previousApplicationStatus;

    public OperationResult() {
        this(null, null, null, null, null);
    }

    public OperationResult(String operationDescription, String defaultApplicationStatus, String processStatus, String applicationStatusExplanation) {
        this(operationDescription, defaultApplicationStatus, processStatus, applicationStatusExplanation, null);
    }

    public OperationResult(String operationDescription, String defaultApplicationStatus, String processStatus, String applicationStatusExplanation, String previousApplicationStatus) {
        this.operationDescription = operationDescription;
        this.defaultApplicationStatus = defaultApplicationStatus;
        this.processStatus = processStatus;
        this.applicationStatusExplanation = applicationStatusExplanation;
        this.previousApplicationStatus = previousApplicationStatus;
    }

    public String getOperationDescription() {
        return operationDescription;
    }

    public String getDefaultApplicationStatus() {
        return defaultApplicationStatus;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public String getPreviousApplicationStatus() {
        return previousApplicationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationResult that = (OperationResult) o;

        if (applicationStatusExplanation != null ? !applicationStatusExplanation.equals(that.applicationStatusExplanation) : that.applicationStatusExplanation != null)
            return false;
        if (defaultApplicationStatus != null ? !defaultApplicationStatus.equals(that.defaultApplicationStatus) : that.defaultApplicationStatus != null)
            return false;
        if (previousApplicationStatus != null ? !previousApplicationStatus.equals(that.previousApplicationStatus) : that.previousApplicationStatus != null)
            return false;
        if (processStatus != null ? !processStatus.equals(that.processStatus) : that.processStatus != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = defaultApplicationStatus != null ? defaultApplicationStatus.hashCode() : 0;
        result = 31 * result + (processStatus != null ? processStatus.hashCode() : 0);
        result = 31 * result + (applicationStatusExplanation != null ? applicationStatusExplanation.hashCode() : 0);
        result = 31 * result + (previousApplicationStatus != null ? previousApplicationStatus.hashCode() : 0);
        return result;
    }
}
