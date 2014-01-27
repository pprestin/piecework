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
package piecework;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ServiceLocator implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Map<Class<?>, Object> serviceMap = new Hashtable<Class<?>, Object>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public <T> T getService(Class<T> type) {
        if (!serviceMap.isEmpty() && serviceMap.containsKey(type))
            return type.cast(serviceMap.get(type));

        return applicationContext.getBean(type);
    }

    public <T> void setService(Class<T> type, T service) {
        serviceMap.put(type, service);
    }

}
