/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.micro.core.util;

import org.osgi.framework.Bundle;

import java.util.Arrays;

public class Axis2ConfigItemHolder {

    private Bundle[] moduleBundles;
    private Bundle[] deployerBundles;

    public Bundle[] getModuleBundles(){
        return moduleBundles;
    }

    public Bundle[] getDeployerBundles(){
        return deployerBundles;    
    }

    public void setModuleBundles(Bundle[] bundles){
        moduleBundles = Arrays.copyOf(bundles, bundles.length);
    }

    public void setDeployerBundles(Bundle[] bundles){
        deployerBundles = Arrays.copyOf(bundles, bundles.length);
    }
}
