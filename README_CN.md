# opengemini-client-java

![License](https://img.shields.io/badge/开源许可证-Apache2.0-green) ![language](https://img.shields.io/badge/语言-Java-blue.svg) [![version](https://img.shields.io/github/v/tag/opengemini/opengemini-client-java?label=%e5%8f%91%e8%a1%8c%e7%89%88%e6%9c%ac&color=blue)](https://github.com/opengemini/opengemini-client-java/releases)

[English](README.md) | 简体中文

`opengemini-client-java`是一个用 Java 语言编写的 OpenGemini 客户端

## 设计文档

[OpenGemini Client 设计文档](https://github.com/openGemini/openGemini.github.io/blob/main/src/zh/guide/develop/client_design.md)

## 关于 OpenGemini

OpenGemini 是一款云原生分布式时序数据库。获取更多信息，请点击[这里](https://github.com/openGemini/openGemini)

## 依赖

- 编译本项目至少需要OpenJDK 17, Maven 3.8.0或更高版本

## 集成

### 构建

使用常见的maven构建语法即可

```shell
mvn install -Dmaven.test.skip=true
```

若要使用```mvn test``` 请先本地运行一个opengemini的server，推荐使用官方容器镜像版本，如：

```
docker run -p 8086:8086 --name opengemini --rm opengeminidb/opengemini-server
```

### maven引用

```xml
<dependency>
    <groupId>io.opengemini</groupId>
    <artifactId>opengemini-client</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### HTTP 引擎选择

OpenGeminiClient利用 [http-façade](https://github.com/openfacade/http-façade) 支持了多个 [HTTP 引擎](https://github.com/openfacade/http-façade?tab=readme-ov-file#httpclient-support-engines)。默认情况下，客户端使用 JDK 自带的 HTTP 引擎，并根据 Java 版本自动选择合适的实现，支持 Java 8 和 Java 11+。如果有需要，你可以在 `HttpClientConfig` 中通过 `.engine` 选项配置不同的 HTTP 引擎。请注意，若选择不同的引擎，则需要手动添加相应的依赖。

## 快速上手

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
## 贡献

欢迎[加入我们](CONTRIBUTION_CN.md)
