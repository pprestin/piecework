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
package piecework.engine.config;

import java.sql.Driver;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author James Renfro
 */
@Configuration
@EnableTransactionManagement
public class EngineConfiguration {

	@Value("${activiti.datasource.hibernate.dialect}")
	String activitiDataSourceHiberateDialect;
	
	@Value("${activiti.datasource.driver.name}")
	String activitiDataSourceDriverName;
	
	@Value("${activiti.datasource.url}")
	String activitiDataSourceUrl;
	
	@Value("${activiti.datasource.username}")
	String activitiDataSourceUsername;
	
	@Value("${activiti.datasource.password}")
	String activitiDataSourcePassword;
	
	@Value("${activiti.datasource.ddl.auto}")
	String activitiDataSourceAutoDDL;
	
	@Value("${activiti.datasource.show.sql}")
	String activitiDataSourceShowSQL;
	
	@Bean
	public DataSource activitiDataSource() throws ClassNotFoundException {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		
		@SuppressWarnings("unchecked")
		Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(activitiDataSourceDriverName);
		dataSource.setDriverClass(driverClass);
		dataSource.setUrl(activitiDataSourceUrl);
		dataSource.setUsername(activitiDataSourceUsername);
		dataSource.setPassword(activitiDataSourcePassword);

		return dataSource;
	}
	
	@Bean
	public PlatformTransactionManager activitiDataSourceTransactionManager() throws ClassNotFoundException {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(activitiDataSource());
		return transactionManager;
	}
	
	@Bean
	public ProcessEngineConfigurationImpl activitiEngineConfiguration() throws ClassNotFoundException {
		SpringProcessEngineConfiguration engineConfiguration = new SpringProcessEngineConfiguration();
		engineConfiguration.setDataSource(activitiDataSource());
		engineConfiguration.setTransactionManager(activitiDataSourceTransactionManager());
		engineConfiguration.setDatabaseSchemaUpdate("true");
		
		return engineConfiguration;
	}
	
	@Bean(destroyMethod="close")
	public ProcessEngine activitiEngine(ApplicationContext applicationContext) throws Exception {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setApplicationContext(applicationContext);
		factoryBean.setProcessEngineConfiguration(activitiEngineConfiguration());
		
		return factoryBean.getObject();
	}
	
	@Bean
	public FormService activitiFormService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine(applicationContext).getFormService();
	}

	@Bean
	public RepositoryService activitiRepositoryService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine(applicationContext).getRepositoryService();
	}
	
	@Bean
	public RuntimeService activitiRuntimeService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine(applicationContext).getRuntimeService();
	}
	
	@Bean
	public TaskService activitiTaskService(ApplicationContext applicationContext) throws Exception {
		return activitiEngine(applicationContext).getTaskService();
	}
	
}
