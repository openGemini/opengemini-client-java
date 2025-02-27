# opengemini-client-java

![License](https://img.shields.io/badge/license-Apache2.0-green) ![Language](https://img.shields.io/badge/language-Java-blue.svg) [![version](https://img.shields.io/github/v/tag/opengemini/opengemini-client-java?label=release&color=blue)](https://github.com/opengemini/opengemini-client-java/releases)

English | [简体中文](README_CN.md)

`opengemini-client-java` is a Java client for OpenGemini

## Design Doc

[OpenGemini Client Design Doc](https://github.com/openGemini/openGemini.github.io/blob/main/src/guide/develop/client_design.md)

## About OpenGemini

OpenGemini is a cloud-native distributed time series database, find more information [here](https://github.com/openGemini/openGemini)

## Prerequisites

Compiling this project requires at least OpenJDK 17, and Maven 3.8.0 or later.

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
    <artifactId>opengemini-client</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### HTTP Engine Selection

OpenGeminiClient supports multiple [HTTP engines](https://github.com/openfacade/http-facade?tab=readme-ov-file#httpclient-support-engines) by leveraging [http-facade](https://github.com/openfacade/http-facade). By default, the client uses the built-in HTTP engine provided by the JDK. It automatically selects the appropriate implementation based on the Java version, supporting both Java 8 and Java 11+. If needed, you can configure a different HTTP engine by specifying the `.engine` option in the `HttpClientConfig`. Please note, if a different engine is chosen, you will need to manually include the corresponding dependencies.

## Quick Start

```java
package org.example;

import io.github.openfacade.http.HttpClientConfig;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.impl.OpenGeminiClient;
import io.opengemini.client.impl.OpenGeminiClientFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException, OpenGeminiException {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .httpConfig(httpConfig)
                .build();

        OpenGeminiClient client = OpenGeminiClientFactory.create(configuration);

        String databaseName = "db_quick_start";
        CompletableFuture<Void> createdb = client.createDatabase(databaseName);
        createdb.get();

        Point point = new Point();
        point.setMeasurement("ms_quick_start");
        HashMap<String, String> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        tags.put("tag1", "tag value1");
        fields.put("field1", "field value1");
        point.setTags(tags);
        point.setFields(fields);

        client.write(databaseName, point).get();

        // Creating a new tag requires waiting for the server to create and update indexes
        Thread.sleep(3000);

        Query selectQuery = new Query("select * from " + "ms_quick_start", databaseName, "");
        CompletableFuture<QueryResult> queryRst = client.query(selectQuery);

        System.out.println("query result: " + queryRst.get());
    }
}
```

## Contribution

Welcome to [join us](CONTRIBUTION.md)
