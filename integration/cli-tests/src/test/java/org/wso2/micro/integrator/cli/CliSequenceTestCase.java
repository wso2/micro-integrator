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

import java.io.IOException;
import java.util.List;

public class CliSequenceTestCase {

    private static final String CLI_TEST_SEQUENCE = "CliTestSequence";
    private static final String CLI_SAMPLE_SEQUENCE = "CliSampleSequence";

    /**
     * Get information about all Sequence
     */
    @Test
    public void miShowSequenceAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.SEQUENCE, Constants.SHOW);
        String artifactName_seq_1[] = TestUtils.getArtifactList(outputForCLICommand).get(0).split(" ", 2);
        String artifactName_seq_2[] = TestUtils.getArtifactList(outputForCLICommand).get(1).split(" ", 2);

        Assert.assertEquals(artifactName_seq_1[0], CLI_TEST_SEQUENCE);
        Assert.assertEquals(artifactName_seq_2[0], CLI_SAMPLE_SEQUENCE);
    }

    /**
     * Get information about single Sequence
     */
    @Test
    public void miShowSequenceTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.SEQUENCE, Constants.SHOW, CLI_TEST_SEQUENCE);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - CliTestSequence");
    }

    /**
     * Test un-deployed Sequence
     */
    @Test
    public void miShowSequenceNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.SEQUENCE, Constants.SHOW, "CLITestSequence");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the Sequence 404 Not Found");
    }
}
