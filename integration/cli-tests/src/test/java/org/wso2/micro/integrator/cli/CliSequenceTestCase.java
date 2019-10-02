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

import java.io.IOException;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.cli.util.TestUtils;

public class CliSequenceTestCase extends AbstractCliTest {

    private static final String CLI_TEST_SEQUENCE = "CliTestSequence";
    private static final String CLI_SAMPLE_SEQUENCE = "CliSampleSequence";
    private static final String MAIN_SEQUENCE = "main";
    private static final String FAULT_SEQUENCE = "fault";

    @BeforeClass
    public void loginBeforeClass() throws IOException {
        super.login();
    }

    /**
     * Get information about all Sequence
     */
    @Test
    public void miShowSequenceAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.SEQUENCE, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.size(), 5);
        // 5: Table heading, main, fault, CliTestSequence, CliSampleSequence

        String tableHeading = "NAME                STATS      TRACING";
        String testSequenceTableRow = "CliTestSequence     disabled   disabled";
        String sampleSequenceTableRow = "CliSampleSequence   disabled   disabled";
        String mainSequenceTableRow = "main                disabled   disabled";
        String faultSequenceTableRow = "fault               disabled   disabled";

        Assert.assertEquals(outputForCLICommand.get(0), tableHeading);
        Assert.assertTrue(outputForCLICommand.contains(testSequenceTableRow));
        Assert.assertTrue(outputForCLICommand.contains(sampleSequenceTableRow));
        Assert.assertTrue(outputForCLICommand.contains(mainSequenceTableRow));
        Assert.assertTrue(outputForCLICommand.contains(faultSequenceTableRow));
    }

    /**
     * Get information about single Sequence
     */
    @Test
    public void miShowSequenceTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.SEQUENCE,
                Constants.SHOW, CLI_TEST_SEQUENCE);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - " + CLI_TEST_SEQUENCE);
    }

    /**
     * Test un-deployed Sequence
     */
    @Test
    public void miShowSequenceNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.SEQUENCE,
                Constants.SHOW, "UndefinedSequence");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the Sequence 404 Not Found");
    }

    @AfterClass
    public void logoutAfterClass() throws IOException {
        super.logout();
    }
}
