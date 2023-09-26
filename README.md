- [Local Endpoints](#local-endpoints)
  * [Actuator Info for Loggers](#actuator-info-for-loggers)
  * [Log Output](#log-output)
  * [Provoke Unhandled Exception/Error](#provoke-unhandled-exceptionerror)
- [Logging with Spring Boot](#logging-with-spring-boot)
  * [Spring Boot Default Logging](#spring-boot-default-logging)
  * [Java Code](#java-code)
  * [application.properties vs. logback-spring.xml](#applicationproperties-vs-logback-springxml)
    + [application.properties for Logging Levels](#applicationproperties-for-logging-levels)
    + [logback-spring.xml for Appenders, Patterns, etc.](#logback-springxml-for-appenders-patterns-etc)
  * [Recommendations](#recommendations)
    + [Configuration Example for a Cloud Microservice](#configuration-example-for-a-cloud-microservice)
  * [Logging Levels](#logging-levels)
    + [Setting Logging Levels](#setting-logging-levels)
  * [logback-spring.xml vs. logback.xml](#logback-springxml-vs-logbackxml)
- [Logback Appender](#logback-appender)
  * [Pattern](#pattern)
    + [Example](#example)
  * [Preconfigured Appenders](#preconfigured-appenders)
- [Cloud Foundry](#cloud-foundry)
  * [Loggregator](#loggregator)
  * [Saving Logs Externally](#saving-logs-externally)
  * [Configuration for JSON Logging](#configuration-for-json-logging)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


# Local Endpoints

## Actuator Info for Loggers

<http://localhost:8080/actuator/loggers>

## Log Output

<http://localhost:8080/greeting?name=Alf>

<http://localhost:8080/hierarchy>

<http://localhost:8080/loglevels>

## Provoke Unhandled Exception/Error

<http://localhost:8080/npe>

<http://localhost:8080/explode>

# Logging with Spring Boot

## Spring Boot Default Logging

By default, Spring Boot uses **[logback](https://logback.qos.ch/)** for logging and logs everything to the console.
Nowhere else, no file.

## Java Code

You use the SLF4J API (that's what logback implements).

    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error("Things failed miserably.", noRenderOutputException);

SLF4J comes with a [MessageFormatter](https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html) that uses its
own syntax but claims to be fast: *The formatting conventions are different from those of MessageFormat which ships with
the Java platform. This is justified by the fact that SLF4J's implementation is 10 times faster than that of
MessageFormat. This local performance difference is both measurable and significant in the larger context of the
complete logging processing chain.*

The SLF4J MessageFormatter is used when you work with the so-called *formatting anchor* `{}`:

     logger.info("Hello {}, the temperature is {} degrees Celsius.", name, 24);

## application.properties vs. logback-spring.xml

With Spring, avoid logback-specific configuration if possible. If you do need it (for JSON logging, special formatting,
etc.), use this filename and location: `..\src\main\resources\logback-spring.xml`

Prefer the Spring configuration mechanisms for logging (`application.properties`, etc.). This provides a level of
abstraction that isolates you from logback features that do not work with Spring Boot (like `scan="true"`), and it
leaves the door open for switching to another logging library in the future.

### application.properties for Logging Levels

Configure logging levels in `application.properties` (note the prefix `logging.level.` before the logger name):

```
logging.level.root=INFO
logging.level.com.example=DEBUG
```

Use profile-specific properties files like `application-dev.properties` or `application-cloud.properties` if you need
different logging levels depending on the active Spring profiles.

### logback-spring.xml for Appenders, Patterns, etc.

For the following requirements you need `logback-spring.xml`:

1. **Appenders:** Configuring logback appenders, such as `ConsoleAppender`, `FileAppender`.
2. **Log Formatting:** If you need custom log message formatting or layouts (`encoder`, `Pattern`).
3. **Logger Configuration:** Defining specific loggers, their names, and their associated appenders is generally done in
   XML.
4. **logback debugging:** `<configuration debug="true">`

**Once you have introduced a "logback-spring.xml" you must use it to configure a logger for all Spring profiles, or you
will not get any logging at all.** The default Spring Boot logging is gone.

## Recommendations ###

- Avoid `logback-spring.xml` if possible.
- If you need `logback-spring.xml`, keep it minimal and put it in `../src/main/resources`.
- Specify log levels in `application.properties` and do not set them in `logback-spring.xml`. Managing log levels in a
  single place makes it easier to understand what the current settings are. If you omit the level
  in `logback-spring.xml` (i.e. just `<root>` instead of `<root level="INFO">`) the default will be DEBUG. The log
  levels in `application.properties` override `logback-spring.xml`. You should ensure that you specify the correct log
  levels in `application.properties`. However, if you happen to overlook something, the default DEBUG setting will grab
  your attention.
- Remember that once you have a `logback-spring.xml` file, it must contain loggers for all situations. The Spring Boot
  default is not available anymore.
- Do not use the "scan" feature of logback (see section "logback-spring.xml vs. logback.xml" below).

### Configuration Example for a Cloud Microservice

The following configuration is for a microservice that is deployed to a cloud. With the Spring profile "cloud" it logs
in JSON format. For any other Spring profile it logs to the console in standard format.

Logging levels are set in `application.properties`:

```
logging.level.root=INFO
```
For JSON logging you have to add this dependency to build.gradle.kts:

    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.4")

To configure the JSON appender for the "cloud" Spring profile we need `logback-spring.xml` and because we have
introduced this file, we must also configure logging for all non-cloud environments. Logging levels are not set here
because we want to keep them centralized in `application.properties`.

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <springProfile name="!cloud">
        <!-- log to console only for all profiles that are not "cloud", i.e. "localhost" for example -->
        <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root>
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="cloud">
        <!-- log JSON, and ONLY that (don't bring in Spring Boot root logger) -->
        <appender name="json" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        </appender>
        <root>
            <appender-ref ref="json"/>
        </root>
    </springProfile>

</configuration>
```

## Logging Levels

These are the levels that are used for logging and configuration:

    TRACE < DEBUG < INFO <  WARN < ERROR

In addition, the value `OFF` can be used in the logging configuration. All six values mentioned work
in `logback-spring.xml` as well as in `application.properties`, i.e. they are "Spring compatible".

These are some behaviors I have observed with logback and Spring Boot 3:

- If you do not explicitly define a log level, the logger will inherit the level of its closest ancestor.
- In a logger hierarchy only the log level of the logger used in the log statement is relevant. The levels of ancestors
  have no influence. Say you have logger "one" with ancestor "ROOT". Logger "one" has level DEBUG (and
  additivity `true` so that the message travels up the hierarchy) and "ROOT" has level ERROR. The log statement
  is `loggerOne.warn("you were warned");`. You might expect that only "one" logs the line because "ROOT" has level
  ERROR. But filtering occurs at "one" exclusively and therefore *both* loggers, "one " *and* "ROOT", print the WARN log
  line.
- If you mistype and specify an invalid log level then DEBUG will be used, *not* the ancestor's level.

### Setting Logging Levels

Loggers are identified by matching their name. If you instantiate loggers in Java
with `LoggerFactory.getLogger(getClass())` you can conveniently use the Java package names for setting log levels in a
hierarchical way. You can also target a specific logger by including the class name after the package, as
in `logging.level.bk.example.logging.GreetingController=INFO`.

Logging levels can be specified in different ways:

1. In **logback-spring.xml**: `<logger name="bk.example.logging" level="TRACE" ...` (must not have `logging.level.`
   prefix!)
2. In **application.properties/yml**: `logging.level.bk.example.logging=TRACE` (`logging.level.` prefix required!)
3. On the **command line**: `-Dlogging.level.bk.example.logging=TRACE` (`logging.level.` prefix required!)

**Observation:** *The log level specified in application.properties overrides the level set in logback-spring.xml!*

## logback-spring.xml vs. logback.xml

The configuration file `../src/main/resources/logback-spring.xml` is loaded by Spring itself, which understands
the `springProfile` XML property.

If the file is called `logback.xml` then logback code will load the file and Spring profiles won't work! You will see
this WARN during startup (if you have logback debug enabled with `<configuration debug="true">`):

    19:47:42,885 |-WARN in ch.qos.logback.core.model.processor.ImplicitModelHandler - Ignoring unknown property [springProfile] in [ch.qos.logback.classic.LoggerContext]

**To ensure a clean configuration, it is important to let Spring be the only component responsible for loading the
logback configuration.** This involves using the name `logback-spring.xml` and avoiding the logback "scan" feature,
which is not supported by Spring.

    <configuration scan="true" scanPeriod="1 seconds">       Don't use scan with Spring Boot!

The above "scan" settings cause logback to detect changes in the configuration file while the application is running.
However, this feature is not supported by Spring Boot. Even if you use the Spring filename "logback-spring.xml", the
configuration is loaded by logback itself when the scanPeriod is over, not Spring. Given that you may also be employing
"springProfile" in your configuration (a keyword not supported by logback), the logging configuration will not reload
correctly, leading to irregular behavior of the running application.

# Logback Appender

## Pattern

The elements you can use in patterns are documented
on [the logback website](https://logback.qos.ch/manual/layouts.html).
If you want to know what `C%` does, you have to look for `C{` (which is short for `class{}`).

### Example

```
<appender name="Console1" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
        <Pattern>
            %red(Console1:) %black(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %yellow(%C{1}): %msg%n%throwable
        </Pattern>
    </encoder>
</appender>
```

Breakdown of the log pattern in the example above:

- `%red(Console1:)`: The text "Console1:" in red.
- `%black(%d{ISO8601})`: Date and time (in ISO8601 format) to be displayed in black.
- `%highlight(%-5level)`: Log level with highlighting, where the level text is left-aligned (`%-5level`) and the actual
  color would depend on your logback configuration. It's typically used to colorize the log level text based on
  severity (e.g., ERROR might be in red).
- `[%blue(%t)]`: The thread name (`%t`) in blue. Thread names help identify which threads are producing log entries in a
  multi-threaded application.
- `%yellow(%C{1})`: The class name (`%C{1}`) in yellow. The number `1` specifies the maximum length of the parts of the
  full class name (with the package). But as the rightmost part is never abbreviated, you get single letters for the
  packages and the unabbreviated class name when you use `%C{1}` (for example `b.e.l.GreetingController`). If the logger
  was created with `LoggerFactory.getLogger(getClass())` the class name may happen to be the same as the logger name.
  But `%C` is *not* the logger name, it is indeed the name of the class that printed the log line.
- `%msg`: This is where the log message (`%msg`) is displayed.
- `%n`: This is a newline.
- `%throwable`: This outputs any exception stack trace if present.

## Preconfigured Appenders

Spring ships two preconfigured appenders, CONSOLE and FILE, that can be imported with `include`.

Log only to the console:

    <springProfile name="localhost">
        <include resource="org/springframework/boot/logging/logback/defaults.xml" />
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

Log only to a file:

    <springProfile name="localhost">
        <property name="LOG_FILE" value="./log/myapp.log"/>
        <include resource="org/springframework/boot/logging/logback/defaults.xml" />
        <include resource="org/springframework/boot/logging/logback/file-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>

If you really want both appenders for logging to the console and a file simultaneously, you can include "base.xml".

# Cloud Foundry

One of the tenets of the twelve-factor manifesto:

*A twelve-factor app never concerns itself with routing or storage of its output
stream. It should not attempt to write to or manage logfiles. Instead, each
running process writes its event stream, unbuffered, to stdout. During local
development, the developer will view this stream in the foreground of their
terminal to observe the app’s behavior.*

## Loggregator

Cloud Foundry’s Loggregator component captures logs from the various components and applications running on the
platform, aggregates these logs, and gives users full access to the data either via CLI or the management console.

Tail:

    cf logs YOUR-APP-NAME

Display all the lines in the Loggregator buffer:

    cf logs YOUR-APP-NAME --recent

## Saving Logs Externally

Cloud Foundry can be configured to export the log data to an external logging solution, typically the ELK stack (
Elasticsearch, Logstash, Kibana). In a production environment you want to do this because the regular log data is only
stored for a short time.

The basic steps are:

1. Make sure your app logs to the console.
2. Prepare a receiver for the log data (e.g. Logstash).
3. Create a user provided log drain service associated with the receiver.
4. Bind the log drain to your app.

See [this project's README](https://github.com/StaticNoiseLog/logstash-cloud-foundry-config/) for a full explanation
and example on how to get logs into the ELK stack on Cloud Foundry.

## Configuration for JSON Logging

When running in Cloud Foundry, you may have to log in JSON format (no real solution for multiline problem with
Logstash).

See the section "Configuration Example for a Cloud Microservice" above for more details.