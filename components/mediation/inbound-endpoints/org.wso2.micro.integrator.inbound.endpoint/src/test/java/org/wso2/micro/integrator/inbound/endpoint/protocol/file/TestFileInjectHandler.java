/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.micro.integrator.inbound.endpoint.protocol.file;

import org.apache.commons.vfs2.FileObject;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.protocol.file.FileInjectHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestFileInjectHandler extends FileInjectHandler {

    List<String> fileNames;
    boolean mockFailure = false;

    public TestFileInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
                                 SynapseEnvironment synapseEnvironment, Properties vfsProperties) {
        //do nothing
        super(injectingSeq, onErrorSeq, sequential, synapseEnvironment, vfsProperties);
        fileNames = new ArrayList<>();
    }

    @Override
    public boolean invoke(Object object, String name) throws SynapseException {
        FileObject file = (FileObject) object;
        fileNames.add(file.getName().getPath());
        if (mockFailure) {
            throw new SynapseException("Failure while processing the file");
        }
        return true;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setMockFailure(boolean mockFailure) {
        this.mockFailure = mockFailure;
    }
}
