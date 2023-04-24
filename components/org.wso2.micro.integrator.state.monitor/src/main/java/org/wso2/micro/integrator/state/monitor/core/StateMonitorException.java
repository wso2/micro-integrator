package org.wso2.micro.integrator.dashboard.state.monitor;

/**
 * State monitor exception class to handle exceptions.
 */
public class StateMonitorException extends Exception {

        public StateMonitorException(String error) {

            super(error);
        }

        public StateMonitorException(String error, Throwable cause) {

            super(error, cause);
        }
}
