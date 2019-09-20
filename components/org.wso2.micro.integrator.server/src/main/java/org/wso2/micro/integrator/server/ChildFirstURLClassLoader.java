/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.server;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class ChildFirstURLClassLoader extends URLClassLoader {

    public ChildFirstURLClassLoader(URL[] urls) {
        super(urls);
    }

    public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public URL getResource(String name) {
        URL resource = findResource(name);
        if (resource == null) {
            ClassLoader parent = getParent();
            if (parent != null) {
                resource = parent.getResource(name);
            }
        }
        return resource;
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = findLoadedClass(name);
        if (clazz == null) {
            try {
                clazz = findClass(name);
            } catch (ClassNotFoundException e) {
                ClassLoader parent = getParent();
                if (parent != null) {
                    clazz = parent.loadClass(name);
                } else {
                    clazz = getSystemClassLoader().loadClass(name);
                }
            }
        }

        if (resolve) {
            resolveClass(clazz);
        }

        return clazz;
    }

    // we want to ensure that the framework has AllPermissions
    protected PermissionCollection getPermissions(CodeSource codesource) {
        return allPermissions;
    }

    static final PermissionCollection allPermissions = new PermissionCollection() {
        private static final long serialVersionUID = 482874725021998286L;
        // The AllPermission permission
        Permission allPermission = new AllPermission();

        // A simple PermissionCollection that only has AllPermission
        public void add(Permission permission) {
            // do nothing
        }

        public boolean implies(Permission permission) {
            return true;
        }

        public Enumeration<Permission> elements() {
            return new Enumeration<Permission>() {
                int cur = 0;

                public boolean hasMoreElements() {
                    return cur < 1;
                }

                public Permission nextElement() {
                    if (cur == 0) {
                        cur = 1;
                        return allPermission;
                    }
                    throw new NoSuchElementException();
                }
            };
        }
    };
}
