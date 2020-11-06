package bk.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * When using Spring Boot starters, Logback is used for logging by default.
 * The dependency is spring-jcl and already included.
 * <p>
 * Levels are: TRACE	DEBUG	INFO	WARN	ERROR	OFF
 * <p>
 * Default is INFO.
 * Change level on command line:
 * java -jar target/spring-boot-logging-0.0.1-SNAPSHOT.jar --trace
 * java -jar target/spring-boot-logging-0.0.1-SNAPSHOT.jar --debug
 * <p>
 * Spring Boot also gives us access to a more fine-grained log level setting via environment variables:
 * <p>
 * VM option:
 * -Dlogging.level.org.springframework=TRACE
 * ./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE
 * <p>
 * Permanently in application.properties:
 * logging.level.root=WARN
 * logging.level.com.baeldung=TRACE
 * <p>
 * Best permanently:
 * In a Logback configuration file, best called logback-spring.xml (anywhere in the classpath):
 * <logger name="org.springframework" level="INFO" />
 * <logger name="com.baeldung" level="INFO" />
 */
@RestController
public class GreetingController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        logger.info(">>> INFO right");
        logger.debug(">>>> DEBUG right");
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    /**
     * http://localhost:8080/hierarchy
     */
    @GetMapping("/hierarchy")
    public void hierarchy() {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger loggerOne = LoggerFactory.getLogger("one");
        Logger loggerTwo = LoggerFactory.getLogger("one.two");
        Logger loggerThree = LoggerFactory.getLogger("one.two.three");

        rootLogger.error("logged with rootLogger");
        loggerOne.error("logged with loggerOne");
        loggerTwo.error("logged with loggerTwo");
        loggerThree.error("logged with loggerThree");
    }

    /**
     * http://localhost:8080/loglevels
     */
    @GetMapping("/loglevels")
    public Greeting loglevels() {
        logger.error("wuff level error");
        logger.warn("wuff level warn");
        logger.info("wuff level info");
        logger.debug("wuff level debug");
        logger.trace("wuff level trace");
        return new Greeting(counter.incrementAndGet(), String.format(template, "/loglevels, wuff!"));
    }

    /**
     * http://localhost:8080/stacktrace
     */
    @GetMapping("/stacktrace")
    public Greeting stacktrace() {
        throw new NullPointerException("null is null");
    }

    /**
     * http://localhost:8080/explode
     */
    @GetMapping("/explode")
    public void explode() {
        logger.warn("I am about to explode...");
        List<long[]> list = new ArrayList<>();
        while (true) {
            logger.warn("GROWING...");
            long[] l = new long[Integer.MAX_VALUE];
            list.add(l);
        }
    }
}