#Piecework: business process web forms
=========

Piecework is an api for managing web forms on remote hosts and passing associated data through business process management workflows. It allows an organization to own the template web page on a server that is under its control, but to have the data be managed in a central place. 


## Installation

Some configuration is required to connect Piecework into backend identity management systems. To let Piecework know where your configuration files will live, pass a system property "piecework.config.location" to the servlet container on startup. This should point to a directory that can contain one or more .properties files. 

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



