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

package org.wso2.micro.integrator.server.util;

public class JarInfo {

    private String name;
    private String path;
    private String md5SumValue;

    JarInfo(String name, String path) {
        this(name, path, null);
    }

    JarInfo(String name, String path, String md5SumValue) {
        this.name = name;
        this.path = path;
        this.md5SumValue = md5SumValue;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getMd5SumValue() {
        return md5SumValue;
    }

    public void setMd5SumValue(String md5SumValue) {
        this.md5SumValue = md5SumValue;
    }
}
