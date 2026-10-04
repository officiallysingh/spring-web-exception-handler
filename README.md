# Guide to exception handling in Spring MVC applications

[![Java](https://img.shields.io/badge/java-21-blue.svg)](https://www.oracle.com/java/technologies/)
[![Spring Boot](https://img.shields.io/badge/spring_boot-4.1.1-blue.svg)](https://spring.io/projects/spring-boot)
[![Postgres](https://img.shields.io/badge/postgres-latest-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/mongodb-latest-blue.svg)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/maven-3.9.x-blue.svg)](https://maven.apache.org/)

**A library for handling exceptions in Spring MVC (servlet) applications**, implementing
[**Problem Details (RFC 7807) for HTTP APIs**](https://datatracker.ietf.org/doc/html/rfc7807).
Requires **Java 21** and **Spring Boot 4.1+** (Jakarta EE 11).

![Exception Handling](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*0s2E6-iNFqr_xptwrmJTdg.jpeg)

## Table of Contents
1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Features](#features)
4. [Controller advices](#controller-advices)
   - [General advices](#general-advices)
   - [DAO advices](#dao-advices)
   - [Security advices](#security-advices)
5. [Application configuration](#application-configuration)
6. [Library properties](#library-properties)
7. [Error key](#error-key)
8. [Error response](#error-response)
9. [Message resolvers](#message-resolvers)
10. [Message internationalization](#message-internationalization)
11. [Creating and throwing exceptions](#creating-and-throwing-exceptions)
12. [Customizations](#customizations)
    - [Customize messages](#customize-messages)
    - [Override an advice](#override-an-advice)
13. [Define new advices](#define-new-advices)
14. [Testing support](#testing-support)
15. [Example error responses](#example-error-responses)

## Introduction

Exception handling is a cross-cutting concern. Keep it separate from business logic and apply it declaratively.

A common practice is to invent a `ServiceException` plus an error-code enum for every failure.
Unchecked exceptions cover almost every case, so callers do not need `try` / `catch` or `throws`.
Spring already handles exceptions with `@RestControllerAdvice`. **spring-web-exception-handler** supplies that layer for **Spring MVC** REST applications: add the dependency, describe error attributes in a `properties` file, and the library turns thrown exceptions into a Problem Details response.

The error key used to look up those attributes is the exception's simple class name, such as `JobInstanceAlreadyCompleteException`. See [Error key](#error-key). The response body is described in [Error response](#error-response). Application code throws [`ThrowableProblem`](src/main/java/com/neolama/common/exception/core/ThrowableProblem.java) through [`Problems`](src/main/java/com/neolama/common/exception/core/Problems.java).

## Installation

> **Current version: 0.0.1-SNAPSHOT**

Add the jar. Autoconfiguration registers the advices when the library is on the classpath of a Spring MVC application.

Maven
```xml
<dependency>
    <groupId>com.neolama</groupId>
    <artifactId>spring-web-exception-handler</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
Gradle
```groovy
implementation 'com.neolama:spring-web-exception-handler:0.0.1-SNAPSHOT'
```

The application needs `spring-boot-starter-webmvc`. Security, PostgreSQL, and MongoDB handling switch on when those libraries are present. See [Controller advices](#controller-advices).

[`WebExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebExceptionHandler.java) is always registered and handles general, HTTP, I/O, routing, and validation exceptions. Exceptions with no dedicated advice still get a response: the fallback advice handles `Throwable`, and `code`, `title`, `detail`, and `status` can be set in a `properties` file. A custom advice is useful when the error key or the message placeholders must be built from fields on the exception. Implement [`Exceptional`](src/main/java/com/neolama/common/exception/advice/Exceptional.java) and follow an existing advice in this repository.

## Features

* `@RestControllerAdvice` implementations for the exceptions Spring MVC applications throw most often.
* An error response for any other exception, addressed by the exception simple name in a `properties` file.
* A stable [error response](#error-response): `type`, `title`, `status`, `detail`, `instance`, `method`, `timestamp`, and `code`, plus `violations`, `errors`, or extra attributes when the advice adds them.
* Message externalization and internationalization through Spring `MessageSource` and `LocaleContextHolder`.
* [`ProblemModule`](src/main/java/com/neolama/common/exception/jackson/ProblemModule.java) so Jackson writes `HttpStatus` and `HttpMethod` values used on the problem.
* DAO advices for PostgreSQL and MongoDB constraint failures, and security advices for Spring Security, each gated as described below.
* Room to override an advice or add one, using the same [`Exceptional`](src/main/java/com/neolama/common/exception/advice/Exceptional.java) contract.

## Controller advices

Advice types are interfaces. The autoconfigured `@RestControllerAdvice` classes implement those interfaces. Package root is `com.neolama.common.exception`.

[`WebExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebExceptionHandler.java) implements the general, HTTP, I/O, routing, and validation advice interfaces below.

### General advices

| Advice | Handles | Status | Error key |
| --- | --- | --- | --- |
| [**`GeneralAdvice`**](src/main/java/com/neolama/common/exception/advice/general/GeneralAdvice.java) | | | |
| `├──` [`ThrowableProblemAdvice`](src/main/java/com/neolama/common/exception/advice/general/ThrowableProblemAdvice.java) | [`ThrowableProblem`](src/main/java/com/neolama/common/exception/core/ThrowableProblem.java) | Taken from the problem | Already set when the problem was built |
| `├──` [`ThrowableAdvice`](src/main/java/com/neolama/common/exception/advice/general/ThrowableAdvice.java) | `Throwable` | [`500`](https://httpstatus.es/500), or `@ResponseStatus`, or `status.{SimpleName}` | Exception simple name |
| `└──` [`UnsupportedOperationAdvice`](src/main/java/com/neolama/common/exception/advice/general/UnsupportedOperationAdvice.java) | `UnsupportedOperationException` | [`501`](https://httpstatus.es/501) | `UnsupportedOperationException` |
| [**`HttpAdvice`**](src/main/java/com/neolama/common/exception/advice/http/HttpAdvice.java) | | | |
| `├──` [`HttpMediaTypeNotAcceptableAdvice`](src/main/java/com/neolama/common/exception/advice/http/HttpMediaTypeNotAcceptableAdvice.java) | `HttpMediaTypeNotAcceptableException` | [`406`](https://httpstatus.es/406) | `HttpMediaTypeNotAcceptableException` |
| `├──` [`HttpMediaTypeNotSupportedExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/http/HttpMediaTypeNotSupportedExceptionAdvice.java) | `HttpMediaTypeNotSupportedException` | [`415`](https://httpstatus.es/415) | `HttpMediaTypeNotSupportedException` |
| `├──` [`UnsupportedMediaTypeStatusAdvice`](src/main/java/com/neolama/common/exception/advice/http/UnsupportedMediaTypeStatusAdvice.java) | `UnsupportedMediaTypeStatusException` | [`415`](https://httpstatus.es/415) | `UnsupportedMediaTypeStatusException` |
| `├──` [`HttpRequestMethodNotSupportedAdvice`](src/main/java/com/neolama/common/exception/advice/http/HttpRequestMethodNotSupportedAdvice.java) | `HttpRequestMethodNotSupportedException` | [`405`](https://httpstatus.es/405) | `HttpRequestMethodNotSupportedException` |
| `├──` [`MethodNotAllowedAdvice`](src/main/java/com/neolama/common/exception/advice/http/MethodNotAllowedAdvice.java) | `MethodNotAllowedException` | [`405`](https://httpstatus.es/405) | `MethodNotAllowedException` |
| `├──` [`NotAcceptableStatusAdvice`](src/main/java/com/neolama/common/exception/advice/http/NotAcceptableStatusAdvice.java) | `NotAcceptableStatusException` | [`406`](https://httpstatus.es/406) | `NotAcceptableStatusException` |
| `└──` [`ResponseStatusAdvice`](src/main/java/com/neolama/common/exception/advice/http/ResponseStatusAdvice.java) | `ResponseStatusException` | Status carried by the exception | `ResponseStatusException` |
| [**`IOAdvice`**](src/main/java/com/neolama/common/exception/advice/io/IOAdvice.java) | | | |
| `├──` [`MessageNotReadableAdvice`](src/main/java/com/neolama/common/exception/advice/io/MessageNotReadableAdvice.java) | `HttpMessageNotReadableException` | [`400`](https://httpstatus.es/400) | `HttpMessageNotReadableException`, or keys derived from `InvalidFormatException` |
| `├──` [`MultipartAdvice`](src/main/java/com/neolama/common/exception/advice/io/MultipartAdvice.java) | `MultipartException` | [`400`](https://httpstatus.es/400) | `MultipartException` |
| `├──` [`MaxUploadSizeExceededExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/io/MaxUploadSizeExceededExceptionAdvice.java) | `MaxUploadSizeExceededException` | [`400`](https://httpstatus.es/400) | `MaxUploadSizeExceededException` |
| `└──` [`DataBufferLimitExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/io/DataBufferLimitExceptionAdvice.java) | `DataBufferLimitException` | [`400`](https://httpstatus.es/400) | `DataBufferLimitException` |
| [**`RoutingAdvice`**](src/main/java/com/neolama/common/exception/advice/routing/RoutingAdvice.java) | | | |
| `├──` [`MissingRequestHeaderAdvice`](src/main/java/com/neolama/common/exception/advice/routing/MissingRequestHeaderAdvice.java) | `MissingRequestHeaderException` | [`400`](https://httpstatus.es/400) | `MissingRequestHeaderException.{Controller}.{method}.{header}` |
| `├──` [`MissingServletRequestParameterAdvice`](src/main/java/com/neolama/common/exception/advice/routing/MissingServletRequestParameterAdvice.java) | `MissingServletRequestParameterException` | [`400`](https://httpstatus.es/400) | `MissingServletRequestParameterException.{parameterName}` |
| `├──` [`MissingServletRequestPartAdvice`](src/main/java/com/neolama/common/exception/advice/routing/MissingServletRequestPartAdvice.java) | `MissingServletRequestPartException` | [`400`](https://httpstatus.es/400) | `MissingServletRequestPartException.{partName}` |
| `├──` [`NoHandlerFoundAdvice`](src/main/java/com/neolama/common/exception/advice/routing/NoHandlerFoundAdvice.java) | `NoHandlerFoundException` | [`404`](https://httpstatus.es/404) | `NoHandlerFoundException` |
| `└──` [`ServletRequestBindingAdvice`](src/main/java/com/neolama/common/exception/advice/routing/ServletRequestBindingAdvice.java) | `ServletRequestBindingException` | [`400`](https://httpstatus.es/400) | `ServletRequestBindingException` |
| [**`ValidationAdvice`**](src/main/java/com/neolama/common/exception/advice/validation/ValidationAdvice.java) | | | |
| `├──` [`ConstraintViolationAdvice`](src/main/java/com/neolama/common/exception/advice/validation/ConstraintViolationAdvice.java) | `ConstraintViolationException` | [`400`](https://httpstatus.es/400) | `ConstraintViolationException` |
| `├──` [`BindAdvice`](src/main/java/com/neolama/common/exception/advice/validation/BindAdvice.java) | `BindException` | [`400`](https://httpstatus.es/400) | `BindException` |
| `├──` [`MethodArgumentNotValidAdvice`](src/main/java/com/neolama/common/exception/advice/validation/MethodArgumentNotValidAdvice.java) | `MethodArgumentNotValidException` | [`400`](https://httpstatus.es/400) | `MethodArgumentNotValidException` |
| `├──` [`MethodArgumentTypeMismatchAdvice`](src/main/java/com/neolama/common/exception/advice/validation/MethodArgumentTypeMismatchAdvice.java) | `MethodArgumentTypeMismatchException` | [`400`](https://httpstatus.es/400) | `MethodArgumentTypeMismatchException.{Controller}.{method}.{parameter}` |
| `└──` [`TypeMismatchAdvice`](src/main/java/com/neolama/common/exception/advice/validation/TypeMismatchAdvice.java) | `TypeMismatchException` | [`400`](https://httpstatus.es/400) | `TypeMismatchException.{errorCode}.{propertyName}` |

`InvalidFormatException` (Jackson, nested under `HttpMessageNotReadableException`) tries message codes from most specific to least specific:

1. `InvalidFormatException.{package}.{ClassNames}.{property}`
2. `InvalidFormatException.{ClassNames}.{property}`
3. `InvalidFormatException.{TargetType}.{property}`
4. `InvalidFormatException.{property}`

### DAO advices

| Advice | Handles | Status | Error key |
| --- | --- | --- | --- |
| [**`DaoAdvice`**](src/main/java/com/neolama/common/exception/advice/dao/DaoAdvice.java) | | | |
| `├──` [`DataIntegrityViolationAdvice`](src/main/java/com/neolama/common/exception/advice/dao/DataIntegrityViolationAdvice.java) | `DataIntegrityViolationException` | [`500`](https://httpstatus.es/500) | `DataIntegrityViolationException.{constraintName}` |
| `└──` [`DuplicateKeyExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/dao/DuplicateKeyExceptionAdvice.java) | `DuplicateKeyException` | [`500`](https://httpstatus.es/500) | `DuplicateKeyException.{constraintName}` |

[`WebDaoExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebDaoExceptionHandler.java) is the `@RestControllerAdvice` for these exceptions. It is registered when `problem.dao-advice-enabled` is `true` (the default).

[`DatabaseConstraintResolverConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/DatabaseConstraintResolverConfiguration.java) registers a [`ConstraintNameResolver`](src/main/java/com/neolama/common/exception/advice/dao/constraint/ConstraintNameResolver.java) under the same condition:

* [`PostgresConstraintNameResolver`](src/main/java/com/neolama/common/exception/advice/dao/constraint/PostgresConstraintNameResolver.java) when `org.postgresql.PGConnection` is on the classpath.
* [`MongoConstraintNameResolver`](src/main/java/com/neolama/common/exception/advice/dao/constraint/MongoConstraintNameResolver.java) when `com.mongodb.MongoClientSettings` is on the classpath.

The handler treats an exception message that contains `WriteError` as MongoDB and every other message as PostgreSQL. Constraint name resolvers exist for those two databases. For another database, implement `ConstraintNameResolver` and declare it as a bean.

### Security advices

| Advice | Handles | Status | Error key |
| --- | --- | --- | --- |
| [**`SecurityAdvice`**](src/main/java/com/neolama/common/exception/advice/security/SecurityAdvice.java) | | | |
| `├──` [`AuthenticationExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/security/AuthenticationExceptionAdvice.java) | `AuthenticationException` | [`401`](https://httpstatus.es/401) | `AuthenticationException` |
| `├──` [`InsufficientAuthenticationAdvice`](src/main/java/com/neolama/common/exception/advice/security/InsufficientAuthenticationAdvice.java) | `InsufficientAuthenticationException` | [`401`](https://httpstatus.es/401) | `InsufficientAuthenticationException` |
| `├──` [`BadCredentialsExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/security/BadCredentialsExceptionAdvice.java) | `BadCredentialsException` | [`401`](https://httpstatus.es/401) | `BadCredentialsException` |
| `├──` [`UsernameNotFoundAdvice`](src/main/java/com/neolama/common/exception/advice/security/UsernameNotFoundAdvice.java) | `UsernameNotFoundException` | [`401`](https://httpstatus.es/401) | `UsernameNotFoundException` |
| `└──` [`AccessDeniedExceptionAdvice`](src/main/java/com/neolama/common/exception/advice/security/AccessDeniedExceptionAdvice.java) | `AccessDeniedException` | [`403`](https://httpstatus.es/403) | `AccessDeniedException` |

[`WebSecurityExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebSecurityExceptionHandler.java) is registered when `problem.security-advice-enabled` is `true` (the default) and `org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration` is on the classpath.

It also publishes:

* [`ProblemAuthenticationEntryPoint`](src/main/java/com/neolama/common/exception/advice/security/ProblemAuthenticationEntryPoint.java) as an `AuthenticationEntryPoint` bean
* [`ProblemAccessDeniedHandler`](src/main/java/com/neolama/common/exception/advice/security/ProblemAccessDeniedHandler.java) as an `AccessDeniedHandler` bean

Wire those beans into the application's `SecurityFilterChain` so authentication and access-denied failures go through the same problem response:

```java
@Autowired
private AuthenticationEntryPoint authenticationEntryPoint;

@Autowired
private AccessDeniedHandler accessDeniedHandler;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/swagger-ui.*", "/v3/api-docs", "/v3/api-docs/**", "/webjars/**")
                .permitAll()
                .anyRequest()
                .authenticated()
            );

    if (this.authenticationEntryPoint != null) {
      http.exceptionHandling(
              exceptionHandling ->
                      exceptionHandling.authenticationEntryPoint(this.authenticationEntryPoint));
    }
    if (this.accessDeniedHandler != null) {
      http.exceptionHandling(
              exceptionHandling -> exceptionHandling.accessDeniedHandler(this.accessDeniedHandler));
    }

    return http.build();
}
```

## Application configuration

[`NoHandlerFoundAdvice`](src/main/java/com/neolama/common/exception/advice/routing/NoHandlerFoundAdvice.java) needs Spring MVC to throw when no handler matches:

```properties
spring.mvc.throw-exception-if-no-handler-found=true
```

Leave Spring Boot's own problem-details support off. `spring.mvc.problemdetails.enabled` defaults to `false`. Turning it on lets Boot's handler shadow the advices in this library.

```properties
spring.mvc.problemdetails.enabled=false
```

Disable Boot's MVC error page so it does not replace the problem body:

```java
@EnableAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
```

or

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
```

Point `MessageSource` at the application bundle and at `i18n/problems`, which ships with this library. Put `i18n/problems` last so application messages win.

```properties
spring.messages.basename=i18n/errors,i18n/problems
spring.messages.use-code-as-default-message=true
```

The bundled file [`i18n/problems.properties`](src/main/resources/i18n/problems.properties) defines:

```properties
detail.ConcurrencyFailureException=Conflicted with another concurrent update, please retry
detail.UnsupportedOperationException=The requested operation is not supported yet
detail.MaxUploadSizeExceededException=Upload file size exceeded the maximum allowed limit: {0}
```

`ProblemMessageProvider.getMessage` passes a default when a code is missing, so a missing key falls back to that default (HTTP status phrase, exception message, or the built-in detail). `use-code-as-default-message=true` covers lookups that do not pass a default.

## Library properties

These are the only `problem.*` properties. They are declared in [`spring-configuration-metadata.json`](src/main/resources/META-INF/spring-configuration-metadata.json).

```properties
problem.type-url=http://localhost:8080/problems/help.html
problem.dao-advice-enabled=true
problem.security-advice-enabled=true
```

| Property | Default | Meaning |
| --- | --- | --- |
| `problem.type-url` | `http://localhost:8080/problems/help.html` | Help page base URL declared for the library. |
| `problem.dao-advice-enabled` | `true` | Enables the DAO advice and the PostgreSQL and MongoDB constraint name resolvers. |
| `problem.security-advice-enabled` | `true` | Enables security exception handling when Spring Security is on the classpath. |

[`ProblemConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/ProblemConfiguration.java) always registers [`ProblemMessageProvider`](src/main/java/com/neolama/common/exception/autoconfigure/ProblemMessageProvider.java) and [`ProblemModule`](src/main/java/com/neolama/common/exception/jackson/ProblemModule.java).

## Error key

The error key selects `code`, `title`, `detail`, and, where applicable, `status` in the message bundles. It must be unique for each error you want to phrase differently.

For an exception whose advice does not build a more specific key, the key is the **simple class name**:

| Exception | Error key |
| --- | --- |
| `org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException` | `JobInstanceAlreadyCompleteException` |
| `org.springframework.web.bind.MethodArgumentNotValidException` | `MethodArgumentNotValidException` |
| `java.lang.IllegalStateException` | `IllegalStateException` |

Some advices append detail so two failures of the same type can have different messages:

| Situation | Error key |
| --- | --- |
| Missing query parameter `page` | `MissingServletRequestParameterException.page` |
| Missing header `X-Request-Id` on `OrderController.create` | `MissingRequestHeaderException.OrderController.create.X-Request-Id` |
| PostgreSQL constraint `uk_employee_name` | `DataIntegrityViolationException.uk_employee_name` |
| Type mismatch on property `dateTime` | `TypeMismatchException.{errorCode}.dateTime` |
| Problem you throw yourself | The key you pass to `Problems.of(...)`, or `ErrorType.getErrorKey()` |

With key `some.error.key`, the bundle entries are:

```properties
code.some.error.key=some-error
title.some.error.key=Some Error
detail.some.error.key=Something has gone wrong, please look into the logs for details
```

When no dedicated advice sets the status (the `Throwable` fallback, and any key you want to remap), also set:

```properties
status.some.error.key=400
```

`status.` is also consulted by advices that start from a default status. A matching `status.{errorKey}` entry replaces that default. The value is the numeric code, for example `409`.

Renaming a class, a controller method, a bean property, or a database constraint changes the derived key. Update the bundle when that happens.

## Error response

Example body:

```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "A job instance already exists and is complete for parameters={'date':'{value=2023-08-13, type=class java.time.LocalDate, identifying=true}'}.  If you want to run this job again, change the parameters.",
  "instance": "/api/myjob",
  "method": "PUT",
  "timestamp": "2023-08-14T15:15:45.737227Z",
  "code": "XYZ-001"
}
```

`Content-Type` is `application/problem+json` for JSON and `application/problem+xml` for XML.

[`ProblemDetails`](src/main/java/com/neolama/common/exception/core/ProblemDetails.java) extends Spring's `ProblemDetail` and adds `code`, `method`, `timestamp`, `violations`, and `errors`.

* `type` — URI of the problem type. Set to `about:blank`, which is Spring's default when no more specific type is configured.
* `title` — Short summary, such as `Bad Request`.
* `status` — HTTP status code, such as `500`.
* `detail` — Explanation for this occurrence.
* `instance` — Request URI.
* `method` — HTTP method of that request.
* `timestamp` — `OffsetDateTime` when the problem was created (UTC).
* `code` — Stable code for this error, used in `type`. Prefer letters, digits, `_`, and `-`.

Validation advices add `violations`, a list of [`Problem`](src/main/java/com/neolama/common/exception/core/Problem.java) objects (`code`, `message`, `details`). `Problems.of` with several problems adds `errors`, the same object shape. `Problems` `.parameter(...)` copies extra attributes onto the problem body. Names `type`, `status`, `instance`, `method`, `code`, `title`, `detail`, `timestamp`, `violations`, and `errors` are reserved.

If the bundle has no `code.` entry, the default code is:

* the error key converted to kebab-case, with a trailing `-exception` removed, when the key contains no `.`  
  `JobInstanceAlreadyCompleteException` → `job-instance-already-complete`
* the status name in kebab-case when the key contains a `.`  
  HTTP 400 → `bad-request`, HTTP 500 → `internal-server-error`

`title` defaults to the status reason phrase. `detail` defaults to the detail passed by the advice (usually `exception.getMessage()`), or `An unexpected error occurred`.

## Message resolvers

Error keys are written at **TRACE** by [`Exceptional`](src/main/java/com/neolama/common/exception/advice/Exceptional.java). Enable that logger while you fill in the bundle:

```properties
logging.level.com.neolama.common.exception.advice.Exceptional=TRACE
```

A single key is logged as:

```text
---------------------- Error Keys ----------------------
JobInstanceAlreadyCompleteException
--------------------------------------------------------
```

Validation advices log every candidate key, most specific first. Copy the key you want into `errors.properties`:

```properties
status.JobInstanceAlreadyCompleteException=409
code.JobInstanceAlreadyCompleteException=Some code
title.JobInstanceAlreadyCompleteException=Some title
detail.JobInstanceAlreadyCompleteException=Some message details
```

For a `ConstraintViolationException` on property `name`, the violation message codes are:

```properties
detail.ConstraintViolationException.name=User name length should be between 3 and 10
message.ConstraintViolationException.name=name
code.ConstraintViolationException.name=400
```

For `MethodArgumentNotValidException` and `BindException`, field messages use Spring's field-error codes, prefixed by `detail.`, `message.`, or `code.` and the exception simple name. The TRACE log lists those codes. Arguments available to the message are the field name, the rejected value, and the constraint arguments.

When `status.{errorKey}` is set and `code.` / `title.` / `detail.` are omitted:

* **Code** follows the rules in [Error response](#error-response).
* **Title** is the reason phrase of the resolved status.
* **Detail** is the exception message, or the advice default.

`status.` is read for every advice that calls `resolveStatus`. The `Throwable` fallback uses it when the exception has no `@ResponseStatus`.

## Message internationalization

[`ProblemMessageProvider`](src/main/java/com/neolama/common/exception/autoconfigure/ProblemMessageProvider.java) resolves messages with `LocaleContextHolder.getLocale()`. Spring Boot's `AcceptHeaderLocaleResolver` reads that locale from the `Accept-Language` header.

An `Accept-Language: fr` request reads `errors_fr.properties` (and `problems_fr.properties` if you add one) from the basenames configured above.

Spring's `MessageSource` treats a single quote in a pattern as a quote delimiter. Escape a literal quote with two single quotes (`''`).

## Creating and throwing exceptions

Throw [`ThrowableProblem`](src/main/java/com/neolama/common/exception/core/ThrowableProblem.java), an unchecked exception that already carries a [`ProblemDetails`](src/main/java/com/neolama/common/exception/core/ProblemDetails.java). [`ThrowableProblemAdvice`](src/main/java/com/neolama/common/exception/advice/general/ThrowableProblemAdvice.java) writes that body.

[`Problems`](src/main/java/com/neolama/common/exception/core/Problems.java) is the builder.

Simplest form: an error key plus a status. `code`, `title`, and `detail` come from the bundle.

```java
throw Problems.of("sample.problem").throwAble(HttpStatus.EXPECTATION_FAILED);
```

```properties
code.sample.problem=AYX123
title.sample.problem=Some title
detail.sample.problem=Some message details
```

`sample.problem` contains a `.`, so a missing `code.` entry defaults to the status name `expectation-failed`. A missing `title.` entry defaults to `Expectation Failed`. A missing `detail.` entry defaults to `An unexpected error occurred` unless you pass `defaultDetail`.

```java
throw Problems.of("sample.problem")
    .defaultDetail("Default details if not found in properties file with param1: {0} and param2: {1}")
    .detailArgs("P1", "P2")
    .cause(new IllegalStateException("Artificially induced illegal state"))
    .throwAble(HttpStatus.EXPECTATION_FAILED);
```

```properties
code.sample.problem=404
title.sample.problem=Some title
detail.sample.problem=Some details with param one: {0} and param other: {1}
```

`throwAble()` with no status uses `HttpStatus.INTERNAL_SERVER_ERROR`, unless you started from an [`ErrorType`](src/main/java/com/neolama/common/exception/core/ErrorType.java), in which case it uses `ErrorType.getStatus()`.

Extra attributes:

```java
throw Problems.of("invalid.request")
    .defaultDetail("Invalid request received, Please retry with correct input")
    .parameter("additional-attribute", "Some additional attribute")
    .throwAble(HttpStatus.BAD_REQUEST);
```

An enum (or [`DefaultProblem`](src/main/java/com/neolama/common/exception/core/DefaultProblem.java)) can hold the key, the default detail, and the status. Bundle entries for that key still override `detail`, `title`, and `code`.

```java
@Getter
public enum AppErrors implements ErrorType {

  REMOTE_HOST_NOT_AVAILABLE(
      "remote.host.not.available",
      "Looks like something wrong with remote host: {0}",
      HttpStatus.SERVICE_UNAVAILABLE);

  private final String errorKey;
  private final String defaultDetail;
  private final HttpStatus status;

  AppErrors(final String errorKey, final String defaultDetail, final HttpStatus status) {
    this.errorKey = errorKey;
    this.defaultDetail = defaultDetail;
    this.status = status;
  }
}
```

```java
throw Problems.of(AppErrors.REMOTE_HOST_NOT_AVAILABLE)
    .detailArgs("http://some.remote.host.com")
    .throwAble();
```

Several problems in one response use [`Problem`](src/main/java/com/neolama/common/exception/core/Problem.java) (`code`, `message`, `details`). The HTTP status is `207 Multi-Status`, and the list is exposed as `errors`.

```java
throw Problems.of(
        Problem.of("500", "Internal Server Error", "First problem"),
        Problem.of("503", "Service Unavailable", "Second problem"))
    .throwAble();
```

`Problem.of(errorKey)` and `Problem.of(errorKey, defaultCode, defaultMessage, defaultDetails)` resolve `code.`, `title.`, and `detail.` for that key before the object is stored. `title.` is stored in `Problem.message` and `detail.` in `Problem.details`.

`@ResponseStatus` on an exception type is honored by the `Throwable` fallback. Other attributes then default from that status, and the error key is still the simple class name.

```java
@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class MyException extends RuntimeException {
}
```

## Customizations

### Customize messages

The usual customization is the properties file: `code.{errorKey}`, `title.{errorKey}`, `detail.{errorKey}`, and `status.{errorKey}`. See [Message resolvers](#message-resolvers).

### Override an advice

Declare another `@RestControllerAdvice` that implements the same advice interface and give it `@Order(Ordered.HIGHEST_PRECEDENCE)` so it is chosen ahead of [`WebExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebExceptionHandler.java).

[`MethodArgumentNotValidAdvice`](src/main/java/com/neolama/common/exception/advice/validation/MethodArgumentNotValidAdvice.java) builds each field problem in `handleFieldError`. Override that method to change how a field violation is turned into a [`Problem`](src/main/java/com/neolama/common/exception/core/Problem.java). The snippet below uses a `@JsonProperty` name in the fallback detail. Message codes still come from Spring's field-error codes:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class CustomMethodArgumentNotValidAdvice implements MethodArgumentNotValidAdvice {

  @Override
  public Problem handleFieldError(
      final FieldError fieldError, final Throwable exception, final HttpStatus status) {
    String field = fieldError.getField();
    try {
      if (fieldError.contains(ConstraintViolation.class)) {
        final ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
        final Field declaredField = violation.getRootBeanClass().getDeclaredField(field);
        final JsonProperty annotation = declaredField.getAnnotation(JsonProperty.class);
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
          field = annotation.value();
        }
      }
    } catch (Exception ignored) {
      // Keep the Java field name when it cannot be read.
    }

    final String prefix = ClassUtils.getShortClassName(exception.getClass());
    final ProblemMessageSourceResolver codeResolver =
        ProblemMessageSourceResolver.of(CODE_CODE_PREFIX + prefix, fieldError, status.value());
    final ProblemMessageSourceResolver messageResolver =
        ProblemMessageSourceResolver.of(MESSAGE_CODE_PREFIX + prefix, fieldError);
    final ProblemMessageSourceResolver detailsResolver =
        ProblemMessageSourceResolver.of(
            DETAIL_CODE_PREFIX + prefix, fieldError, "Invalid value for property " + field);
    return Problem.of(
        ProblemMessageProvider.getMessage(codeResolver),
        ProblemMessageProvider.getMessage(messageResolver),
        ProblemMessageProvider.getMessage(detailsResolver));
  }
}
```

## Define new advices

A new exception type needs an advice only when the key or the message must be computed from the exception. Implement [`Exceptional`](src/main/java/com/neolama/common/exception/advice/Exceptional.java), annotate with `@RestControllerAdvice` and `@Order(Ordered.HIGHEST_PRECEDENCE)`, and return `toResponse(...)` so `type`, `instance`, and `method` are set.

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MyCustomAdvice implements Exceptional {

  @ExceptionHandler
  public ProblemDetails handleMyCustomException(
      final MyCustomException exception, final NativeWebRequest request) {
    final String errorKey = exception.getClass().getSimpleName();
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, HttpStatus.BAD_REQUEST, exception.getMessage());
    return toResponse(problemDetails, request, exception);
  }
}
```

If the key can stay as the simple class name and the status is fixed, `@ResponseStatus` plus bundle entries are enough and this class is unnecessary. [`ThrowableAdvice`](src/main/java/com/neolama/common/exception/advice/general/ThrowableAdvice.java) will handle it.

## Testing support

Autoconfiguration classes, from [`AutoConfiguration.imports`](src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports):

| Configuration | Role |
| --- | --- |
| [`ProblemConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/ProblemConfiguration.java) | `ProblemMessageProvider` and `ProblemModule` |
| [`WebExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebExceptionHandler.java) | General, HTTP, I/O, routing, and validation advices |
| [`DatabaseConstraintResolverConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/DatabaseConstraintResolverConfiguration.java) | PostgreSQL and MongoDB constraint name resolvers |
| [`WebDaoExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebDaoExceptionHandler.java) | DAO advices |
| [`WebSecurityExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebSecurityExceptionHandler.java) | Security advices and the entry point / access-denied handler |

`@WebMvcTest` does not always apply library autoconfiguration. Import the classes the test needs:

```java
@TestConfiguration
@ImportAutoConfiguration(
    classes = {
      ProblemConfiguration.class,
      WebExceptionHandler.class
      // WebSecurityExceptionHandler.class
      // WebDaoExceptionHandler.class
      // DatabaseConstraintResolverConfiguration.class
    })
public class WebTestConfiguration {
}
```

```java
@WebMvcTest(MyController.class)
@ImportAutoConfiguration(classes = {WebTestConfiguration.class})
class StateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MyService myService;

  @Test
  @DisplayName("Test Create Resource successfully")
  void testCreateResource_Success() throws Exception {
  }
}
```

## Example error responses
**Following are example error responses in different scenarios.**
The error response attributes `code`, `title` and `detail` can be customized for each error by specifying
the same in `errors.properties` file for different error keys. Enable TRACE logging on `com.neolama.common.exception.advice.Exceptional` to see those keys.

### Constraint violations
#### Jakarta Constraint violations error
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Constraint violations has happened, please correct the request and try again",
  "instance": "/problems/handler-constraint-violation",
  "method": "POST",
  "timestamp": "2023-10-29T16:41:59.876471+05:30",
  "code": "constraint-violations",
  "violations": [
    {
      "code": "400",
      "detail": "User name length should be between 3 and 10",
      "propertyPath": "name"
    },
    {
      "code": "400",
      "detail": "Address state name is required",
      "propertyPath": "address.state"
    },
    {
      "code": "400",
      "detail": "User designation length should be between 2 and 5",
      "propertyPath": "designation"
    }
  ]
}
```

#### PostgresDB Unique constraint violation error
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Employee name must be unique, a record with given name already exists",
  "instance": "/api/employees",
  "method": "POST",
  "timestamp": "2023-10-29T16:44:10.917194+05:30",
  "code": "500"
}
```

#### MongoDB Unique constraint violation error
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "State name must be unique",
  "instance": "/api/states",
  "method": "POST",
  "timestamp": "2023-10-29T16:44:44.806613+05:30",
  "code": "500"
}
```

#### Invalid Query parameters
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Constraint violations has happened, please correct the request and try again",
  "instance": "/problems/handler-invalid-query-strings",
  "method": "GET",
  "timestamp": "2023-10-29T14:51:37.889537+05:30",
  "code": "constraint-violations",
  "violations": [
    {
      "code": "400",
      "detail": "must be greater than or equal to 0",
      "propertyPath": "page"
    }
  ]
}
```

#### Invalid format error
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid date time value or format. Expected a valid date time in ISO format",
  "instance": "/problems/handler-datetime-conversion",
  "method": "GET",
  "timestamp": "2023-10-29T16:05:09.953099+05:30",
  "code": "400",
  "propertyPath": "dateTime"
}
```

#### File upload max size exceeds error
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Upload file size exceeded the maximum allowed limit: 10485760B",
  "instance": "/problems/uploadfile",
  "method": "POST",
  "timestamp": "2023-10-29T14:31:33.073971+05:30",
  "code": "400"
}
```

### Spring framework thrown exceptions
#### Invalid Media type error
```json
{
  "type": "about:blank",
  "title": "Unsupported Media Type",
  "status": 415,
  "detail": "Media Type: application/xml Not Acceptable, Supported Media Types are: application/json",
  "instance": "/problems/handler-json-body",
  "method": "POST",
  "timestamp": "2023-10-29T14:45:47.467268+05:30",
  "code": "415"
}
```

#### Method not allowed error
```json
{
  "type": "about:blank",
  "title": "Method Not Allowed",
  "status": 405,
  "detail": "Requested Method: POST not allowed, allowed methods are: GET, PUT",
  "instance": "/problems/handler-datetime-conversion",
  "method": "POST",
  "timestamp": "2023-10-29T16:15:08.916369+05:30",
  "code": "405"
}
```

### Programmatically thrown exceptions
#### Any unhandled Throwable
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Expected argument invalid",
  "instance": "/problems/handler-throwable",
  "method": "GET",
  "timestamp": "2023-10-29T14:49:40.998497+05:30",
  "code": "500"
}
```

#### Error with dynamic additional attributes
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request received, Please retry with correct input",
  "instance": "/problems/throw-problem-with-additional-attribute",
  "method": "GET",
  "timestamp": "2023-10-29T16:24:37.976724+05:30",
  "code": "3456",
  "additional-attribute": "Some additional attribute"
}
```

#### Multiple errors
```json
{
  "type": "about:blank",
  "title": "Multi-Status",
  "status": 207,
  "detail": "Multi-Status",
  "instance": "/problems/throw-multiple-problems",
  "method": "GET",
  "timestamp": "2023-10-29T16:22:53.363785+05:30",
  "code": "207",
  "errors": [
    {
      "code": "500",
      "title": "Internal Server Error",
      "detail": "Sample error message defined in 'errors.properties'"
    },
    {
      "code": "503",
      "title": "Service Unavailable",
      "detail": "Looks like something wrong with remote host: http://some.remote.host.com"
    },
    {
      "code": "3456",
      "title": "Bad Request",
      "detail": "Invalid request received, Please retry with correct input",
      "additional-attribute": "Some additional attribute"
    },
    {
      "code": "500",
      "title": "Internal Server Error",
      "detail": "Just for testing exception"
    },
    {
      "code": "111",
      "title": "Dummy",
      "detail": "Hardcode attributes broblem"
    }
  ]
}
```

### OpenAPI Specification violation error
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Constraint violations has happened, please correct the request and try again",
  "instance": "/api/pets",
  "method": "POST",
  "timestamp": "2023-10-29T16:06:18.335463+05:30",
  "code": "constraint-violations",
  "violations": [
    {
      "code": "400",
      "detail": "[Path '/id'] Numeric instance is lower than the required minimum (minimum: 1, found: 0)",
      "propertyPath": "id"
    }
  ]
}
```

### Security error
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Either Authorization header bearer token is missing or invalid",
  "instance": "/api/employees/1",
  "method": "GET",
  "timestamp": "2023-10-29T16:08:40.466566+05:30",
  "code": "401"
}
```

## Licence
Open source [**The MIT License**](http://www.opensource.org/licenses/mit-license.php)

## Authors and acknowledgment
[**Rajveer Singh**](https://www.linkedin.com/in/rajveer-singh-589b3950/), In case you find any issues or need any support, please email me at raj14.1984@gmail.com.
Please give me a :star: if you find it helpful.

## Credits and references
Inspired by [**Zalando Problem libraries**](https://github.com/zalando/problem-spring-web).

## Known issues
* [`ConstraintNameResolver`](src/main/java/com/neolama/common/exception/advice/dao/constraint/ConstraintNameResolver.java) implementations exist for PostgreSQL and MongoDB. [`WebDaoExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebDaoExceptionHandler.java) sends a message that contains `WriteError` to the MongoDB resolver and every other message to the PostgreSQL resolver. An application that uses another database, or both PostgreSQL and another relational database, needs its own `ConstraintNameResolver`.
