/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserStoreException;

import java.io.IOException;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

/**
 * This is a wrapper class for LdapContext. Wrapped the LdapContext with StartTlsResponse.
 */
public class LdapContextWrapper implements LdapContext {

    private static Log log = LogFactory.getLog(LdapContextWrapper.class);
    private StartTlsResponse startTlsResponse;
    private LdapContext ldapContext;
    private StartTlsResponseWrapper startTlsResponseWrapper;

    private LdapContextWrapper(LdapContext ldapContext, StartTlsResponse startTlsResponse) {

        this.ldapContext = ldapContext;
        this.startTlsResponse = startTlsResponse;
        this.startTlsResponseWrapper = new StartTlsResponseWrapper(this.startTlsResponse);
        this.startTlsResponseWrapper.incrementReferenceCounter();
    }

    private LdapContextWrapper(LdapContext ldapContext, StartTlsResponseWrapper startTlsResponseWrapper) {

        this.ldapContext = ldapContext;
        this.startTlsResponseWrapper = startTlsResponseWrapper;
        this.startTlsResponseWrapper.incrementReferenceCounter();
    }

    /**
     * Initialize the LDAP context with secured connection by applying StartTLS extended operation.
     *
     * @param environment        environment used to create the initial Context.
     * @param connectionControls connection request controls for the initial context.
     * @return secured ldap connection context.
     * @throws NamingException    if a naming exception is encountered.
     * @throws UserStoreException if a user store related exception is encountered.
     */
    public static LdapContext startTLS(Hashtable<?, ?> environment, Control[] connectionControls)
            throws NamingException, UserStoreException {

        Hashtable<String, Object> tempEnv = getEnvironmentForSecuredLdapInitialization(environment);
        LdapContext ldapContext = new InitialLdapContext(tempEnv, connectionControls);
        try {
            StartTlsResponse startTlsResponse = (StartTlsResponse) ldapContext.extendedOperation(new StartTlsRequest());
            startTlsResponse.negotiate();
            if (log.isDebugEnabled()) {
                log.debug("StartTLS connection established successfully with LDAP server");
            }
            LdapContextWrapper ldapContextWrapper = new LdapContextWrapper(ldapContext, startTlsResponse);
            ldapContextWrapper.performAuthenticationIfProvided(environment);
            return ldapContextWrapper;
        } catch (IOException e) {
            throw new UserStoreException("Unable to establish the StartTLS connection", e);
        }
    }

    /**
     * Get environment variables to initialize secured LDAP context.
     *
     * @param environment environment used to create the initial Context.
     * @return environment.
     */
    private static Hashtable<String, Object> getEnvironmentForSecuredLdapInitialization(Hashtable<?, ?> environment) {

        Hashtable<String, Object> tempEnv = new Hashtable<>();
        // Create a temp env for this particular connection by eliminating user credentials details from original env.
        for (Object key : environment.keySet()) {
            if (Context.SECURITY_PRINCIPAL.equals(key) || Context.SECURITY_CREDENTIALS.equals(key) ||
                    Context.SECURITY_AUTHENTICATION.equals(key)) {
                if (log.isDebugEnabled()) {
                    log.debug("Attribute " + key + " is skip adding to the environment for TLS LDAP initialization");
                }
            } else {
                tempEnv.put((String) key, environment.get(key));
            }
        }
        return tempEnv;
    }

    /**
     * Perform simple client authentication.
     *
     * @param environment environment used to create the initial Context.
     * @throws NamingException if a naming exception is encountered.
     */
    private void performAuthenticationIfProvided(Hashtable<?, ?> environment)
            throws NamingException {

        // Adding provided user credentials details one by one after TLS connection started.
        if (environment.containsKey(Context.SECURITY_AUTHENTICATION)) {
            ldapContext.addToEnvironment(Context.SECURITY_AUTHENTICATION,
                    environment.get(Context.SECURITY_AUTHENTICATION));
            if (log.isDebugEnabled()) {
                log.debug("Attribute " + Context.SECURITY_AUTHENTICATION + " is added to the " +
                        "TLS LdapContext environment");
            }
        }
        if (environment.containsKey(Context.SECURITY_PRINCIPAL)) {
            ldapContext.addToEnvironment(Context.SECURITY_PRINCIPAL,
                    environment.get(Context.SECURITY_PRINCIPAL));
            if (log.isDebugEnabled()) {
                log.debug("Attribute " + Context.SECURITY_PRINCIPAL + " is added to the " +
                        "TLS LdapContext environment");
            }
        }
        if (environment.containsKey(Context.SECURITY_CREDENTIALS)) {
            ldapContext.addToEnvironment(Context.SECURITY_CREDENTIALS,
                    environment.get(Context.SECURITY_CREDENTIALS));
            if (log.isDebugEnabled()) {
                log.debug("Attribute " + Context.SECURITY_CREDENTIALS + " is added to the " +
                        "TLS LdapContext environment");
            }
        }
    }

    @Override
    public ExtendedResponse extendedOperation(ExtendedRequest request) throws NamingException {

        return ldapContext.extendedOperation(request);
    }

    @Override
    public LdapContext newInstance(Control[] requestControls) throws NamingException {

        return new LdapContextWrapper(ldapContext.newInstance(requestControls), startTlsResponseWrapper);
    }

    @Override
    public void reconnect(Control[] connCtls) throws NamingException {

        ldapContext.reconnect(connCtls);
    }

    @Override
    public Control[] getConnectControls() throws NamingException {

        return ldapContext.getConnectControls();
    }

    @Override
    public Control[] getRequestControls() throws NamingException {

        return ldapContext.getRequestControls();
    }

    @Override
    public void setRequestControls(Control[] requestControls) throws NamingException {

        ldapContext.setRequestControls(requestControls);
    }

    @Override
    public Control[] getResponseControls() throws NamingException {

        return ldapContext.getResponseControls();
    }

    @Override
    public Attributes getAttributes(Name name) throws NamingException {

        return ldapContext.getAttributes(name);
    }

    @Override
    public Attributes getAttributes(String name) throws NamingException {

        return ldapContext.getAttributes(name);
    }

    @Override
    public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {

        return ldapContext.getAttributes(name, attrIds);
    }

    @Override
    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {

        return ldapContext.getAttributes(name, attrIds);
    }

    @Override
    public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws NamingException {

        ldapContext.modifyAttributes(name, mod_op, attrs);
    }

    @Override
    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {

        ldapContext.modifyAttributes(name, mod_op, attrs);
    }

    @Override
    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {

        ldapContext.modifyAttributes(name, mods);
    }

    @Override
    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {

        ldapContext.modifyAttributes(name, mods);
    }

    @Override
    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {

        ldapContext.bind(name, obj, attrs);
    }

    @Override
    public void bind(String name, Object obj, Attributes attrs) throws NamingException {

        ldapContext.bind(name, obj, attrs);
    }

    @Override
    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {

        ldapContext.rebind(name, obj, attrs);
    }

    @Override
    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {

        ldapContext.rebind(name, obj, attrs);
    }

    @Override
    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.createSubcontext(name, attrs), startTlsResponseWrapper);
    }

    @Override
    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.createSubcontext(name, attrs), startTlsResponseWrapper);
    }

    @Override
    public DirContext getSchema(Name name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.getSchema(name), startTlsResponseWrapper);
    }

    @Override
    public DirContext getSchema(String name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.getSchema(name), startTlsResponseWrapper);
    }

    @Override
    public DirContext getSchemaClassDefinition(Name name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.getSchemaClassDefinition(name), startTlsResponseWrapper);
    }

    @Override
    public DirContext getSchemaClassDefinition(String name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.getSchemaClassDefinition(name), startTlsResponseWrapper);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes,
                                                  String[] attributesToReturn) throws NamingException {

        return ldapContext.search(name, matchingAttributes, attributesToReturn);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
                                                  String[] attributesToReturn) throws NamingException {

        return ldapContext.search(name, matchingAttributes, attributesToReturn);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {

        return ldapContext.search(name, matchingAttributes);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {

        return ldapContext.search(name, matchingAttributes);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
            throws NamingException {

        return ldapContext.search(name, filter, cons);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {

        return ldapContext.search(name, filter, cons);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
                                                  SearchControls cons) throws NamingException {

        return ldapContext.search(name, filterExpr, filterArgs, cons);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
                                                  SearchControls cons) throws NamingException {

        return ldapContext.search(name, filterExpr, filterArgs, cons);
    }

    @Override
    public Object lookup(Name name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.lookup(name), startTlsResponseWrapper);
    }

    @Override
    public Object lookup(String name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.lookup(name), startTlsResponseWrapper);

    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {

        ldapContext.bind(name, obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {

        ldapContext.bind(name, obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {

        ldapContext.rebind(name, obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {

        ldapContext.rebind(name, obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {

        ldapContext.unbind(name);
    }

    @Override
    public void unbind(String name) throws NamingException {

        ldapContext.unbind(name);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {

        ldapContext.rename(oldName, newName);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {

        ldapContext.rename(oldName, newName);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {

        return ldapContext.list(name);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {

        return ldapContext.list(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {

        return ldapContext.listBindings(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {

        return ldapContext.listBindings(name);
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {

        ldapContext.destroySubcontext(name);
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {

        ldapContext.destroySubcontext(name);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.createSubcontext(name), startTlsResponseWrapper);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {

        return new LdapContextWrapper((LdapContext) ldapContext.createSubcontext(name), startTlsResponseWrapper);
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {

        return ldapContext.lookupLink(name);
    }

    @Override
    public Object lookupLink(String name) throws NamingException {

        return ldapContext.lookupLink(name);
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {

        return ldapContext.getNameParser(name);
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {

        return ldapContext.getNameParser(name);
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {

        return ldapContext.composeName(name, prefix);
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {

        return ldapContext.composeName(name, prefix);
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {

        return ldapContext.addToEnvironment(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {

        return ldapContext.removeFromEnvironment(propName);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {

        return ldapContext.getEnvironment();
    }

    @Override
    public void close() throws NamingException {

        try {
            startTlsResponseWrapper.close();
        } finally {
            ldapContext.close();
        }
    }

    @Override
    public String getNameInNamespace() throws NamingException {

        return ldapContext.getNameInNamespace();
    }
}
