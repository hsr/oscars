package net.es.oscars.logging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OSCARSNetLoggerize {
    String moduleName();
    String serviceName() default "NoServiceName";
}
