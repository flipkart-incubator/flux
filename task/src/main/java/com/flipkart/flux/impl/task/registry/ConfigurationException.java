package com.flipkart.flux.impl.task.registry;

/**
 * Raised to indicate any issues with configuration specification.
 * // TODO move this to a common package
 * @author yogesh.nachnani
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }
}
