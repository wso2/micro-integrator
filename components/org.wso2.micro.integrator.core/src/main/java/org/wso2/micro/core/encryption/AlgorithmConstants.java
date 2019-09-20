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

package org.wso2.micro.core.encryption;

final class AlgorithmConstants {

    static final String ENC_METHOD_AES = "AES";
    static final String ENC_METHOD_DES = "DES";

    static final class AES {

        static final String GCM_MODE = "GCM";
        static final String CBC_MODE = "CBC";
        static final String OFB_MODE = "OFB";
        static final String CFB_MODE = "CFB";
        static final int DEFAULT_IV_LENGTH = 16;
        static final String DEFAULT_KEY_LENGTH = "128";
        static final int DEFAULT_GCM_TAG_LENGTH = 128;
    }

    static final class DES {

        static final int DEFAULT_IV_LENGTH = 8;
        static final String DEFAULT_KEY_LENGTH = "64";
    }

}
