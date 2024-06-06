# handover-option-availability service

## Description
The service contains methods allows you to get information on how and when a client can receive his order.

## Requirements
For building and running the application you need:
- [JDK 17](https://libericajdk.ru/pages/downloads/#/java-17-lts%20/%20current)
- [Maven](https://maven.apache.org)

## Reference Documentation
For further reference, please consider the following sections:
* [handover-option-availability documentation](https://wiki.mvideo.ru/display/CASE/handover-option-availability)
* [handover-option-availability service swagger page](https://handover-option-availability-dev.lards.yc.mvideo.ru/swagger-ui/#/)
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.3.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.3.RELEASE/maven-plugin/reference/html/#build-image)

## Clone repository
- Add Personal Access Token for your Gitlab account (profile - settings - access tokens - add scopes and create token)
- Create ~/.m2/settings.xml with the following structure:
```xml
<settings>
  <servers>
    <server>
      <id>gitlab-maven</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>$personal-access-token-value$</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>
</settings>
```
- Run `git clone` with gitlab login and Personal Access Token value as password

## Running the application locally
Please configure Environment variables before trying to run

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `ru.mvideo.handoveroptionavailability.Application` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run -pl handover-option-availability-app
```

## Backlog
[CLOP Backlog](https://jira.mvideo.ru/jira/secure/RapidBoard.jspa?rapidView=1718&projectKey=CS)

## Copyright
![Mvideo](http://static.mvideo.ru/assets/img/mvideo-logo.png)