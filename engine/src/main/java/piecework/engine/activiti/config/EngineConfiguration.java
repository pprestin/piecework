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
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import piecework.engine.activiti.CustomBpmnProcessParseHandler;
import piecework.engine.activiti.CustomBpmnUserTaskParseHandler;

/**
 * @author James Renfro
 */
@Configuration
@EnableTransactionManagement
public class EngineConfiguration {

    @Autowired
    Environment environment;

    @Autowired
    ProcessEngine activitiEngine;

//    @Autowired
//    DataSource activitiDataSource;
//
//    @Autowired
//    DataSourceTransactionManager platformTransactionManager;

    @Autowired
    CustomBpmnProcessParseHandler customBpmnProcessParseHandler;

    @Autowired
    CustomBpmnUserTaskParseHandler customBpmnUserTaskParseHandler;

    @Bean
    public DataSource activitiDataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        Class<Driver> driverClass = environment.getPropertyAsClass("activiti.datasource.driver.name", Driver.class);
        dataSource.setDriverClass(driverClass);
        dataSource.setUrl(environment.getProperty("activiti.datasource.url"));
        dataSource.setUsername(environment.getProperty("activiti.datasource.username"));
        dataSource.setPassword(environment.getProperty("activiti.datasource.password"));

        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    public PlatformTransactionManager activitiDataSourceTransactionManager() {
        return new DataSourceTransactionManager((activitiDataSource()));
    }

//    public PlatformTransactionManager annotationDrivenTransactionManager() {
//        return activitiDataSourceTransactionManager();
//    }

    @Bean
    public CustomBpmnProcessParseHandler customBpmnProcessParseHandler() {
        return new CustomBpmnProcessParseHandler();
    }

    @Bean
    public CustomBpmnUserTaskParseHandler customBpmnUserTaskParseHandler() {
        return new CustomBpmnUserTaskParseHandler();
    }

	@Bean
	public ProcessEngineConfigurationImpl activitiEngineConfiguration(Environment environment) throws ClassNotFoundException {
        SpringProcessEngineConfiguration engineConfiguration = new SpringProcessEngineConfiguration();
		engineConfiguration.setDataSource(activitiDataSource());
		engineConfiguration.setTransactionManager(activitiDataSourceTransactionManager());
		engineConfiguration.setDatabaseSchemaUpdate("true");
        engineConfiguration.setIdGenerator(new StrongUuidGenerator());
//        engineConfiguration.setEnableSafeBpmnXml(true);
        engineConfiguration.setPostBpmnParseHandlers(Arrays.<BpmnParseHandler>asList(customBpmnProcessParseHandler(), customBpmnUserTaskParseHandler()));

        engineConfiguration.setConfigurators(Collections.singletonList(ldapConfigurator(environment)));

		return engineConfiguration;
	}

	@Bean
	public ProcessEngineFactoryBean activitiEngineBean(ApplicationContext applicationContext, Environment environment) throws Exception {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setApplicationContext(applicationContext);
		factoryBean.setProcessEngineConfiguration(activitiEngineConfiguration(environment));

		return factoryBean;
	}
	
	@Bean
	public FormService activitiFormService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine.getFormService();
	}

    @Bean
    public HistoryService activitiHistoryService(ApplicationContext applicationContext) throws Exception {
        return activitiEngine.getHistoryService();
    }

    @Bean
    public IdentityService activitiIdentityService(ApplicationContext applicationContext) throws Exception {
        return activitiEngine.getIdentityService();
    }

	@Bean
	public RepositoryService activitiRepositoryService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine.getRepositoryService();
	}
	
	@Bean
	public RuntimeService activitiRuntimeService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine.getRuntimeService();
	}
	
	@Bean
	public TaskService activitiTaskService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine.getTaskService();
	}


    private ProcessEngineConfigurator ldapConfigurator(Environment environment) {
        String ldapPersonUrl = environment.getProperty("ldap.person.url");
        String defaultUser = environment.getProperty("ldap.authentication.user");
        String defaultPassword = environment.getProperty("ldap.authentication.password");
        String baseDn = environment.getProperty("ldap.person.db");
        String userByUserId = environment.getProperty("ldap.person.search.filter.internal");

        LDAPConfigurator ldapConfigurator = new LDAPConfigurator();

        ldapConfigurator.setServer(ldapPersonUrl);
        ldapConfigurator.setUser(defaultUser);
        ldapConfigurator.setPassword(defaultPassword);

        ldapConfigurator.setBaseDn(baseDn);
        ldapConfigurator.setQueryUserByUserId(userByUserId);
        ldapConfigurator.setQueryUserByFullNameLike("displayName={0}");
        ldapConfigurator.setQueryGroupsForUser("member=uwNetID={1}");

        ldapConfigurator.setUserLastNameAttribute("cn");
        ldapConfigurator.setUserFirstNameAttribute("sn");
        ldapConfigurator.setUserIdAttribute("uwRegID");

        ldapConfigurator.setGroupIdAttribute("cn");
        ldapConfigurator.setGroupNameAttribute("cn");

        return ldapConfigurator;
    }

}
