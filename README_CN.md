# opengemini-client-java

![License](https://img.shields.io/badge/开源许可证-Apache2.0-green) ![language](https://img.shields.io/badge/语言-Java-blue.svg) [![version](https://img.shields.io/github/v/tag/opengemini/opengemini-client-java?label=%e5%8f%91%e8%a1%8c%e7%89%88%e6%9c%ac&color=blue)](https://github.com/opengemini/opengemini-client-java/releases)

[English](README.md) | 简体中文  

`opengemini-client-java`是一个用 Python 语言编写的 OpenGemini 客户端

## 设计文档

[OpenGemini Client 设计文档](https://github.com/openGemini/openGemini.github.io/blob/main/src/zh/guide/develop/client_design.md)

## 关于 OpenGemini

OpenGemini 是一款云原生分布式时序数据库。获取更多信息，请点击[这里](https://github.com/openGemini/openGemini)

## 特点

- 本项目由多个组件独立实现，可根据不同的诉求单独引用
- 本项目与opengemini-client-go、opengemini-client-python等其他sdk实现，在API层的理念一致，对不同的使用者而言，使用体验非常相似

| 组件                              | 运行依赖最低OpenJDK版本 | 推荐场景                             | 说明                                                         |
| --------------------------------- | ----------------------- | ------------------------------------ | ------------------------------------------------------------ |
| opengemini-client-jdk             | OpenJDK 17              | 最小依赖场景                         | jdk原生的httpclient是在OpenJDK 9引入的，但由于OpenJDK 9等版本即将不再受支持，推荐升级到更高的OpenJDK 17版本 |
| opengemini-client-reactor         | OpenJDK 8(1.8)          | 响应式范式框架                       | 敬请期待                                                     |
| opengemini-client-okhttp          | OpenJDK 8(1.8)          | 业界最流行的okhttp组件，安卓场景首选 | 敬请期待                                                     |
| opengemini-client-asynchttpclient | OpenJDK 11              | 追求性能的异步编程框架场景           | 敬请期待                                                     |
| opengemini-client-kotlin/scala    | -                       | 对应开发语言的首选                   | 敬请期待                                                     |


## 依赖

- 编译本项目至少需要OpenJDK 17
- 运行本项目时，各组件依赖的jdk版本不同，如上个章节表格中所示


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
    <artifactId>opengemini-client-jdk</artifactId>
    <version>${latest.version}</version>
</dependency>
```

## 快速上手

```java
package org.example;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.jdk.Configuration;
import io.opengemini.client.impl.OpenGeminiJdkClient;

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
## 贡献

欢迎[加入我们](CONTRIBUTION_CN.md)
