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

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.model.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class Payload<T> {

    public enum PayloadType { INSTANCE, MULTIPART, FORMDATA };

    private PayloadType type;
    private T instance;
    private MultipartBody multipartBody;
    private Map<String, List<String>> formData;

    @SuppressWarnings("unchecked")
    public <P extends Payload<T>> P processInstance(T instance) {
        this.type = PayloadType.INSTANCE;
        this.instance = instance;
        return (P)this;
    }

    @SuppressWarnings("unchecked")
    public <P extends Payload<T>> P multipartBody(MultipartBody multipartBody) {
        this.type = PayloadType.MULTIPART;
        this.multipartBody = multipartBody;
        return (P)this;
    }

    @SuppressWarnings("unchecked")
    public <P extends Payload<T>> P formData(Map<String, List<String>> formData) {
        this.type = PayloadType.FORMDATA;
        this.formData = formData;
        return (P)this;
    }

    public PayloadType getType() {
        return type;
    }

    public T getInstance() {
        return instance;
    }

    public MultipartBody getMultipartBody() {
        return multipartBody;
    }

    public Map<String, List<String>> getFormData() {
        return formData;
    }

}
