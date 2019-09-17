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

package org.wso2.carbon.ndatasource.rdbms;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.micro.integrator.ndatasource.rdbms.RDBMSDataSourceReader;
import org.wso2.micro.integrator.ndatasource.rdbms.utils.RDBMSDataSourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DBPropertiesTestCase {

    @Test
    public void modifyDBConfig() throws DataSourceException {
        RDBMSConfiguration configuration = new RDBMSConfiguration();
        List<RDBMSConfiguration.DataSourceProperty> propertyList = new ArrayList<>();
        RDBMSConfiguration.DataSourceProperty property = new RDBMSConfiguration.DataSourceProperty();
        property.setName("SetFloatAndDoubleUseBinary");
        property.setValue("true");
        propertyList.add(property);
        configuration.setDatabaseProps(propertyList);
        PoolConfiguration poolConfiguration = RDBMSDataSourceUtils.createPoolConfiguration(configuration);
        Assert.assertEquals("true", poolConfiguration.getDbProperties().getProperty("SetFloatAndDoubleUseBinary"));
    }

    @Test
    public void modifyDataSourceConfig() throws Exception {
        String fileName = this.getClass().getClassLoader().getResource("master-datasources.xml").getPath();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            RDBMSConfiguration configuration = RDBMSDataSourceReader.loadConfig(everything);
            PoolConfiguration poolConfiguration = RDBMSDataSourceUtils.createPoolConfiguration(configuration);
            Assert.assertEquals("true", poolConfiguration.getDbProperties().getProperty("SetFloatAndDoubleUseBinary"));
        }
    }

}
