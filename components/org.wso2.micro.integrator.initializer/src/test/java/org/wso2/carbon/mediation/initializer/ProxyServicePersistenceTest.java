/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.initializer;

import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.ProxyServiceFactory;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.mediators.builtin.LogMediator;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProxyServicePersistenceTest extends MediationPersistenceTest {

    public void testProxyPersistence() throws IOException {
        System.out.println("Starting proxy persistence test...");

        String fileName = "proxy1.xml";
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        ProxyService proxy = createProxy(in);
        in.close();
        proxy.setFileName(fileName);
        synapseConfigSvc.getSynapseConfiguration().addProxyService(proxy.getName(), proxy);
        getMediationPersistenceManager().saveItem(proxy.getName(),
                ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        System.out.println("Added new proxy : " + proxy.getName());
        checkSavedProxy(proxy);

        if (proxy.getTargetInLineInSequence() != null) {
            proxy.getTargetInLineInSequence().addChild(new LogMediator());
            getMediationPersistenceManager().saveItem(proxy.getName(),
                    ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
            System.out.println("Updated proxy : " + proxy.getName());
            checkSavedProxy(proxy);
        }

        synapseConfigSvc.getSynapseConfiguration().removeProxyService(proxy.getName());
        getMediationPersistenceManager().deleteItem(proxy.getName(), fileName,
                ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        System.out.println("Proxy : " + proxy.getName() + " removed");
        hold();

        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR, fileName);
        if (file.exists()) {
            fail("The file : " + fileName + " has not been deleted");
        }
        System.out.println("Proxy service file : " + fileName + " deleted successfully");

        checkSynapseXMLPersistence();

        System.out.println("Proxy service persistence test completed successfully...");
    }

    private void checkSynapseXMLPersistence() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("proxy2.xml");
        ProxyService proxy2 = createProxy(in);
        synapseConfigSvc.getSynapseConfiguration().addProxyService(proxy2.getName(), proxy2);
        in.close();
        getMediationPersistenceManager().saveItem(proxy2.getName(),
                ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        System.out.println("Added new proxy service : " + proxy2.getName());
        hold();
        ProxyService copy = getConfigurationFromSynapseXML().getProxyService(proxy2.getName());
        assertEquals(proxy2, copy);
        System.out.println("Proxy service : " + proxy2.getName() + " saved successfully");

        synapseConfigSvc.getSynapseConfiguration().removeProxyService(proxy2.getName());
        getMediationPersistenceManager().deleteItem(proxy2.getName(), null,
                ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        System.out.println("Proxy service : " + proxy2.getName() + " removed");
        hold();

        ProxyService removedProxy = getConfigurationFromSynapseXML().getProxyService(proxy2.getName());
        assertNull(removedProxy);
        System.out.println("Proxy : " + proxy2.getName() + " deleted from synapse.xml successfully");
    }

    private void checkSavedProxy(ProxyService proxy) {
        hold();
        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR, proxy.getFileName());
        try {
            FileInputStream fin = new FileInputStream(file);
            ProxyService proxyCopy = ProxyServiceFactory.createProxy(parse(fin), new Properties());
            assertEquals(proxy, proxyCopy);
            System.out.println("Proxy service : " + proxy.getName() + " saved successfully");
            fin.close();
        } catch (FileNotFoundException e) {
            fail("The proxy service : " + proxy.getName() + " has not been saved");
        } catch (IOException e){
            fail("Error when closing file.");
        }
    }
}
