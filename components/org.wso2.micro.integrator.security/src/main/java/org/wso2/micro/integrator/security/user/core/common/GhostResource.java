package org.wso2.micro.integrator.security.user.core.common;

import java.io.Serializable;

public class GhostResource <T> implements Serializable{
    private static final long serialVersionUID = -2953483852512559586L;
    private transient T resource;

    public GhostResource(T resource) {
        this.resource = resource;
    }

    public T getResource() {
        return this.resource;
    }

    public void setResource(T resource) {
        this.resource = resource;
    }
}
