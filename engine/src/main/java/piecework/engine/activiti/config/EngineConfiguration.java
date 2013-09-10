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
package piecework.engine.activiti.config;

import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;

import javax.sql.DataSource;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.ldap.LDAPConfigurator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import piecework.engine.activiti.CustomBpmnProcessParseHandler;
import piecework.engine.activiti.CustomBpmnUserTaskParseHandler;
import piecework.engine.activiti.GeneralExecutionListener;
import piecework.engine.activiti.GeneralUserTaskListener;

/**
 * @author James Renfro
 */
@Configuration
@ImportResource("classpath:META-INF/piecework/piecework-engine.xml")
public class EngineConfiguration {

    @Autowired
    Environment environment;

}
