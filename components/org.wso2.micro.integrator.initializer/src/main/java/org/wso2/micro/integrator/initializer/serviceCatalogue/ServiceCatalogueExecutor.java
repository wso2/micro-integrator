package org.wso2.micro.integrator.initializer.serviceCatalogue;

import java.io.File;

public class ServiceCatalogueExecutor implements Runnable {


    public static final String SERVICE_CATALOGUE = "ServiceCatalogue";

    private String repoLocation;
    private static String CAPP_UNZIP_DIR;

    public ServiceCatalogueExecutor(String repoLocation) {
        this.repoLocation = repoLocation;
    }

    static {
        String javaTmpDir = System.getProperty("java.io.tmpdir");
        CAPP_UNZIP_DIR = javaTmpDir.endsWith(File.separator) ? javaTmpDir + SERVICE_CATALOGUE :
                javaTmpDir + File.separator + SERVICE_CATALOGUE;
    }

    @Override
    public void run() {
        File cappFolder = new File(repoLocation+"/carbonapps");

    }
}
