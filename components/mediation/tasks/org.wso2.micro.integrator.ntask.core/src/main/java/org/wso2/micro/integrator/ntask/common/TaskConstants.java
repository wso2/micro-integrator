/**
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.integrator.ntask.common;

import javax.xml.bind.annotation.XmlEnum;

/**
 * This class represents the task service constants.
 */
public class TaskConstants {

    public static final String TASK_CLASS_NAME = "__TASK_CLASS_NAME__";

    public static final String TASK_PROPERTIES = "__TASK_PROPERTIES__";

    @XmlEnum
    public static enum TaskMisfirePolicy {
        DEFAULT, IGNORE_MISFIRES, FIRE_AND_PROCEED, DO_NOTHING, FIRE_NOW, NEXT_WITH_EXISTING_COUNT, NEXT_WITH_REMAINING_COUNT, NOW_WITH_EXISTING_COUNT, NOW_WITH_REMAINING_COUNT
    }

}
