package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axis2.Constants;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Utils {


    /**
     * Gives a List of query parameters
     *
     * @param axis2MessageContext
     * @return List<NameValuePair> - List of query parameters
     *
     */
    public static List<NameValuePair> getQueryParameters(org.apache.axis2.context.MessageContext axis2MessageContext){

        List<NameValuePair> queryParameter = null;

        // extract the query parameters from the Url
        try {
            queryParameter = URLEncodedUtils.parse(new URI((String) axis2MessageContext.getProperty(
                    Constants.Configuration.TRANSPORT_IN_URL)), "UTF-8");
        }catch (URISyntaxException e){
            e.printStackTrace();
        }

        if(queryParameter != null && queryParameter.size() > 0){
            return queryParameter;
        }

        return null;

    }
}
