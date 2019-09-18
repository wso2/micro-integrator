package org.wso2.micro.integrator.pox.security.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;

@Component(
        name = "org.wso2.micro.integrator.ws.security.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private static String POX_SECURITY_MODULE = "POXSecurityModule";

    private static Log log = LogFactory.getLog(ServiceComponent.class);

    private  ConfigurationContext configCtx;

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            engagePoxSecurity();
        } catch (Throwable e) {
            log.error("Failed to activate Micro Integrator ws security bundle ", e);
        }
    }

    private void engagePoxSecurity() {
        try {
            //ConfigurationContext mainConfigCtx = configCtx.getServerConfigContext();
            //BundleContext bundleCtx = ctxt.getBundleContext();
            String enablePoxSecurity = CarbonServerConfigurationService .getInstance()
                    .getFirstProperty("EnablePoxSecurity");
            if (enablePoxSecurity == null || "true".equals(enablePoxSecurity)) {
                AxisConfiguration mainAxisConfig = configCtx.getAxisConfiguration();
                // Check for the module availability
                if (mainAxisConfig.getModules().toString().contains(POX_SECURITY_MODULE)){
                    mainAxisConfig.engageModule(POX_SECURITY_MODULE);
                    log.info("UT Security is activated");
                }
                log.info("UT Security is not activated");
            } else {
                log.info("POX Security Disabled");
            }

/*            bundleCtx.registerService(SecurityConfigAdmin.class.getName(),
                    new SecurityConfigAdmin(mainAxisConfig, registryService.getConfigSystemRegistry(), null), null);
            bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new SecurityAxis2ConfigurationContextObserver(), null);*/

            log.info("Security Mgt bundle is activated");

        } catch (Throwable e) {
            log.error("Failed to activate Micro Integrator WS security bundle ", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Micro Integrator Security bundle is deactivated ");
    }

    @Reference(
            name = "pox.security.set.configuration.context",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContext"
    )
    protected void setConfigurationContext(Axis2ConfigurationContextService configCtx) {
        this.configCtx = configCtx.getServerConfigContext();
    }

    protected void unsetConfigurationContext(Axis2ConfigurationContextService configCtx) {
        this.configCtx = null;
    }
}

