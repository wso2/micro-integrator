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

package org.wso2.micro.integrator.server.util;

import java.util.ArrayList;
import java.util.List;

public class PatchInfo {

    List<String> newPatches = new ArrayList<String>();
    List<String> removedPatches = new ArrayList<String>();

    public int getNewPatchesCount() {
        return newPatches.size();
    }

    public int getRemovedPatchesCount() {
        return removedPatches.size();
    }

    public void addNewPatches(String newPatchDirName) {
        if (newPatchDirName != null && !newPatchDirName.equals("")) {
            newPatches.add(newPatchDirName);
        }
    }

    public void addRemovedPatches(String removedPatchDirName) {
        if (removedPatchDirName != null && !removedPatchDirName.equals("")) {
            removedPatches.add(removedPatchDirName);
        }
    }

    public boolean isPatchesChanged() {
        return ((getNewPatchesCount() > 0) || (getRemovedPatchesCount() > 0));
    }

}