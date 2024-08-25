# openGemini Spring Integrations

openGemini Spring Integrations provide seamless integration between the openGemini client and Spring applications.
This project offers both synchronous and asynchronous configurations tailored to different Spring environments.

## Overview

This project includes three primary modules:

- opengemini-spring: Core integration module for [Spring](https://spring.io/) applications.
- opengemini-spring-boot-starter: Synchronous starter for [Spring WebMVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html).
- opengemini-spring-boot-starter-reactive: Asynchronous starter for [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html).

## Features

- SpringBoot auto configuration support for an OpenGeminiTemplate/ReactiveOpenGeminiTemplate instance.
- `OpenGeminiTemplate`/`ReactiveOpenGeminiTemplate` class that increases productivity performing common openGemini operations.
  Includes integrated object mapping between points and POJOs.
- Annotation based mapping metadata.
- Support create database and retention policy automatically.

## Prerequisites

This project requires JDK 17 or later and supports Spring 6 and Spring Boot 3.

## Getting Started With Spring Boot Starter

Below is a brief example demonstrating how to use the OpenGemini Spring Boot Starter in a Java application.

### Maven Configuration

Add the following dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.opengemini</groupId>
    <artifactId>opengemini-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### SpringBoot Application Configuration

Following properties can be used in your `application.yaml`:

```yaml
spring:
  opengemini:
    addresses: localhost:8086
```

### Code Sample

```java
@Database(name = "test_db", create = false)
@RetentionPolicy(name = "test_rp", create = false)
@Measurement(name = "test_ms")
public class Weather {

    @Tag(name = "Location")
    private String location;

    @Field(name = "Temperature")
    private Double temperature;

    @Time(precision = Precision.PRECISIONMILLISECOND)
    private Long time;

}
```

```java
@Service
public class WeatherService {

    private final OpenGeminiTemplate template;

    public WeatherService(OpenGeminiTemplate template) {
        this.template = template;
    }

    public void doWork() {
        Weather weather = new Weather();
        weather.setLocation("shenzhen");
        weather.setTemperature(30.5D);
        weather.setTime(System.currentTimeMillis());

        MeasurementOperations<Weather> operations = template.opsForMeasurement(Weather.class);
        operations.write(weather);

        Query query = new Query("SELECT * FROM \"test_ms\" limit 1");
        List<Weather> results = operations.query(query);
    }
}
```
