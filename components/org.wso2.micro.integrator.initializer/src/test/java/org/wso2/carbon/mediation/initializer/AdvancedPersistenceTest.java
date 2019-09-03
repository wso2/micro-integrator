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

import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.wso2.micro.integrator.initializer.persistence.MediationPersistenceManager;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AdvancedPersistenceTest extends MediationPersistenceTest {

    public void testMediationPersistence() throws IOException {
        String seqFileName = "seq1.xml";
        String proxyFileName = "proxy1.xml";
        
        InputStream inSeq = getClass().getClassLoader().getResourceAsStream(seqFileName);
        SequenceMediator seq = createSequence(inSeq);
        seq.setFileName(seqFileName);
        inSeq.close();
        Endpoint endpoint = createEndpoint(getClass().getClassLoader().
                getResourceAsStream("epr1.xml"));
        // Do not set a file name for the endpoint
        
        InputStream inProxy = getClass().getClassLoader().getResourceAsStream(proxyFileName);
        ProxyService proxy = createProxy(inProxy);
        proxy.setFileName(proxyFileName);
        inProxy.close();

        SynapseConfiguration synapseConfig = synapseConfigSvc.getSynapseConfiguration();
        synapseConfig.addSequence(seq.getName(), seq);
        synapseConfig.addEndpoint(endpoint.getName(), endpoint);
        synapseConfig.addProxyService(proxy.getName(), proxy);

        MediationPersistenceManager pm = getMediationPersistenceManager();
        pm.saveItem(seq.getName(), ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        pm.saveItem(endpoint.getName(), ServiceBusConstants.ITEM_TYPE_ENDPOINT);
        pm.saveItem(proxy.getName(), ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);

        hold();

        try {
            File seqFile = new File(path + File.separator +
                    MultiXMLConfigurationBuilder.SEQUENCES_DIR, seq.getFileName());
            FileInputStream finSeq = new FileInputStream(seqFile);
            SequenceMediator seqCopy = createSequence(finSeq);
            assertEquals(seq, seqCopy);
            finSeq.close();

            File proxyFile = new File(path + File.separator +
                    MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR, proxy.getFileName());
            FileInputStream finProxy = new FileInputStream(proxyFile);
            ProxyService proxyCopy = createProxy(finProxy);
            assertEquals(proxy, proxyCopy);
            finProxy.close();

            Endpoint endpointCopy = getConfigurationFromSynapseXML().getDefinedEndpoints().
                    get(endpoint.getName());
            assertEquals(endpoint, endpointCopy);

        } catch (FileNotFoundException e) {
            fail("An item has not been saved to the file system : " + e.getMessage());
        }catch (IOException e){
            fail("Error when closing file.");
        }

    }
}
