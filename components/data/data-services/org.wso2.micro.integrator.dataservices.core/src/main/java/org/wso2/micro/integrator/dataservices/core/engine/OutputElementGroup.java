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
package org.wso2.micro.integrator.dataservices.core.engine;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * Represents a multilevel <element/> element.
 */
public class OutputElementGroup extends OutputElement {

    private List<StaticOutputElement> attributeEntries;

    private List<StaticOutputElement> elementEntries;

    private List<CallQuery> callQueryEntries;

    private List<OutputElementGroup> elementGroupEntries;

    private List<OutputElement> allElements;

    private Result parentResult;

    /**
     * Thread local variables are kept to manage content filtering,
     * where certain attributes and elements are only visible to the current user,
     * so when a request is coming in, these TL values are populated accordingly.
     * These values cannot be stored in a session, since we cannot be certain,
     * that session management is supported.
     */
    private ThreadLocal<List<StaticOutputElement>> roleAttributeEntries =
            new ThreadLocal<List<StaticOutputElement>>();

    private ThreadLocal<List<OutputElement>> roleAllElements =
            new ThreadLocal<List<OutputElement>>();

    public OutputElementGroup(String name, String namespace, Set<String> requiredRoles,
    		String arrayName) {
        super(name, namespace, requiredRoles, arrayName);
        this.allElements = new ArrayList<OutputElement>();
        this.elementEntries = new ArrayList<StaticOutputElement>();
        this.callQueryEntries = new ArrayList<CallQuery>();
        this.elementGroupEntries = new ArrayList<OutputElementGroup>();
        this.attributeEntries = new ArrayList<StaticOutputElement>();
    }

    public void init() throws DataServiceFault {
        for (CallQuery callQuery : this.getCallQueryEntries()) {
            callQuery.init();
        }
        for (OutputElementGroup groups : this.getOutputElementGroupEntries()) {
            groups.init();
        }
    }

    public Result getParentResult() {
        return parentResult;
    }

    public void setParentResult(Result parentResult) {
        this.parentResult = parentResult;
    }

    @Override
    public void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                                  int queryLevel, boolean escapeNonPrintableChar) throws DataServiceFault {
        try {
            /* increment query level */
            queryLevel++;
            /* start writing element group */
            if (this.getName() != null) {
                this.startWrapperElement(xmlWriter, this.getNamespace(), this.getName(),
                        this.getParentResult().getResultType());
            }
            /* write attributes first */
            List<StaticOutputElement> attributes = this.getAttributeEntriesForCurrentRole();
            for (OutputElement oe : attributes) {
                oe.execute(xmlWriter, params, queryLevel, this.getParentResult().isEscapeNonPrintableChar());
            }
            /* write elements / call queries / element groups */
            List<OutputElement> elements = this.getAllElementsForCurrentRole();
            for (OutputElement oe : elements) {
                if (oe instanceof OutputElementGroup) {
                    ((OutputElementGroup) oe).applyUserRoles(oe.getRequiredRoles());
                }
                oe.execute(xmlWriter, params, queryLevel, this.getParentResult().isEscapeNonPrintableChar());
            }
            /* end element-group element */
            if (this.getName() != null) {
                this.endElement(xmlWriter);
            }
        } catch (XMLStreamException e) {
            throw new DataServiceFault(e, "Error in XML generation at OutputElementGroup.execute");
        }
    }

    /**
     * This populated the thread local variables that, track the allowed
     * attributes and elements for the given user roles.
     */
    public void applyUserRoles(Set<String> userRoles) {
        /* process attributes */
        List<StaticOutputElement> attrs =
                new ArrayList<StaticOutputElement>(this.getAttributeEntries());
        Iterator<StaticOutputElement> attrItr = attrs.iterator();
        StaticOutputElement attr;
        while (attrItr.hasNext()) {
            attr = attrItr.next();
            if (attr.getRequiredRoles() != null) {
                if (!this.rolesCompatible(userRoles, attr.getRequiredRoles())) {
                    attrItr.remove();
                }
            }
        }
        this.roleAttributeEntries.set(attrs);
        /* process elements */
        List<OutputElement> els = new ArrayList<OutputElement>(this.getAllElements());
        Iterator<OutputElement> elItr = els.iterator();
        OutputElement el;
        while (elItr.hasNext()) {
            el = elItr.next();
            if (el.getRequiredRoles() != null) {
                if (!this.rolesCompatible(userRoles, el.getRequiredRoles())) {
                    elItr.remove();
                }
            }
        }
        this.roleAllElements.set(els);
    }

    private boolean rolesCompatible(Set<String> userRoles, Set<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.size() == 0) {
            return true;
        }
        if ((userRoles == null || userRoles.size() == 0)) {
            return false;
        }
        Set<String> intersection = new HashSet<String>(userRoles);
        intersection.retainAll(requiredRoles);
        return !intersection.isEmpty();
    }

    public void addAttributeEntry(StaticOutputElement attr) throws DataServiceFault {
        if (!this.getAttributeEntries().contains(attr)) {
            this.getAttributeEntries().add(attr);
        } else {
            throw new DataServiceFault("Error while adding attributes. " +
                    "Cannot use same attribute name more than once");
        }
    }

    public void addElementEntry(StaticOutputElement el) throws DataServiceFault {
        this.getElementEntries().add(el);
        this.getAllElements().add(el);
    }

    public void addCallQueryEntry(CallQuery callQuery) {
        this.getCallQueryEntries().add(callQuery);
        this.getAllElements().add(callQuery);
    }

    public void addOutputElementGroupEntry(
            OutputElementGroup outputElementGroup) {
        this.getOutputElementGroupEntries().add(outputElementGroup);
        this.getAllElements().add(outputElementGroup);
    }

    public List<StaticOutputElement> getAttributeEntries() {
        return attributeEntries;
    }

    public List<StaticOutputElement> getElementEntries() {
        return elementEntries;
    }

    public List<CallQuery> getCallQueryEntries() {
        return callQueryEntries;
    }

    public List<OutputElementGroup> getOutputElementGroupEntries() {
        return elementGroupEntries;
    }

    public List<OutputElement> getAllElements() {
        return allElements;
    }

    public List<StaticOutputElement> getAttributeEntriesForCurrentRole() {
        return roleAttributeEntries.get();
    }

    public List<OutputElement> getAllElementsForCurrentRole() {
        return roleAllElements.get();
    }

}
