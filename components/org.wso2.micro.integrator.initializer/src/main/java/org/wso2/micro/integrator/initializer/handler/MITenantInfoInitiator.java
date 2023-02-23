package org.wso2.micro.integrator.initializer.handler;

import org.apache.synapse.commons.util.ext.TenantInfoInitiator;

public class MITenantInfoInitiator implements TenantInfoInitiator {

    @Override
    public void initTenantInfo(){
        //Nothing to do here, since MI does not have tenancy
    }

    /**
     * initialize tenant information based on the request URI
     * @param uri request URI
     */
    @Override
    public void initTenantInfo(String uri) {
        //Nothing to do here, since MI does not have tenancy
    }

    @Override
    public void cleanTenantInfo() {
        //Nothing to do here, since MI does not have tenancy
    }

}
