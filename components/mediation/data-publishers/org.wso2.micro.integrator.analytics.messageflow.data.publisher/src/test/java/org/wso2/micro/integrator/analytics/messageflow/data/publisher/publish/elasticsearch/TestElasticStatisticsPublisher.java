/*
 * Copyright (c) (2017-2022), WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch;

import com.google.gson.JsonObject;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch.schema.ElasticDataSchema;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch.schema.ElasticDataSchemaElement;

import java.util.LinkedList;
import java.util.Queue;

public class TestElasticStatisticsPublisher extends ElasticStatisticsPublisher {
    private final Queue<JsonObject> analyticsQueue = new LinkedList<>();

    @Override
    void publishAnalytic(ElasticDataSchemaElement payload) {
        ElasticDataSchema dataSchemaInst = new ElasticDataSchema(payload);
        analyticsQueue.offer(dataSchemaInst.getJsonObject());
    }

    public int getAnalyticsCount() {
        return  analyticsQueue.size();
    }

    public JsonObject getAnalyticData() {
        return analyticsQueue.poll();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enableService() {
        enabled = true;
    }

    public void disableService() {
        enabled = false;
    }

    public void reset() {
        analyticsQueue.clear();
    }
}
