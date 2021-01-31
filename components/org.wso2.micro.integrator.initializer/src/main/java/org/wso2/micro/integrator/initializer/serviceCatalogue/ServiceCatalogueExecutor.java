package org.wso2.micro.integrator.initializer.serviceCatalogue;

import org.wso2.micro.application.deployer.AppDeployerUtils;
import org.wso2.micro.application.deployer.config.ApplicationConfiguration;
import org.wso2.micro.core.util.CarbonException;

import java.io.File;
import java.io.FilenameFilter;

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
        FilenameFilter filter = (f, name) -> {
            // filter car files
            return name.endsWith(".car");
        };

        File cappFolder = new File(repoLocation,"carbonapps");
        File[] files = cappFolder.listFiles(filter);
        for (File file : files) {
            try {
                String tempExtractedDirPath = AppDeployerUtils.extractCarbonApp(file.getPath());
                File f = new File(tempExtractedDirPath + ApplicationConfiguration.METADATA_XML);
                if (f.exists()) {
                    // only new CAPPs have the metadata.xml file
                    System.out.println("helooooo");
                }
            } catch (CarbonException e) {
                e.printStackTrace();
            }
        }
    }
}
