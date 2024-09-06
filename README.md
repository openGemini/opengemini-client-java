# opengemini-client-java

![License](https://img.shields.io/badge/license-Apache2.0-green) ![Language](https://img.shields.io/badge/language-Java-blue.svg) [![version](https://img.shields.io/github/v/tag/opengemini/opengemini-client-java?label=release&color=blue)](https://github.com/opengemini/opengemini-client-java/releases)

English | [简体中文](README_CN.md)

`opengemini-client-java` is a Java client for OpenGemini

## Design Doc

[OpenGemini Client Design Doc](https://github.com/openGemini/openGemini.github.io/blob/main/src/guide/develop/client_design.md)

## About OpenGemini

OpenGemini is a cloud-native distributed time series database, find more information [here](https://github.com/openGemini/openGemini) 

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


## Prerequisites

- Compiling this project requires at least OpenJDK 17
- When running this project, the OpenJDK versions depended on by various components are different, as shown in the table in the previous chapter

## Integration

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

## Quick Start

```java
package org.example;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.jdk.Configuration;
import io.opengemini.client.jdk.OpenGeminiJdkClient;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(5))
                .build();

        OpenGeminiJdkClient openGeminiJdkClient = new OpenGeminiJdkClient(configuration);

        String databaseName = "db_quick_start";
        CompletableFuture<Void> createdb = openGeminiJdkClient.createDatabase(databaseName);
        createdb.get();

        Point point = new Point();
        point.setMeasurement("ms_quick_start");
        HashMap<String, String> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        tags.put("tag1", "tag value1");
        fields.put("field1", "field value1");
        point.setTags(tags);
        point.setFields(fields);

        openGeminiJdkClient.write(databaseName, point).get();

        // Creating a new tag requires waiting for the server to create and update indexes
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + "ms_quick_start", databaseName, "");
        CompletableFuture<QueryResult> queryRst = openGeminiJdkClient.query(selectQuery);

        System.out.println("query result: " + queryRst.get());
    }
}
```

## Contributor

Welcome to [join us](CONTRIBUTION.md)
