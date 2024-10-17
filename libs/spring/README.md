# Latta spring recording library

![Latta logo](../../docs/logo.svg)

> Seamlessly integrate exception monitoring into your Spring applications with Latta.


## Overview

Library provides robust error tracking and monitoring for your Spring applications. It automatically captures and reports both process-level exceptions and request-specific errors to the Latta reporting system using Spring interceptors.

## Installation

Dependency installation is different for each project management tool.

Choose yours: 

**maven**:

File: *pom.xml*

Append `dependency` to your project dependencies.

```xml
<dependency>
    <groupId>ai.latta</groupId>
    <artifactId>latta-spring-recorder</artifactId>
    <version>1.0</version>
    <scope>compile</scope>
</dependency>
```

**groovy (gradle)**

File: *build.gradle*

```groovy
implementation 'ai.latta:latta-spring-recorder:1.0'
```

**kotlin (gradle)**

File: *build.gradle.kts*

```kotlin
implementation("ai.latta:latta-spring-recorder:1.0")
```

## Usage

Adding Latta to your Spring application requires few changes in your `configurer` class.

1. Add `LattaInterceptor` interceptor

In your `configurer` class you need to override `addInterceptors` method like this:

```java
import ai.latta.spring.interceptors.LattaInterceptor;

...

@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LattaInterceptor("<API_KEY>")).addPathPatterns("/**");;
}
```

Don't forget to replace "<API_KEY>" with your API key obtained from Latta.

2. Add response filter bean

In your `configurer` add `applyLattaResponseFilter` method.

```java
import ai.latta.spring.filters.LattaResponseFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

...

@Bean
public FilterRegistrationBean<LattaResponseFilter> applyLattaResponseFilter() {
    FilterRegistrationBean<LattaResponseFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new LattaResponseFilter());
    registrationBean.addUrlPatterns("/*"); // Apply to all requests
    return registrationBean;
}
```

3. Done!
4. You can always look to your demo application for correct working solution


## Support

If you encounter any issues or need assistance:

- Email: [support@latta.ai](mailto:support@latta.ai)
- Website: [https://latta.ai](https://latta.ai)
- Documentation: [Full Documentation](https://docs.latta.ai/frameworks/spring/)

