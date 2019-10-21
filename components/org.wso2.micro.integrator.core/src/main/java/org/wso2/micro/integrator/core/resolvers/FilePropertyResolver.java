package org.wso2.micro.integrator.core.resolvers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.FilePropertyLoader;

/**
 *  File Property resolver can be used to resolve file property variables in the synapse config.
 */
public class FilePropertyResolver implements Resolver {

    private static final Log log = LogFactory.getLog(FilePropertyResolver.class);

    private String input;

    /**
     * set environment variable which needs to resolved
     **/
    @Override
    public void setVariable(String input) {
        this.input = input;
    }

    /**
     * file property variable is resolved in this function
     * @return resolved value for the file property variable
     */
    @Override
    public String resolve() {

        FilePropertyLoader fileLoaderObject = FilePropertyLoader.getFileLoaderInstance();
        fileLoaderObject.setFileValue(input);
        String filePropertyValue = fileLoaderObject.getFileValue();

        if (filePropertyValue == null) {
            throw new ResolverException("File Property variable could not be found");
        }
        return filePropertyValue;
    }
}
