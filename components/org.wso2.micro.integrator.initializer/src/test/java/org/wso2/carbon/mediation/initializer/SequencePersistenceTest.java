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
import org.apache.synapse.mediators.builtin.LogMediator;
import org.apache.synapse.config.xml.SequenceMediatorFactory;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class SequencePersistenceTest extends MediationPersistenceTest {

    public void testSequencePersistence() throws IOException {
        System.out.println("Starting sequence persistence test...");

        String fileName = "seq1.xml";
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        SequenceMediator seq = createSequence(in);
        seq.setFileName(fileName);
        in.close();
        synapseConfigSvc.getSynapseConfiguration().addSequence(seq.getName(), seq);
        getMediationPersistenceManager().saveItem(seq.getName(),
                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        System.out.println("Added new sequence : " + seq.getName());
        checkSavedSequence(seq);

        seq.addChild(new LogMediator());
        getMediationPersistenceManager().saveItem(seq.getName(),
                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        System.out.println("Updated sequence : " + seq.getName());
        checkSavedSequence(seq);

        synapseConfigSvc.getSynapseConfiguration().removeSequence(seq.getName());
        getMediationPersistenceManager().deleteItem(seq.getName(), fileName,
                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        System.out.println("Sequence : " + seq.getName() + " removed");
        hold();

        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.SEQUENCES_DIR, fileName);
        if (file.exists()) {
            fail("The file : " + fileName + " has not been deleted");
        }
        System.out.println("Sequence file : " + fileName + " deleted successfully");

        checkSynapseXMLPersistence();

        System.out.println("Sequence persistence test completed successfully...");
    }

    private void checkSynapseXMLPersistence() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("seq2.xml");
        SequenceMediator seq2 = createSequence(in);
        in.close();
        synapseConfigSvc.getSynapseConfiguration().addSequence(seq2.getName(), seq2);
        getMediationPersistenceManager().saveItem(seq2.getName(),
                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        System.out.println("Added new sequence : " + seq2.getName());
        hold();
        SequenceMediator copy = getConfigurationFromSynapseXML().getDefinedSequences().
                get(seq2.getName());
        assertEquals(seq2, copy);
        System.out.println("Sequence : " + seq2.getName() + " saved successfully");

        synapseConfigSvc.getSynapseConfiguration().removeSequence(seq2.getName());
        getMediationPersistenceManager().deleteItem(seq2.getName(), null,
                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        System.out.println("Sequence : " + seq2.getName() + " removed");
        hold();

        SequenceMediator removedSeq = getConfigurationFromSynapseXML().getDefinedSequences().
                get(seq2.getName());
        assertNull(removedSeq);
        System.out.println("Sequence : " + seq2.getName() + " deleted from synapse.xml successfully");
    }

    private void checkSavedSequence(SequenceMediator seq) {
        hold();
        SequenceMediatorFactory factory = new SequenceMediatorFactory();
        File file = new File(path + File.separator +
                MultiXMLConfigurationBuilder.SEQUENCES_DIR, seq.getFileName());
        try {
            FileInputStream fin = new FileInputStream(file);
            SequenceMediator seqCopy = (SequenceMediator) factory.createMediator(parse(fin),
                    new Properties());
            assertEquals(seq, seqCopy);
            System.out.println("Sequence : " + seq.getName() + " saved successfully");
            fin.close();
        } catch (FileNotFoundException e) {
            fail("The sequence : " + seq.getName() + " has not been saved");
        } catch (IOException e){
            fail("Error when closing file.");
        }
    }
}
