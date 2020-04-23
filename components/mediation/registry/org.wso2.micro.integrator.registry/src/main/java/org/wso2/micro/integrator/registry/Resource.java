/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 *
 */

package org.wso2.micro.integrator.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Adding basic resource object class so that this can be extended when needed.
 */
public class Resource {

    private File resourceFile;

    public Resource(File file) {
        resourceFile = file;
    }

    /**
     * Returns an input stream from the resource file.
     *
     * @return the input stream
     * @throws IOException if the file does not exist
     */
    public InputStream getContentStream() throws IOException {
        return new FileInputStream(resourceFile);
    }

}
