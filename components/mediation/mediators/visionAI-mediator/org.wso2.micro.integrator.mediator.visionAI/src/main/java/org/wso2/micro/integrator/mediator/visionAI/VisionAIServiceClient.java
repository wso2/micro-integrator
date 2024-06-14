package org.wso2.micro.integrator.mediator.visionAI;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class VisionAIServiceClient implements MultipartFile {
    MessageContext synCtx = null;
    String originalName;
    String contentType;
    org.apache.axis2.context.MessageContext axis2MessageContext ;

    void VisionAIServiceClient(MessageContext synCtx) {
        this.synCtx = synCtx;
        this.axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
    }
    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getOriginalFilename() {
        this.originalName = axis2MessageContext.getEnvelope().getBody().getFirstElement().getLocalName();
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public void transferTo(File file) throws IOException, IllegalStateException {

    }
}
