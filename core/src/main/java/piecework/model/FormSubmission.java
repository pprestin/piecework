/*
 * Copyright 2012 University of Washington
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
package piecework.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.Sanitizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@Document(collection = "submission")
public class FormSubmission {

    @Id
    private final String submissionId;

    private final String submissionType;

    private final String formInstanceId;

    public final String requestId;

    private final List<FormValue> formData;

    private FormSubmission() {
        this(new FormSubmission.Builder());
    }

    private FormSubmission(FormSubmission.Builder builder) {
        this.submissionId = builder.submissionId;
        this.submissionType = builder.submissionType;
        this.formInstanceId = builder.formInstanceId;
        this.requestId = builder.requestId;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
    }

    public final static class Builder {

        private String submissionId;
        private String submissionType;
        private String formInstanceId;
        private String requestId;
        private List<FormValue> formData;

        public Builder() {
            super();
        }

        public Builder(FormSubmission instance, Sanitizer sanitizer) {
            this.submissionId = sanitizer.sanitize(instance.submissionId);
            this.submissionType = sanitizer.sanitize(instance.submissionType);
            this.formInstanceId = sanitizer.sanitize(instance.formInstanceId);
            this.requestId = sanitizer.sanitize(instance.requestId);

            if (instance.formData != null && !instance.formData.isEmpty()) {
                this.formData = new ArrayList<FormValue>(instance.formData.size());
                for (FormValue formValue : instance.formData) {
                    this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
                }
            }
        }

        public FormSubmission build() {
            return new FormSubmission(this);
        }

        public Builder submissionId(String submissionId) {
            this.submissionId = submissionId;
            return this;
        }

        public Builder formInstanceId(String formInstanceId) {
            this.formInstanceId = formInstanceId;
            return this;
        }

        public Builder submissionType(String submissionType) {
            this.submissionType = submissionType;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
    }
}
