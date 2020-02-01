
set HOME=%cd%
set LIB_HOME=%HOME%\extLib

set JARS=%LIB_HOME%\antlr-2.7.7.jar
set JARS=%JARS%;%LIB_HOME%\byte-buddy-1.8.15.jar
set JARS=%JARS%;%LIB_HOME%\classmate-1.3.4.jar
set JARS=%JARS%;%LIB_HOME%\commons-collections-3.2.2.jar
set JARS=%JARS%;%LIB_HOME%\dom4j-1.6.1.jar
set JARS=%JARS%;%LIB_HOME%\guava-21.0.jar
set JARS=%JARS%;%LIB_HOME%\h2-1.4.197.jar
set JARS=%JARS%;%LIB_HOME%\hamcrest-core-1.3.jar
set JARS=%JARS%;%LIB_HOME%\hibernate-commons-annotations-5.0.4.Final.jar
set JARS=%JARS%;%LIB_HOME%\hibernate-core-5.3.4.Final.jar
set JARS=%JARS%;%LIB_HOME%\hibernate-entitymanager-5.3.4.Final.jar
set JARS=%JARS%;%LIB_HOME%\jandex-2.0.5.Final.jar
set JARS=%JARS%;%LIB_HOME%\javassist-3.23.1-GA.jar
set JARS=%JARS%;%LIB_HOME%\javax.activation-api-1.2.0.jar
set JARS=%JARS%;%LIB_HOME%\javax.persistence-api-2.2.jar
set JARS=%JARS%;%LIB_HOME%\jboss-logging-3.3.2.Final.jar
set JARS=%JARS%;%LIB_HOME%\jboss-transaction-api_1.2_spec-1.1.1.Final.jar
set JARS=%JARS%;%LIB_HOME%\jta-1.1.jar
set JARS=%JARS%;%LIB_HOME%\junit-4.11.jar
set JARS=%JARS%;%LIB_HOME%\log4j-1.2.17.jar

set JARS=%JARS%;%HOME%\mypass-1.0-SNAPSHOT.jar

java -Dapp.home.dir=%HOME% -Dapp.log.dir=%HOME%\log\app.log -Dfile.encoding=UTF-8 -cp %JARS% com.del.mypass.view.Launcher

