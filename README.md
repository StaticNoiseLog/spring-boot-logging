# Trying Out
<http://localhost:8080/greeting>

<http://localhost:8080/hierarchy>

# More on Logging with Spring Boot

## Basics
Spring Boot uses Logback for logging by default, and logs everything to the console.

## Levels
TRACE, DEBUG, INFO, WARN, ERROR, ALL or OFF

The special case-insensitive value INHERITED, or its synonym NULL, will force the
level of the logger to be inherited from higher up in the hierarchy.

If you don’t explicitly define a log level, the logger will inherit the level of its
closest ancestor.

## Setting Log Levels
Logging levels can be specified in different ways:
1. On the command line: `-Dlogging.level.com.baeldung=TRACE`
2. In application.properties/yml: `logging.level.com.baeldung=TRACE`
3. In logback-spring.xml: `<logger name="bk.example.logging" level="trace" ...`

If you define conflicting log levels for a package, using the different options, the lowest level
will be used.

## Live Changes of logback-spring.xml
With the `scan` and `scanPeriod` attribute logback will detect logging configuration changes on the fly.
Note that you must modify the *deployed logback-spring.xml file* for this to work. In this case that would
be ..\logging\build\resources\main\logback-spring.xml.

    <configuration scan="true" scanPeriod="1 seconds">

## Preconfigured Appenders
Spring ships two preconfigured appenders, one is named CONSOLE. But to use them you
have to include them in logback-spring.xml, and also defaults.xml (for CONSOLE_LOG_PATTERN and
FILE_LOG_PATTERN). In a small experiment in 2020 the output was not convincing, though.

    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
    <include resource="org/springframework/boot/logging/logback/file-appender.xml"/>
    
 # Cloud Foundry
 
One of the tenets of the twelve-factor manifesto:

*A twelve-factor app never concerns itself with routing or storage of its output
 stream. It should not attempt to write to or manage logfiles. Instead, each
 running process writes its event stream, unbuffered, to stdout. During local
 development, the developer will view this stream in the foreground of their
 terminal to observe the app’s behavior.*

## Loggregator

Cloud Foundry’s Loggregator component captures logs from the various components
and applications running on the platform, aggregates these logs, and gives users
full access to the data either via CLI or the management console.

Tail:

    cf logs YOUR-APP-NAME

Display all of the lines in the Loggregator buffer:

    cf logs YOUR-APP-NAME --recent

## Saving Logs Externally
Cloud Foundry can be configured to export the log data to an external logging
solution, typically the ELK stack (Elasticsearch, Logstash, Kibana). In a
production environment you want to do this because the regular log data is only
stored for a short time.

The basic steps are:

1. Make sure your app logs to the console.
2. Prepare a receiver for the log data (e.g. Logstash).
3. Create a user provided log drain service associated with the receiver.
4. Bind the log drain to your app.

See [this project's README](https://github.com/StaticNoiseLog/logstash-cloud-foundry-config/) for a full explanation
and example on how to get logs into the ELK stack on Cloud Foundry.