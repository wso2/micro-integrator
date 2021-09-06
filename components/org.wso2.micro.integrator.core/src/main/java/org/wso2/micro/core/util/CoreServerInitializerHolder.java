/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.core.util;

import org.wso2.micro.integrator.core.internal.CoreServerInitializer;

public class CoreServerInitializerHolder {
    private CoreServerInitializer coreServerInitializer;
    private static CoreServerInitializerHolder instance = new CoreServerInitializerHolder();

    private CoreServerInitializerHolder() {
    }

    public static void setInstance(CoreServerInitializerHolder instance) {
        CoreServerInitializerHolder.instance = instance;
    }

    public static CoreServerInitializerHolder getInstance() {
        return instance;
    }

    public void setCoreServerInitializer(CoreServerInitializer coreServerInitializer) {
        this.coreServerInitializer = coreServerInitializer;
    }

    public CoreServerInitializer getCoreServerInitializer() {
        return coreServerInitializer;
    }

    public void shutdown() {
        coreServerInitializer.shutdown();
    }

    public void shutdownGracefully() {
        coreServerInitializer.shutdownGracefully();
    }

    public void restart() {
        coreServerInitializer.restart(false);
    }

    public void restartGracefully() {
        coreServerInitializer.restart(true);
    }
}
