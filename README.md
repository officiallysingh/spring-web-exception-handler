# spring-web-exception-handler

[![Java](https://img.shields.io/badge/java-21-blue.svg)](https://www.oracle.com/java/technologies/)
[![Spring Boot](https://img.shields.io/badge/spring_boot-4.1.1-blue.svg)](https://spring.io/projects/spring-boot)
[![Postgres](https://img.shields.io/badge/postgres-supported-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/mongodb-supported-blue.svg)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/maven-3.9.x-blue.svg)](https://maven.apache.org/)

**Consistent error responses for Spring Boot servlet (Spring MVC) REST APIs.** Add the dependency and
every exception your application throws, or Spring throws for it, becomes an
[RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) response:

```json
{
  "type": "about:blank",
  "status": 404,
  "instance": "/api/employees/42",
  "method": "GET",
  "code": "employee-not-found",
  "title": "Not Found",
  "detail": "Employee 42 not found",
  "timestamp": "2026-10-25T14:10:42.684674Z"
}
```

The wording of every response (`code`, `title`, `detail`) and its `status` can be changed in a
`properties` file, per exception and per locale, without touching code.

## Contents

1. [Requirements](#requirements)
2. [Installation](#installation)
3. [Quick start](#quick-start)
4. [The error response](#the-error-response)
5. [Messages and error keys](#messages-and-error-keys)
6. [Throwing problems from your code](#throwing-problems-from-your-code)
7. [Handled exceptions](#handled-exceptions)
8. [Validation errors](#validation-errors)
9. [Database errors](#database-errors)
10. [Spring Security](#spring-security)
11. [Configuration reference](#configuration-reference)
12. [Customizing](#customizing)
13. [Logging](#logging)
14. [Testing your application](#testing-your-application)
15. [Example responses](#example-responses)
16. [Limitations](#limitations)

## Requirements

* Java 21
* Spring Boot 4.1+ **servlet** web application (`spring-boot-starter-webmvc`). Reactive (WebFlux)
  and non-web applications are not supported; the library switches itself off there.
* Jackson 3, which comes with `spring-boot-starter-webmvc`.

Optional integrations switch on when the matching library is on your classpath:

| Your application has | You get |
| --- | --- |
| `spring-boot-starter-validation` | Validation errors with one entry per failed field or parameter ([Validation errors](#validation-errors)) |
| `spring-tx` (comes with Spring Data JPA, JDBC, or MongoDB) | Data-integrity and duplicate-key handling ([Database errors](#database-errors)) |
| PostgreSQL JDBC driver or MongoDB driver | Constraint and index names in error keys |
| `spring-boot-starter-security` | Authentication and authorization errors ([Spring Security](#spring-security)) |

The web, validation, security, and database libraries are optional dependencies of this library, so
your application decides which ones, and which versions, it uses.

## Installation

> **Current version: 1.0.0-rc2**, available from
> [Maven Central](https://central.sonatype.com/artifact/com.neolama/spring-web-exception-handler).

Maven

```xml
<dependency>
    <groupId>com.neolama</groupId>
    <artifactId>spring-web-exception-handler</artifactId>
    <version>1.0.0-rc2</version>
</dependency>
```

Gradle

```groovy
implementation 'com.neolama:spring-web-exception-handler:1.0.0-rc2'
```

Spring Boot auto-configuration registers everything. There is no annotation to add.

## Quick start

1. **Add the dependency** (above).
2. **Point Spring at your message bundle and the library's bundle.** List the library's
   `i18n/problems` last, so your messages win:

   ```properties
   # src/main/resources/application.properties
   spring.messages.basename=i18n/errors,i18n/problems
   ```

   Spring Boot creates a `MessageSource` only when a bundle named in `spring.messages.basename`
   (default `messages`) exists. Without one, every response uses the library's built-in defaults.
3. **Run your application.** Unknown paths, wrong methods, invalid bodies, failed validation, missing
   parameters, database constraint violations, security failures, and any other exception now return
   `application/problem+json`. See [Example responses](#example-responses).
4. **Phrase the errors your API returns** in `src/main/resources/i18n/errors.properties`:

   ```properties
   detail.DuplicateKeyException.employees_name_key=An employee with this name already exists
   status.DuplicateKeyException.employees_name_key=409
   ```

   [Messages and error keys](#messages-and-error-keys) explains the keys.
5. **Throw your own errors** with [`Problems`](#throwing-problems-from-your-code):

   ```java
   throw Problems.of(AppErrors.EMPLOYEE_NOT_FOUND).detailArgs(id).throwAble();
   ```

## The error response

Every response is `application/problem+json`, written from
[`ProblemDetails`](src/main/java/com/neolama/common/exception/core/ProblemDetails.java), an
extension of Spring's `ProblemDetail`. Fields appear in this order; fields without a value are left
out.

| Field | Meaning |
| --- | --- |
| `type` | Problem type URI. `about:blank` unless [`problem.type-url`](#configuration-reference) is set, then `{type-url}#{code}`. |
| `status` | HTTP status code, the same as the response status. |
| `instance` | Request path, such as `/api/employees/42`. |
| `method` | HTTP method of the request. |
| `code` | Stable, machine-readable code for this error. |
| `title` | Short summary, by default the status reason phrase such as `Not Found`. |
| `detail` | Explanation of this occurrence. |
| `timestamp` | Time the problem was created, ISO-8601 in UTC. |
| `violations` | Validation errors only: one entry per failed field or parameter. |
| `errors` | Problems that bundle several errors: one entry per error. |

Entries in `violations` and `errors` have `code`, `message`, and `details`. Extra fields you add with
`Problems...parameter(name, value)` appear at the top level after `timestamp`.

**Default `code`.** When no `code.{errorKey}` message is configured:

* an error key **without** a dot becomes kebab-case, one segment per capital letter, keeping the
  `Exception` suffix: `NoResourceFoundException` → `no-resource-found-exception`, `IOException` →
  `i-o-exception`, `OrderNotFound` → `order-not-found`;
* an error key **with** a dot becomes the status name: `MissingServletRequestParameterException.page`
  with status 400 → `bad-request`.

**Default `detail`.** Server errors (5xx) never show the exception message to clients; it stays in
the server log. The default is `An unexpected error occurred` unless a `detail.{errorKey}` message is
configured. Client errors (4xx) use a safe library default or Spring's own client message; see
[Handled exceptions](#handled-exceptions).

**Headers.** `405` responses carry `Allow` with the supported methods. `415` and `406` responses
carry `Accept` with the supported media types.

## Messages and error keys

Every problem has an **error key**. The library looks up these messages for it, in the
`MessageSource` and for the request's locale:

| Message | Sets | Example |
| --- | --- | --- |
| `code.{errorKey}` | `code` | `code.DuplicateKeyException.employees_name_key=employee-name-taken` |
| `title.{errorKey}` | `title` | `title.DuplicateKeyException.employees_name_key=Duplicate employee` |
| `detail.{errorKey}` | `detail` | `detail.DuplicateKeyException.employees_name_key=An employee with this name already exists` |
| `status.{errorKey}` | HTTP status | `status.DuplicateKeyException.employees_name_key=409` |

A configured message always wins over the library's and your Java defaults. A `status.` value that is
not a known status code is ignored.

### Which error key?

| Situation | Error key |
| --- | --- |
| Any exception without a more specific key | Simple class name: `IllegalStateException`, `OrderLockedException` |
| A problem you throw | The key you pass to `Problems.of("...")`, or `ErrorType.getErrorKey()` |
| Missing query parameter `page` | `MissingServletRequestParameterException.page` |
| Missing header `X-Tenant` on `OrderController.create` | `MissingRequestHeaderException.OrderController.create.X-Tenant` |
| Missing multipart part `file` | `MissingServletRequestPartException.file` |
| Wrong type for parameter `joinedAfter` on `EmployeeController.search` | `MethodArgumentTypeMismatchException.EmployeeController.search.joinedAfter` |
| Database constraint `employees_name_key` | `DuplicateKeyException.employees_name_key` ([Database errors](#database-errors)) |
| Field and parameter validation | Several candidate keys, most specific first ([Validation errors](#validation-errors)) |
| Invalid value in a JSON body | Several candidate keys, most specific first ([Handled exceptions](#invalid-json-values)) |

**To see the exact keys**, set the logger to TRACE while you write your bundle. Every key that is
looked up is logged, most specific first:

```properties
logging.level.com.neolama.common.exception.advice.Exceptional=TRACE
```

```text
---------------------- Error Keys ----------------------
MethodArgumentNotValidException.Size.employee.name
MethodArgumentNotValidException.Size.name
MethodArgumentNotValidException.Size.java.lang.String
MethodArgumentNotValidException.Size
--------------------------------------------------------
```

Renaming a class, controller method, parameter, field, or database constraint changes the derived
keys. Update the bundle when that happens.

### Placeholders

Messages are `java.text.MessageFormat` patterns. Write a literal single quote as `''`. The arguments
depend on the error:

| Error | Arguments |
| --- | --- |
| A problem you throw | The values passed to `detailArgs(...)` |
| `405` method not allowed | `{0}` requested method, `{1}` allowed methods |
| `415` media type not supported | `{0}` request content type, `{1}` supported media types |
| `406` not acceptable | `{0}` media types that can be produced |
| Upload or request size limit | `{0}` the limit, readable: `512 B`, `1.5 KB`, `10 MB` |
| `@Valid` body field, or controller method parameter | `{0}` field or parameter, `{1}` rejected value, `{2}` field or parameter name, `{3}` onwards the constraint attributes in alphabetical order. For `@Size(min = 3, max = 10)`: `{3}` is `10`, `{4}` is `3` |
| `@Valid` body object (class-level constraint) | `{0}` object name, `{1}` object name, `{2}` onwards the constraint attributes |
| `ConstraintViolationException` | `{0}` property path, `{1}` invalid value |

### Languages

Messages are resolved for the request's locale, which Spring Boot reads from `Accept-Language`. A
request with `Accept-Language: fr` reads `i18n/errors_fr.properties` first and falls back to
`i18n/errors.properties`.

### Messages shipped with the library

[`i18n/problems.properties`](src/main/resources/i18n/problems.properties) contains:

```properties
detail.ConcurrencyFailureException=Conflicted with another concurrent update, please retry
detail.UnsupportedOperationException=The requested operation is not supported yet
detail.MaxUploadSizeExceededException=Upload file size exceeded the maximum allowed limit: {0}
```

## Throwing problems from your code

Throw a [`ThrowableProblem`](src/main/java/com/neolama/common/exception/core/ThrowableProblem.java),
an unchecked exception that carries its response, built with
[`Problems`](src/main/java/com/neolama/common/exception/core/Problems.java). There are three
starting points:

| Start with | Use when |
| --- | --- |
| `Problems.of(ErrorType)` | The error is one of your application's known errors (recommended) |
| `Problems.of("error.key")` | A one-off error |
| `Problems.of(problem, others...)` or `Problems.of(collection)` | Several errors in one response |

### Error types

Declare your errors once, as an enum implementing
[`ErrorType`](src/main/java/com/neolama/common/exception/core/ErrorType.java). It gives each error a
key, a Java default for `detail`, and a default status. Messages in your bundle still win over both.

```java
public enum AppErrors implements ErrorType {
  EMPLOYEE_NOT_FOUND("employee.not.found", "Employee {0} not found", HttpStatus.NOT_FOUND),
  REMOTE_HOST_NOT_AVAILABLE(
      "remote.host.not.available", "Remote host {0} is not available", HttpStatus.SERVICE_UNAVAILABLE);

  private final String errorKey;
  private final String defaultDetail;
  private final HttpStatus status;

  AppErrors(final String errorKey, final String defaultDetail, final HttpStatus status) {
    this.errorKey = errorKey;
    this.defaultDetail = defaultDetail;
    this.status = status;
  }

  @Override
  public String getErrorKey() {
    return this.errorKey;
  }

  @Override
  public String getDefaultDetail() {
    return this.defaultDetail;
  }

  @Override
  public HttpStatus getStatus() {
    return this.status;
  }
}
```

```java
throw Problems.of(AppErrors.EMPLOYEE_NOT_FOUND).detailArgs(id).throwAble();

throw Problems.of(AppErrors.REMOTE_HOST_NOT_AVAILABLE).detailArgs(host).cause(exception).throwAble();
```

For a single error you can also use `DefaultProblem.of(errorKey, defaultDetail, status)` instead of
an enum.

### Error keys and options

```java
throw Problems.of("order.not.placeable")
    .defaultDetail("Order {0} cannot be placed") // used when detail.order.not.placeable is missing
    .detailArgs(7)                              // fills {0}
    .parameter("orderId", 7)                    // extra top-level field in the response
    .throwAble(HttpStatus.BAD_REQUEST);
```

`parameter(...)` and `parameters(map)` add fields to the response. The names `type`, `status`,
`instance`, `method`, `code`, `title`, `detail`, `timestamp`, `violations`, and `errors` are
reserved. `cause(...)` keeps the original exception for the server log.

### Status

`throwAble(status)` always uses the status you pass. `throwAble()` resolves it:

1. `status.{errorKey}` from your bundle, if set;
2. otherwise `ErrorType.getStatus()`, if you started from an error type and it is not `null`;
3. otherwise `500`.

A collection of problems has no single key and uses `500` unless you pass a status.

### Several errors in one response

```java
throw Problems.of(
        Problem.of("row-3", "Invalid row", "Row 3: name is missing"),
        Problem.of("row-7", "Invalid row", "Row 7: date of birth is in the future"))
    .throwAble(HttpStatus.UNPROCESSABLE_CONTENT);
```

The problems are listed under `errors`. `Problem.of("error.key")` resolves its `code`, `message`, and
`details` from `code.`, `title.`, and `detail.` messages for that key.

### Do not reuse a thrown problem

Build a new `ThrowableProblem` for every failure. Do not keep one in a field and throw it again: the
response writes the request path and method onto the instance, and its timestamp is the time it was
created. Reuse the `ErrorType` instead.

### Your own exception classes

Exceptions without a dedicated advice are handled too. Their error key is the simple class name, and
their status comes from `status.{SimpleName}`, then `@ResponseStatus` on the class or its cause, then
`500`:

```java
@ResponseStatus(HttpStatus.CONFLICT)
public class OrderLockedException extends RuntimeException {
  public OrderLockedException(String message) {
    super(message);
  }
}
```

Spring's `ResponseStatusException` also works: its status is used, and its reason becomes the detail.

## Handled exceptions

The advices live in [`advice`](src/main/java/com/neolama/common/exception/advice). Every status can
be changed with `status.{errorKey}`. "Spring's message" means the exception's own message, which is
safe to show for these client errors.

### General

| Exception | Status | Error key | Default detail |
| --- | --- | --- | --- |
| `ThrowableProblem` | As built | As built | As built |
| `UnsupportedOperationException` | `501` | `UnsupportedOperationException` | `The requested operation is not supported yet` (shipped bundle) |
| `ResponseStatusException` | Its status; an unknown code becomes `400` or `500` | `ResponseStatusException` | Its reason, or the status reason phrase |
| Any other exception | `@ResponseStatus`, the status of a Spring `ErrorResponse` (such as `503` for `AsyncRequestTimeoutException`), or `500` | Simple class name | 5xx: `An unexpected error occurred`. 4xx: the exception message |

### HTTP

| Exception | Status | Error key | Default detail |
| --- | --- | --- | --- |
| `HttpRequestMethodNotSupportedException`, `MethodNotAllowedException` | `405` + `Allow` | Simple class name | `Requested Method: {0} not allowed, allowed methods are: {1}` |
| `HttpMediaTypeNotSupportedException`, `UnsupportedMediaTypeStatusException` | `415` + `Accept` | Simple class name | `Media Type: {0} Not Supported, Supported Media Types are: {1}` |
| `HttpMediaTypeNotAcceptableException`, `NotAcceptableStatusException` | `406` + `Accept` | Simple class name | `Media Type Not Acceptable, except: {0}` |

### Request routing

| Exception | Status | Error key | Default detail |
| --- | --- | --- | --- |
| `NoResourceFoundException` (unknown path) | `404` | `NoResourceFoundException` | `No resource found for GET /api/unknown` |
| `NoHandlerFoundException` (unknown path, with `spring.web.resources.add-mappings=false`) | `404` | `NoHandlerFoundException` | Spring's message |
| `MissingServletRequestParameterException` | `400` | `MissingServletRequestParameterException.{parameter}` | Spring's message |
| `MissingRequestHeaderException` | `400` | `MissingRequestHeaderException.{Controller}.{method}.{header}` | Spring's message |
| `MissingServletRequestPartException` | `400` | `MissingServletRequestPartException.{part}` | Spring's message |
| Other `ServletRequestBindingException`, such as a missing cookie | `400`, or the exception's own status | Simple class name | Spring's message |

### Request body and uploads

| Exception | Status | Error key | Default detail |
| --- | --- | --- | --- |
| `HttpMessageNotReadableException` (malformed JSON) | `400` | `HttpMessageNotReadableException` | `Malformed request body` |
| Invalid value in a JSON body (Jackson `InvalidFormatException`) | `400` | [Candidate keys](#invalid-json-values) | `Malformed request body` |
| `MaxUploadSizeExceededException` | `400` | `MaxUploadSizeExceededException` | `Upload file size exceeded the maximum allowed limit: {0}` |
| `DataBufferLimitException` | `400` | `DataBufferLimitException` | `Request data exceeded the allowed limit: {0}` |
| Other `MultipartException` | `400` | `MultipartException` | Spring's message |

#### Invalid JSON values

For a value that cannot be read, such as `"dateOfBirth": "31-12-1990"` for a `LocalDate` field
`dateOfBirth` in `com.example.Employee`, these keys are tried in order:

1. `InvalidFormatException.com.example.Employee.dateOfBirth`
2. `InvalidFormatException.Employee.dateOfBirth`
3. `InvalidFormatException.LocalDate.dateOfBirth`
4. `InvalidFormatException.dateOfBirth`
5. `InvalidFormatException`

Nested objects add their class names in order, such as `InvalidFormatException.Employee.Address.zip`.
A list element reports under the list field. The default code is `bad-request`.

### Validation, database, and security

See [Validation errors](#validation-errors), [Database errors](#database-errors), and
[Spring Security](#spring-security).

## Validation errors

Validation responses have status `400`, the detail `Request validation failed`, and a `violations`
list with one entry per failure. Each entry has:

* `code`: from `code.` messages, by default `400`;
* `message`: from `message.` messages, by default the constraint message such as
  `must not be blank`;
* `details`: from `detail.` messages, by default `Invalid value for property {field}` or
  `Invalid value for parameter {name}`.

| Where the constraint is | Exception | Message keys for each violation |
| --- | --- | --- |
| A `@Valid @RequestBody` or `@ModelAttribute` field | `MethodArgumentNotValidException` | `{prefix}.MethodArgumentNotValidException.{code}` for each Spring field-error code, such as `Size.employee.name`, `Size.name`, `Size.java.lang.String`, `Size` |
| A binding error you raise | `BindException` | `{prefix}.BindException.{code}` as above |
| A controller method parameter, such as `@RequestParam @Min(0) int page` | `HandlerMethodValidationException` | `{prefix}.HandlerMethodValidationException.{code}`, such as `Min.page`, `Min.int`, `Min` |
| A method of a `@Validated` bean | `ConstraintViolationException` | `{prefix}.ConstraintViolationException.{propertyPath}`, such as `list.page` |

`{prefix}` is `code`, `message`, or `detail`. For example:

```properties
message.MethodArgumentNotValidException.Size.employee.name=Name must be {4} to {3} characters long
detail.HandlerMethodValidationException.Min.page=Page must be 0 or more, got {1}
```

The [TRACE log](#which-error-key) lists the exact keys of every violation.

Values that cannot be converted to the parameter's type, such as `?joinedAfter=yesterday` for a
`LocalDate`, return `400` with `Invalid value for parameter joinedAfter`
(`MethodArgumentTypeMismatchException`) or `Invalid value for property ...` (`TypeMismatchException`).

## Database errors

Needs `spring-tx` on the classpath. Spring translates driver errors into
`DataIntegrityViolationException` or its subclass `DuplicateKeyException`; both return:

* status `500`, unless you configure one;
* error key `{Exception}.{constraint}` when the constraint is known, otherwise the exception name
  alone;
* detail `The request conflicts with existing data`. The database's own message, which can contain
  data values, is never sent to clients.

| Database | Constraint name | Example error key |
| --- | --- | --- |
| PostgreSQL (JDBC driver) | The constraint the server reports | `DuplicateKeyException.employees_name_key` |
| MongoDB | `{collection}.{index}` from a duplicate-key error | `DuplicateKeyException.states.name_1` |
| Anything else, or no constraint (such as a NOT NULL violation) | None | `DataIntegrityViolationException` |

```properties
status.DuplicateKeyException.employees_name_key=409
code.DuplicateKeyException.employees_name_key=employee-name-taken
detail.DuplicateKeyException.employees_name_key=An employee with this name already exists

# Covers every other data-integrity failure
detail.DataIntegrityViolationException=The data violates a database rule
```

## Spring Security

With `spring-boot-starter-security` on the classpath:

| Exception | Status | Default detail |
| --- | --- | --- |
| `AuthenticationException` and its subclasses, such as `BadCredentialsException` and `InsufficientAuthenticationException` | `401` | Spring's message |
| `UsernameNotFoundException` | `401` | `Bad credentials`, so the response does not reveal whether the user exists |
| `AccessDeniedException` and its subclasses, such as `AuthorizationDeniedException` and CSRF failures | `403` | Spring's message |

The advices handle these exceptions when they are thrown inside a controller. **Failures in the
security filter chain**, such as missing credentials or a missing role, happen before any controller.
To return problems for them too, set the library's entry point and access-denied handler on your
filter chain. The library provides both as beans:

```java
@Configuration
class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final AuthenticationEntryPoint authenticationEntryPoint,
      final AccessDeniedHandler accessDeniedHandler) {
    return http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .build();
  }
}
```

If your application defines its own `AuthenticationEntryPoint` or `AccessDeniedHandler` bean, the
library does not create its own of that type.

## Configuration reference

### Library properties

| Property | Default | Meaning |
| --- | --- | --- |
| `problem.type-url` | not set | Base URL of a page that documents your error codes. When set, `type` becomes `{type-url}#{code}`, such as `https://example.com/problems#employee-not-found`. Must be an absolute URI; the application fails to start otherwise. |
| `problem.dao-advice-enabled` | `true` | Set to `false` to turn off [database error](#database-errors) handling. The fallback then handles those exceptions. |
| `problem.security-advice-enabled` | `true` | Set to `false` to turn off [Spring Security](#spring-security) handling and the entry point and access-denied handler beans. |

### Spring properties that matter

| Property | Set to | Why |
| --- | --- | --- |
| `spring.messages.basename` | `i18n/errors,i18n/problems` (your bundles first) | Without it no message bundle is used. |
| `spring.mvc.problemdetails.enabled` | `false` (the default) | `true` makes Spring Boot's own handler answer instead of this library: no `code`, no `method`, no messages. |
| `spring.web.resources.add-mappings` | Leave as is | With static resource mapping turned off, unknown paths raise `NoHandlerFoundException` instead of `NoResourceFoundException`. Both return `404`. |

Spring Boot's error page (`ErrorMvcAutoConfiguration`) does not need to be turned off. It never
replaces a problem response.

### Auto-configurations

From [`AutoConfiguration.imports`](src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports).
All of them load only in servlet web applications.

| Auto-configuration | Provides | Extra condition |
| --- | --- | --- |
| [`ProblemConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/ProblemConfiguration.java) | Message lookups and the Jackson module for status and method values | |
| [`WebExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebExceptionHandler.java) | General, HTTP, routing, body, and validation handling | |
| [`WebDaoExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebDaoExceptionHandler.java) | Database error handling | `spring-tx` present, `problem.dao-advice-enabled` not `false` |
| [`DatabaseConstraintResolverConfiguration`](src/main/java/com/neolama/common/exception/autoconfigure/DatabaseConstraintResolverConfiguration.java) | PostgreSQL and MongoDB constraint name resolvers | As above, plus the driver |
| [`WebSecurityExceptionHandler`](src/main/java/com/neolama/common/exception/autoconfigure/WebSecurityExceptionHandler.java) | Security handling, entry point, access-denied handler | Spring Security present, `problem.security-advice-enabled` not `false` |

## Customizing

**Change the wording or status:** use messages ([Messages and error keys](#messages-and-error-keys)).
That covers most needs.

**Handle an exception differently:** declare your own `@RestControllerAdvice` with an
`@ExceptionHandler` for it. Your advice is consulted before the library's, so it wins for the
exceptions it handles. No `@Order` is needed. An advice of yours that handles `Exception` or
`Throwable` takes over everything.

**Change one part of an existing handler:** implement the library's advice interface and override the
method you want. For example, to show the field name in every body-validation violation:

```java
@RestControllerAdvice
class EmployeeValidationAdvice implements MethodArgumentNotValidAdvice {

  @Override
  public Problem handleFieldError(
      final FieldError fieldError, final Throwable exception, final HttpStatus status) {
    final Problem problem =
        MethodArgumentNotValidAdvice.super.handleFieldError(fieldError, exception, status);
    problem.setDetails(fieldError.getField() + ": " + problem.getMessage());
    return problem;
  }
}
```

**Add a handler for your own exception** when the error key or message arguments must come from the
exception. Implement [`Exceptional`](src/main/java/com/neolama/common/exception/advice/Exceptional.java),
which provides status resolution, message lookup, logging, and the request fields:

```java
@RestControllerAdvice
class PaymentAdvice implements Exceptional {

  @ExceptionHandler
  ProblemDetails handlePaymentDeclined(
      final PaymentDeclinedException exception, final NativeWebRequest request) {
    final String errorKey = "PaymentDeclinedException." + exception.getReasonCode();
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.PAYMENT_REQUIRED);
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, "Payment was declined: {0}", exception.getReasonCode());
    return toResponse(problemDetails, request, exception);
  }
}
```

If the error key can stay the simple class name and the status is fixed, you need no handler:
annotate the exception with `@ResponseStatus` and add messages.

Your own advices do not set `type` from `problem.type-url`. Override `typeUrl()` from `Exceptional` if
they should.

## Logging

Everything is logged by `com.neolama.common.exception.advice.Exceptional`:

| Status | Level | What |
| --- | --- | --- |
| 5xx | ERROR | One line plus the stack trace |
| 4xx | WARN | One line, no stack trace |
| 4xx | DEBUG | The stack trace, when DEBUG is enabled |
| any | TRACE | The [error keys](#which-error-key) that are looked up |

```text
ERROR ... : GET /api/reports — 500 illegal-state-exception: IllegalStateException: Connection to report-db.internal:5432 refused
java.lang.IllegalStateException: Connection to report-db.internal:5432 refused
	at ...
WARN  ... : GET /api/unknown — 404 no-resource-found-exception: NoResourceFoundException: No static resource api/unknown for request '/api/unknown'.
```

The log line keeps the exception message that the 5xx response hides.

## Testing your application

`@SpringBootTest` and `@WebMvcTest` both apply the library with no extra setup, so exceptions in your
controller tests become the same problem responses as in the running application:

```java
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EmployeeService employeeService;

  @Test
  void unknownEmployeeIs404() throws Exception {
    given(this.employeeService.find(42)).willThrow(
        Problems.of(AppErrors.EMPLOYEE_NOT_FOUND).detailArgs(42).throwAble());

    this.mockMvc.perform(get("/api/employees/42"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("employee-not-found"));
  }
}
```

The library registers its auto-configurations for `@WebMvcTest` in
[`AutoConfigureWebMvc.imports`](src/main/resources/META-INF/spring/org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc.imports).
The usual conditions still apply: database handling needs `spring-tx`, security handling needs Spring
Security, and the `problem.*` properties turn them off. To leave the library out of a slice test, use
`@WebMvcTest(excludeAutoConfiguration = WebExceptionHandler.class)`.

`Problems` also works in plain unit tests without Spring: problems then get the Java defaults, since
no message bundle is loaded.

## Example responses

Real responses from a demo application with this message bundle:

```properties
message.MethodArgumentNotValidException.Size.employee.name=Name must be {4} to {3} characters long
detail.HandlerMethodValidationException.Min.page=Page must be 0 or more, got {1}
code.DuplicateKeyException.employees_name_key=employee-name-taken
status.DuplicateKeyException.employees_name_key=409
detail.DuplicateKeyException.employees_name_key=An employee with this name already exists
status.DuplicateKeyException.states.name_1=409
detail.DuplicateKeyException.states.name_1=A state with this name already exists
detail.InvalidFormatException.dateOfBirth=Date of birth must be a date like 1990-12-31
code.employee.not.found=employee-not-found
detail.invalid.order=Order {0} has no items
```

### Invalid request body (`@Valid`)

`POST /api/employees` with `{"name":"Al","designation":"Senior","address":{"state":""}}`:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees",
  "method": "POST",
  "code": "method-argument-not-valid-exception",
  "title": "Bad Request",
  "detail": "Request validation failed",
  "timestamp": "2026-10-25T14:10:42.619831Z",
  "violations": [
    {
      "code": "400",
      "message": "must not be blank",
      "details": "Invalid value for property address.state"
    },
    {
      "code": "400",
      "message": "size must be between 2 and 5",
      "details": "Invalid value for property designation"
    },
    {
      "code": "400",
      "message": "Name must be 3 to 10 characters long",
      "details": "Invalid value for property name"
    }
  ]
}
```

### Invalid query parameter

`GET /api/employees?page=-1` with `@RequestParam @Min(0) int page`:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees",
  "method": "GET",
  "code": "handler-method-validation-exception",
  "title": "Bad Request",
  "detail": "Request validation failed",
  "timestamp": "2026-10-25T14:13:45.239681Z",
  "violations": [
    {
      "code": "400",
      "message": "must be greater than or equal to 0",
      "details": "Page must be 0 or more, got -1"
    }
  ]
}
```

### Missing query parameter

`GET /api/employees` without `page`:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees",
  "method": "GET",
  "code": "bad-request",
  "title": "Bad Request",
  "detail": "Required request parameter 'page' for method parameter type int is not present",
  "timestamp": "2026-10-25T14:10:42.739644Z"
}
```

### Parameter of the wrong type

`GET /api/employees/search?joinedAfter=yesterday` with a `LocalDate` parameter:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees/search",
  "method": "GET",
  "code": "bad-request",
  "title": "Bad Request",
  "detail": "Invalid value for parameter joinedAfter",
  "timestamp": "2026-10-25T14:10:42.667781Z"
}
```

### Invalid value in the JSON body

`POST /api/employees` with `{"name":"Alice","dateOfBirth":"31-12-1990"}`:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees",
  "method": "POST",
  "code": "bad-request",
  "title": "Bad Request",
  "detail": "Date of birth must be a date like 1990-12-31",
  "timestamp": "2026-10-25T14:10:42.674176Z"
}
```

### Malformed JSON

`POST /api/employees` with `{"name": Alice}`:

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/employees",
  "method": "POST",
  "code": "http-message-not-readable-exception",
  "title": "Bad Request",
  "detail": "Malformed request body",
  "timestamp": "2026-10-25T14:10:42.679337Z"
}
```

### Error type thrown from code

`throw Problems.of(AppErrors.EMPLOYEE_NOT_FOUND).detailArgs(42).throwAble();`

```json
{
  "type": "about:blank",
  "status": 404,
  "instance": "/api/employees/42",
  "method": "GET",
  "code": "employee-not-found",
  "title": "Not Found",
  "detail": "Employee 42 not found",
  "timestamp": "2026-10-25T14:10:42.684674Z"
}
```

### Problem with an extra field

`throw Problems.of("invalid.order").defaultDetail("Order {0} cannot be placed").detailArgs(7).parameter("orderId", 7).throwAble(HttpStatus.BAD_REQUEST);`

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/orders",
  "method": "POST",
  "code": "bad-request",
  "title": "Bad Request",
  "detail": "Order 7 has no items",
  "timestamp": "2026-10-25T14:10:42.711462Z",
  "orderId": 7
}
```

### Several errors

The `Problems.of(...)` example from [Several errors in one response](#several-errors-in-one-response):

```json
{
  "type": "about:blank",
  "status": 422,
  "instance": "/api/imports",
  "method": "POST",
  "code": "422",
  "title": "Unprocessable Content",
  "detail": "Unprocessable Content",
  "timestamp": "2026-10-25T14:10:42.715535Z",
  "errors": [
    {
      "code": "row-3",
      "message": "Invalid row",
      "details": "Row 3: name is missing"
    },
    {
      "code": "row-7",
      "message": "Invalid row",
      "details": "Row 7: date of birth is in the future"
    }
  ]
}
```

### PostgreSQL unique constraint

`DuplicateKeyException` caused by `duplicate key value violates unique constraint "employees_name_key"`:

```json
{
  "type": "about:blank",
  "status": 409,
  "instance": "/api/employees/import",
  "method": "POST",
  "code": "employee-name-taken",
  "title": "Conflict",
  "detail": "An employee with this name already exists",
  "timestamp": "2026-10-25T14:10:42.692239Z"
}
```

### MongoDB unique index

`DuplicateKeyException` caused by `E11000 duplicate key error collection: hr.states index: name_1`:

```json
{
  "type": "about:blank",
  "status": 409,
  "instance": "/api/states",
  "method": "POST",
  "code": "conflict",
  "title": "Conflict",
  "detail": "A state with this name already exists",
  "timestamp": "2026-10-25T14:10:42.695805Z"
}
```

### Upload too large

```json
{
  "type": "about:blank",
  "status": 400,
  "instance": "/api/documents",
  "method": "POST",
  "code": "max-upload-size-exceeded-exception",
  "title": "Bad Request",
  "detail": "Upload file size exceeded the maximum allowed limit: 10 MB",
  "timestamp": "2026-10-25T14:10:42.700716Z"
}
```

### Unhandled exception

`IllegalStateException("Connection to report-db.internal:5432 refused")`. The message is logged, not
returned:

```json
{
  "type": "about:blank",
  "status": 500,
  "instance": "/api/reports",
  "method": "GET",
  "code": "illegal-state-exception",
  "title": "Internal Server Error",
  "detail": "An unexpected error occurred",
  "timestamp": "2026-10-25T14:10:42.705216Z"
}
```

### Unknown path

```json
{
  "type": "about:blank",
  "status": 404,
  "instance": "/api/unknown",
  "method": "GET",
  "code": "no-resource-found-exception",
  "title": "Not Found",
  "detail": "No resource found for GET /api/unknown",
  "timestamp": "2026-10-25T14:10:42.732491Z"
}
```

### Method not allowed

`DELETE /api/employees`; the response has `Allow: GET,POST`:

```json
{
  "type": "about:blank",
  "status": 405,
  "instance": "/api/employees",
  "method": "DELETE",
  "code": "http-request-method-not-supported-exception",
  "title": "Method Not Allowed",
  "detail": "Requested Method: DELETE not allowed, allowed methods are: GET,POST",
  "timestamp": "2026-10-25T14:10:42.724262Z"
}
```

### Unsupported content type

`POST /api/employees` with `Content-Type: application/xml`; the response has `Accept: application/json`:

```json
{
  "type": "about:blank",
  "status": 415,
  "instance": "/api/employees",
  "method": "POST",
  "code": "http-media-type-not-supported-exception",
  "title": "Unsupported Media Type",
  "detail": "Media Type: application/xml Not Supported, Supported Media Types are: application/json",
  "timestamp": "2026-10-25T14:10:42.720736Z"
}
```

### Not acceptable

`GET /api/employees?page=0` with `Accept: application/xml`; the response has `Accept: application/json`:

```json
{
  "type": "about:blank",
  "status": 406,
  "instance": "/api/employees",
  "method": "GET",
  "code": "http-media-type-not-acceptable-exception",
  "title": "Not Acceptable",
  "detail": "Media Type Not Acceptable, except: application/json",
  "timestamp": "2026-10-25T14:10:42.727071Z"
}
```

### Missing credentials

`GET /api/secure/profile` without credentials, through the security filter chain:

```json
{
  "type": "about:blank",
  "status": 401,
  "instance": "/api/secure/profile",
  "method": "GET",
  "code": "insufficient-authentication-exception",
  "title": "Unauthorized",
  "detail": "Full authentication is required to access this resource",
  "timestamp": "2026-10-25T14:10:42.7363Z"
}
```

## Limitations

* **Servlet applications only.** In reactive and non-web applications the library registers nothing.
* **Databases:** constraint names are read for PostgreSQL through its JDBC driver and for MongoDB.
  Other databases, and PostgreSQL through R2DBC, get the plain exception-name key.
* **Errors outside Spring MVC**, such as a request the servlet container rejects before it reaches
  your application, are not turned into problems.
* **One message source per JVM:** lookups use the message source of the last Spring context that
  started. A test suite that keeps several contexts with different bundles in one JVM should run them
  in separate forks.

## License

[MIT License](https://opensource.org/licenses/MIT)

## Author

[**Rajveer Singh**](https://www.linkedin.com/in/rajveer-singh-589b3950/). For issues or support,
email raj14.1984@gmail.com. Please give the project a :star: if you find it helpful.

## Credits

Inspired by the [Zalando Problem libraries](https://github.com/zalando/problem-spring-web).
