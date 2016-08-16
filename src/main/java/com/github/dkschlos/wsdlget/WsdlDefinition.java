package com.github.dkschlos.wsdlget;

import org.apache.maven.plugins.annotations.Parameter;

public class WsdlDefinition {

    @Parameter(required = true)
    private String serviceName;

    @Parameter(required = true)
    private String url;

    public String getServiceName() {
        return serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
