package fourdsight.demo.urltrackerapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public interface LoggerSupport {
    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass().getName());
    }
}
