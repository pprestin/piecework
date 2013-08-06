#Piecework: process-driven web forms
=========

4/5/2013: Early stage/scaffolding for application that wires together web forms using a workflow/bpm engine to manage approval or review of data between multiple users.

## Getting started

The project builds as a war and can be deployed to tomcat using a maven command (see below). 

Some configuration is required to connect Piecework into backend identity management systems. Currently, only LDAP is supported as an identity provider, and access management is hardcoded for the demo user. 

The following commands will bring up the demo version (not much to see yet), which also starts an embedded mongodb (courtesy of https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) and an embedded LDAP server -- you may run into port conflicts if you already have an LDAP server running on your machine:

> mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.1:run -Dspring.profiles.active=dev,ldap,embedded-ldap,embedded-mongo,data

Open a browser and point to: http://localhost:8080/piecework/secure/v1/form/demo

It's not much yet, and in demo mode Spring security is being bypassed to log in as "rod". Take a look at core/src/main/resources/META-INF/piecework/default.properties to override that and get a default login screen -- comment out both authentication properties.

## Note:

Currently uses spring-security-javaconfig SNAPSHOT, so changes to that project made break the build here temporarily.

## Custom configuration:

To let Piecework know where your configuration files will live, pass a system property "piecework.config.location" to the servlet container on startup. This should point to a directory that can contain one or more .properties files. 

For example, to start up with tomcat using maven on Linux, you'll want to do something like the following:

	> pwd
	/Users/someuser/piecework
	> cd ../
	> mkdir piecework-config
	> cp piecework/integration/src/main/resources/META-INF/piecework/default.properties piecework-config/my.properties
	\[ edit my.properties to point to your ldap system, keystore, etc \]
	> cd piecework
	> mvn tomcat:run -Dpiecework.config.location=/Users/someuser/piecework-config

If you don't want to use TLS protected LDAP for identity and authorization behind a Single-Sign-On authentication layer, then you'll want to make some modifications to the ldap configuration.



