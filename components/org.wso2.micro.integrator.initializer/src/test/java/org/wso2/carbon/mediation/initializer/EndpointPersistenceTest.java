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
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EndpointPersistenceTest extends MediationPersistenceTest {

    public void testEndpointPersistence() throws IOException {
        System.out.println("Starting endpoint persistence test...");

        String fileName = "epr1.xml";
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        Endpoint endpoint = createEndpoint(in);
        in.close();
        endpoint.setFileName(fileName);
        synapseConfigSvc.getSynapseConfiguration().addEndpoint(endpoint.getName(), endpoint);
        getMediationPersistenceManager().saveItem(endpoint.getName(),
                ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        System.out.println("Added new endpoint : " + endpoint.getName());
        checkSavedEndpoint(endpoint);

        if (endpoint instanceof AddressEndpoint) {
            ((AddressEndpoint) endpoint).getDefinition().setAddress("http://wso2.org/services/Foo");
            getMediationPersistenceManager().saveItem(endpoint.getName(),
                    ServiceBusConstants.ITEM_TYPE_ENDPOINT);
            System.out.println("Updated endpoint : " + endpoint.getName());
            checkSavedEndpoint(endpoint);
        }

        synapseConfigSvc.getSynapseConfiguration().removeEndpoint(endpoint.getName());
        getMediationPersistenceManager().deleteItem(endpoint.getName(), fileName,
                ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        System.out.println("Endpoint : " + endpoint.getName() + " removed");
        hold();

        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.ENDPOINTS_DIR, fileName);
        if (file.exists()) {
            fail("The file : " + fileName + " has not been deleted");
        }
        System.out.println("Endpoint file : " + fileName + " deleted successfully");

        checkSynapseXMLPersistence();

        System.out.println("Endpoint persistence test completed successfully...");
    }

    private void checkSynapseXMLPersistence() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("epr2.xml");
        Endpoint endpoint2 = createEndpoint(in);
        synapseConfigSvc.getSynapseConfiguration().addEndpoint(endpoint2.getName(), endpoint2);
        getMediationPersistenceManager().saveItem(endpoint2.getName(),
                ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        System.out.println("Added new endpoint : " + endpoint2.getName());
        hold();
        Endpoint copy = getConfigurationFromSynapseXML().getDefinedEndpoints().
                get(endpoint2.getName());
        assertEquals(endpoint2, copy);
        System.out.println("Endpoint : " + endpoint2.getName() + " saved successfully");

        synapseConfigSvc.getSynapseConfiguration().removeEndpoint(endpoint2.getName());
        getMediationPersistenceManager().deleteItem(endpoint2.getName(), null,
                ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        System.out.println("Endpoint : " + endpoint2.getName() + " removed");
        hold();

        Endpoint removedSeq = getConfigurationFromSynapseXML().getDefinedEndpoints().
                get(endpoint2.getName());
        assertNull(removedSeq);
        System.out.println("Endpoint : " + endpoint2.getName() + " deleted from synapse.xml successfully");
    }

    private void checkSavedEndpoint(Endpoint endpoint) {
        hold();
        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.ENDPOINTS_DIR, endpoint.getFileName());
        try {
            FileInputStream fin = new FileInputStream(file);
            Endpoint endpointCopy = EndpointFactory.getEndpointFromElement(parse(fin), false,
                    new Properties());
            assertEquals(endpoint, endpointCopy);
            System.out.println("Endpoint : " + endpoint.getName() + " saved successfully");
            fin.close();
        } catch (FileNotFoundException e) {
            fail("The endpoint : " + endpoint.getName() + " has not been saved");
        } catch (IOException e){
            fail("Error when closing file.");
        }
    }
}
