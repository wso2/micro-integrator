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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);
    static File miBuildFilePath;
    static String miPath;

    /**
     * Get pom version from the pom file.
     */
    public static String getPomVerion() throws IOException, XmlPullParserException {

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("../pom.xml"));
        String pomVersion = model.getParent().getVersion();
        return pomVersion;
    }

    /**
     * Get the mi build path to run mi commands.
     */
    public static  String getMIBuildPath() throws IOException {
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

   public static List<String> runCLICommand(String artifactType , String command ) {

        List<String> lines = new ArrayList();
        String  line;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtils.runMiCommand(
                TestUtils.getMIBuildPath(),artifactType ,command ).getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.info("Exception occurred while running the command :  " + command + " for artifact : " + artifactType  + " . Exception : "  + e.getMessage());
        }
        return lines;
    }

    public static List<String> runCLICommandWithArtifactName(String artifactType, String command, String artifactName) {

        List<String> lines = new ArrayList();
        String  line;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtils.runMiCommandWithArtifact(
                TestUtils.getMIBuildPath(), artifactType,command , artifactName).getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.info("Exception occurred while running the command :  " + command + " for artifact : " + artifactType + " . Exception : "  + e.getMessage());
        }
        return lines;
    }

    /**
     * Run the mi commands.
     *
     * ex: mi sequence show
     * @param path mi binary file build path
     * @param artifactType artifact type of the mi commands
     * @param command mi command
     */
    public static Process runMiCommand(String path, String artifactType, String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(path, artifactType, command);
        Process process = builder.start();
        return process;
    }

    /**
     * Run the mi commands with the artifact name.
     *
     * ex: mi sequence show sampleSequence
     *  @param path mi binary file build path
     *  @param artifactType artifact type of the mi commands
     *  @param artifactName name of the artifact which want to get the information
     */
    public static  Process runMiCommandWithArtifact(String path, String artifactType, String command, String artifactName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(path, artifactType, command, artifactName);
        Process process = builder.start();
        return process;
    }


}






