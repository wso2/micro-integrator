/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.synapse.MessageContext;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class SecurityHandlerAdapterTest {

    /**
     * Tests a handler configuration similar to the following with no resources defined.
     * <p>
     * <handler name="SampleInternalApiHandlerWithNoResources"
     *          class="internal.http.api.SampleInternalApiHandlerWithNoResources"/>
     */
    @Test
    public void testHandledWithNoResource() {

        //set message context
        MessageContext messageContext = new TestMessageContext();
        EndpointReference endpointReference = new EndpointReference();
        endpointReference.setAddress("/sectest/resource1");
        messageContext.setTo(endpointReference);

        TestSecurityHandler internalAPIHandler = new TestSecurityHandler("/sectest");

        //test with no resources
        internalAPIHandler.setResources(new ArrayList<>());
        internalAPIHandler.invoke(messageContext);
        Assert.assertTrue("Handler should be engaged when no resources are explictely defined, but it was not engaged.",
                          internalAPIHandler.isHandleTriggered());
    }

    /**
     * Tests a handler configuration similar to the following.
     * <p>
     * <handler name="SampleInternalApiHandlerWithCustomResources"
     *          class="internal.http.api.SampleInternalApiHandlerWithCustomResources">
     *  <resources>
     *      <resource>/resource1</resource>
     *      <resource>/resource2</resource>
     *  </resources>
     *</handler>
     */
    @Test
    public void testHandledWithCustomResources() {

        //Create test message context
        MessageContext messageContext = new TestMessageContext();
        EndpointReference endpointReference = new EndpointReference();
        messageContext.setTo(endpointReference);

        TestSecurityHandler internalAPIHandler = new TestSecurityHandler("/sectest");
        List<String> resources = new ArrayList<>();
        resources.add("/resource1");
        resources.add("/resource2");
        internalAPIHandler.setResources(resources);

        //set message context with matching resource
        endpointReference.setAddress("/sectest/resource1");
        internalAPIHandler.invoke(messageContext);
        Assert.assertTrue("Handler should be engaged since resource 1 was defined, but it was not engaged.",
                          internalAPIHandler.isHandleTriggered());

        //set message context with matching resource
        endpointReference.setAddress("/sectest/resource2");
        internalAPIHandler.invoke(messageContext);
        Assert.assertTrue("Handler should be engaged since resource 2 was defined, but it was not engaged.",
                          internalAPIHandler.isHandleTriggered());

        //set message context with matching resource but containing a sub resource
        endpointReference.setAddress("/sectest/resource2/resource22");
        internalAPIHandler.invoke(messageContext);
        Assert.assertTrue("Handler should be engaged since resource 2 was defined, but it was not engaged.",
                          internalAPIHandler.isHandleTriggered());

        //set message context with a resource that is not matching
        endpointReference.setAddress("/sectest/resource3");
        internalAPIHandler.invoke(messageContext);
        Assert.assertFalse("Handler should not be engaged since resource 3 was not defined, but it was engaged.",
                           internalAPIHandler.isHandleTriggered());

    }

    /**
     * Tests a handler configuration similar to the following.
     *
     * <handler name="SampleInternalApiHandlerWithAllResources"
     *          class="internal.http.api.SampleInternalApiHandlerWithAllResources">
     *  <resources>
     *      <resource>/</resource>
     *  </resources>
     *</handler>
     */
    @Test
    public void testHandledWithAllResources() {

        //Create test message context
        MessageContext messageContext = new TestMessageContext();
        EndpointReference endpointReference = new EndpointReference();
        messageContext.setTo(endpointReference);

        TestSecurityHandler internalAPIHandler = new TestSecurityHandler("/sectest");
        List<String> resources = new ArrayList<>();
        resources.add("/");
        internalAPIHandler.setResources(resources);

        //set message context with matching resource
        endpointReference.setAddress("/sectest/resource1");
        internalAPIHandler.invoke(messageContext);
        Assert.assertTrue("Handler should be engaged since all resources are defined, but it was not engaged.",
                          internalAPIHandler.isHandleTriggered());
    }
}