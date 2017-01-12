package com.flipkart.flux.client.runtime;

/**
 * Interface to facilitate dynamic unloading of deployment units. Provides a hook for the client to release all
 * acquired resources like connections and threads.
 * A concrete implementation for this interface must be provided through guice, so that the flux runtime can acquire its
 * reference.
 *
 * @author gaurav.ashok
 */
public interface Stoppable {

    void stop();
}
