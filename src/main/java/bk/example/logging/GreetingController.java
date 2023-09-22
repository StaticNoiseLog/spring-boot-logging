package bk.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    final Logger loggerGreetingController = LoggerFactory.getLogger(getClass()); // logger name "bk.example.logging.GreetingController"

    private static final String TEMPLATE = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        loggerGreetingController.info(">>> INFO {}", name);
        loggerGreetingController.debug(">>>> DEBUG {}", name);
        loggerGreetingController.info("Hello {}, the temperature is {} degrees Celsius.", name, 24);
        return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name));
    }

    /**
     * <a href="http://localhost:8080/hierarchy">experiment with logger hierarchy</a>
     */
    @GetMapping("/hierarchy")
    public void hierarchy() {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        // three explicitly named loggers
        Logger loggerOne = LoggerFactory.getLogger("one");
        Logger loggerTwo = LoggerFactory.getLogger("one.two");
        Logger loggerThree = LoggerFactory.getLogger("one.two.three");

        rootLogger.warn("logged with rootLogger");
        loggerOne.warn("logged with loggerOne (loggerOne additivity is true, therefore logged by rootLogger, too)");
        loggerTwo.warn("logged with loggerTwo (loggerTwo additivity is false, therefore no propagation of this message)");
        loggerThree.warn("logged with loggerThree (loggerThree additivity is true, therefore logged by loggerTwo, too)");
    }

    /**
     * <a href="http://localhost:8080/loglevels">different log levels</a>
     */
    @GetMapping("/loglevels")
    public Greeting loglevels() {
        loggerGreetingController.error("loggerGreetingController level error");
        loggerGreetingController.warn("loggerGreetingController level warn");
        loggerGreetingController.info("loggerGreetingController level info");
        loggerGreetingController.debug("loggerGreetingController level debug");
        loggerGreetingController.trace("loggerGreetingController level trace");
        return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, "/loglevels, loggerGreetingController!"));
    }

    /**
     * <a href="http://localhost:8080/npe">NullPointerException</a>
     */
    @GetMapping("/npe")
    public Greeting npe() {
        throw new NullPointerException("null is null");
    }

    /**
     * <a href="http://localhost:8080/explode">explode with a Java Error (OutOfMemoryError)</a>
     */
    @GetMapping("/explode")
    public void explode() {
        loggerGreetingController.warn("I am about to explode...");
        List<long[]> list = new ArrayList<>();
        while (true) {
            loggerGreetingController.warn("GROWING...");
            long[] l = new long[Integer.MAX_VALUE];
            list.add(l);
        }
    }
}