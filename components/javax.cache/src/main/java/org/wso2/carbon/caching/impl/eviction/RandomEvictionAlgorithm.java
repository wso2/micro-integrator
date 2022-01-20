/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.impl.eviction;

import org.wso2.carbon.caching.impl.CacheEntry;

import java.util.Random;
import java.util.TreeSet;

/**
 * Random cache eviction algorithm
 */
public class RandomEvictionAlgorithm implements EvictionAlgorithm {
    private static Random random = new Random();

    @Override
    public CacheEntry getEntryForEviction(TreeSet<CacheEntry> evictionSet) {
        int evictionIndex = random.nextInt(2);
        return evictionIndex == 0? evictionSet.pollFirst() : evictionSet.pollLast();
    }
}
