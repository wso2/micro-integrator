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
package util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.wso2.carbon.esb.cli.CliAPITestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestUtils {
    protected Log log = LogFactory.getLog(CliAPITestCase.class);
    File miBuildFilePath;
    String miPath;


//     get pom version from the pom file
    public String getPomVerion() throws IOException, XmlPullParserException {

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("../pom.xml"));
        String pomVersion = model.getParent().getVersion();
        return pomVersion;
    }

//    get the mi build path to run mi commands
    public String getMIBuildPath() {
        try {
            TestUtils testUtils = new TestUtils();
            miBuildFilePath = new File(".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "cmd" + File.separator + "build" + File.separator + "wso2mi-cli-" + testUtils.getPomVerion()
                    + File.separator + "bin" + File.separator + "mi");
            miPath = miBuildFilePath.getCanonicalPath();

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } catch (XmlPullParserException e) {
            log.info("Exception = " + e.getMessage());
        }
        return miPath;
    }

    /**
     * run the mi commands
     * ex: mi sequence show
     */
    public Process runMiCommand(String path, String artifact, String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(path, artifact, command);
        Process process = builder.start();
        return process;
    }

    /**
     * run the mi commands with the artifact name
     * ex: mi sequence show sampleSequence
     */
    public Process runMiCommandWithArtifact(String path, String artifact, String command, String name) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(path, artifact, command, name);
        Process process = builder.start();
        return process;
    }

}






