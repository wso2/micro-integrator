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
package org.wso2.micro.integrator.bootstrap;

import java.io.File;
import java.net.MalformedURLException;

/**
 * The bootstrap class used for bootstrapping a checkin cleint.
 * <p/>
 * See http://frank.zinepal.com/embedded-tomcat-class-loading-trickery
 * See http://tomcat.apache.org/tomcat-7.0-doc/class-loader-howto.html
 */
public class CheckinClientBootstrap extends Bootstrap {

    private static final String LIB = "lib";
    private static final String REPOSITORY = "repository";
    private static final String COMPONENTS = "components";
    private static final String COMPONENT_PLUGINS_DIR_PATH = "components.repo";

    public static void main(String args[]) {
        new CheckinClientBootstrap().loadClass(args);
    }

    @Override
    protected void addClassPathEntries() throws MalformedURLException {
        super.addClassPathEntries(); // To change body of overridden methods use
        // File | Settings | File Templates.

        // lib/core/WEB-INF/lib
        addFileUrl(new File(
                ROOT + File.separator + LIB + File.separator + "core" + File.separator + "WEB-INF" + File.separator
                        + LIB + File.separator));
        addJarFileUrls(new File(
                ROOT + File.separator + LIB + File.separator + "core" + File.separator + "WEB-INF" + File.separator
                        + LIB));

        // lib/api
        addFileUrl(new File(ROOT + File.separator + LIB + File.separator + "api" + File.separator));
        addJarFileUrls(new File(ROOT + File.separator + LIB + File.separator + "api"));

        // repository/lib
        addFileUrl(new File(ROOT + File.separator + REPOSITORY + File.separator + LIB + File.separator));
        addJarFileUrls(new File(ROOT + File.separator + REPOSITORY + File.separator + LIB));

        // repository/components/plugins
        addFileUrl(new File(
                ROOT + File.separator + REPOSITORY + File.separator + COMPONENTS + File.separator + "plugins"
                        + File.separator));
        addJarFileUrls(new File(
                ROOT + File.separator + REPOSITORY + File.separator + COMPONENTS + File.separator + "plugins"));

        // repository/components/lib
        addFileUrl(new File(ROOT + File.separator + REPOSITORY + File.separator + COMPONENTS + File.separator + LIB
                                    + File.separator));
        addJarFileUrls(
                new File(ROOT + File.separator + REPOSITORY + File.separator + COMPONENTS + File.separator + LIB));

        //add component/plugins
        String internalLibPath = System.getProperty(COMPONENT_PLUGINS_DIR_PATH);
        if (internalLibPath != null) {
            File pluginsFile = new File(internalLibPath);
            addFileUrl(pluginsFile);
            addJarFileUrls(pluginsFile);
        }

    }

    @Override
    protected String getClassToLoad() {
        return "org.wso2.registry.checkin.Client";
    }

    @Override
    protected String getMethodToInvoke() {
        return "start";
    }
}
