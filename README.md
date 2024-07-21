# opengemini-client-java

opengemini-client-java is a Java client for OpenGemini. 

Find OpenGemini Client design doc at https://github.com/openGemini/openGemini.github.io/blob/main/src/guide/develop/client_design.md

English | [简体中文](README_CN.md)

OpenGemini is an open-source time series database, find more about OpenGemini at https://github.com/openGemini/openGemini
## Characteristic

- This project is implemented independently by multiple components, which can be referenced separately according to different requirements.
- This project is implemented in line with other SDKs such as opengemini-client-go and opengemini-client-python, adhering to the same API layer philosophy, resulting in a very similar user experience for different users.

| Sdk Components                    | Runtime Minimum OpenJDK Version | Recommended Scenario                 | Notice                                                       |
| --------------------------------- | ------------------------------- | ------------------------------------ | ------------------------------------------------------------ |
| opengemini-client-jdk             | OpenJDK 17                      | Minimum Dependency Scenario          | The native httpclient of OpenJDK was introduced in OpenJDK 9, but as versions such as OpenJDK 9 are no longer supported, it is recommended to upgrade to the higher jdk17 version|
| opengemini-client-reactor         | OpenJDK 8(1.8)                  | Reactive Paradigm Framework          | Stay Tuned |
| opengemini-client-okhttp          | OpenJDK 8(1.8)                  | Popular okhttp component, preferred for Android scenarios            | Stay Tuned |
| opengemini-client-asynchttpclient | OpenJDK 11                      | Asynchronous Programming Framework Scenarios for Performance Pursuit | Stay Tuned |
| opengemini-client-kotlin/scala    | -                               | Preferred for Corresponding Development Language                     | Stay Tuned |


## Requirements

- Compiling this project requires at least OpenJDK 17
- When running this project, the OpenJDK versions depended on by various components are different, as shown in the table in the previous chapter

## Usage

### Build

You can use common Maven build syntax

```shell
mvn install -Dmaven.test.skip=true
```

To use mvn test, please first run an opengemini server locally. We recommend using the official container image version, as shown below

```
docker run -p 8086:8086 --name opengemini --rm opengeminidb/opengemini-server
```

### maven import

```xml
<dependency>
    <groupId>io.opengemini</groupId>
    <artifactId>opengemini-client-jdk</artifactId>
    <version>${latest.version}</version>
</dependency>
```
