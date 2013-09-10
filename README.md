#Piecework: process-driven web forms
=========

## Getting started

The project builds as a war and can be deployed to tomcat using a maven command (see below). 

Some configuration is required to connect Piecework into backend identity management systems. Currently, only LDAP is supported as an identity provider, and access management is hardcoded for the demo user. 

The following commands will bring up the demo version, which also starts an embedded mongodb (courtesy of https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) and an embedded LDAP server. 
You may run into port conflicts if you already have an LDAP server running on your machine. I would recommend installing
a local MongoDB instance on your workstation since the embedded mongo slows start up and occasionally requires the 
data/storage directory to be deleted. If you want to install MongoDB, you should be able to run the following
command without the 'embedded-mongo' profile. Ditto for LDAP, though you'll have to customize properties if you want
to connect to an external LDAP server. Mongo should just connect as long as you're on the default port and running
an install with auth = false. 

	% mvn clean install -Dmaven.test.skip=true
	% mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.1:run -Dspring.profiles.active=embedded-ldap,embedded-mongo,data -Ddebug.mode=true

Open a browser and point to: http://localhost:8000/piecework/secure/form.html

In debug mode Spring security is being bypassed to log in as "rod". Take a look at core/src/main/resources/META-INF/piecework/default.properties to override that and get a default login screen -- comment out both authentication properties.




## Note:

Currently uses spring-security-javaconfig SNAPSHOT, so changes to that project made break the build here temporarily.

## Custom configuration:

To let Piecework know where your configuration files will live, pass a system property "piecework.config.location" to the servlet container on startup. 
This should point to a directory that can contain one or more .properties files. Alternatively, you can just create a directory called
/etc/piecework on your workstation, and stick properties files there. You can customize properties for particular
Spring profiles by adding a hyphen following by the profile name, for example general-dev.properties will only be
read if you pass in a Spring profile "dev". 

For example, to start up with tomcat using maven on Linux with a custom config, you'll want to do something like the following:

	% pwd
	/Users/someuser/piecework
	% cd ../
	% mkdir piecework-config
	% cp piecework/integration/src/main/resources/META-INF/piecework/default.properties piecework-config/my.properties
	% cd piecework
	% mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.1:run -Dpiecework.config.location=/Users/someuser/piecework-config

If you don't want to use TLS protected LDAP for identity and authorization behind a Single-Sign-On authentication layer, then you'll want to make some modifications to the ldap configuration.

## Keystore

To bring up Tomcat with the httpsPort setting requires a default Java keystore file with a self-signed certificate
installed. Assuming that you're working on Linux or Mac, you should be able to just run the java keytool command and it will generate a file called .keystore under your home directory. On Windows, it may be necessary to adjust the
pom.xml under web/ to have it point to the keystore you generate.

	% keytool -genkey -keyalg RSA

