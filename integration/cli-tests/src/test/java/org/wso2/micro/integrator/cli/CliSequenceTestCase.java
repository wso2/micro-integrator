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

package org.wso2.micro.integrator.cli;

import util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class CliSequenceTestCase {

    private static final String cliTestSeq = "CliTestSequence";
    private static final String cliSampleSeq = "CliSampleSequence";

    /**
     * Get information about all Sequence
     */
    @Test
    public void miShowEndpointAllTest() {

        List<String> lines =  TestUtils.runCLICommand("sequence" ,"show");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestSeq)),cliTestSeq +" Sequence not found");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliSampleSeq)),cliSampleSeq + "Sequence not found");
    }

    /**
     * Get information about single Sequence
     */
    @Test
    public void miShowSequenceTest() {

        List<String> lines =  TestUtils.runCLICommandWithArtifactName("sequence" ,"show", cliTestSeq);
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestSeq)),cliTestSeq +" Sequence not found");
    }

    /**
     * Test un-deployed Sequence
     */
    @Test
    public void miShowSequenceNotFoundTest() {

        List<String> lines =  TestUtils.runCLICommandWithArtifactName("sequence" ,"show", "CLITestSequence");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Sequence 404 Not Found")),"Sequence 404 Not Found");
    }
}
