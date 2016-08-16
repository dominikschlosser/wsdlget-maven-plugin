package com.github.dkschlos.wsdlget.internal;

import com.github.dkschlos.wsdlget.WsdlDefinition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

public class WsdlDownloader {

    private final File baseFolder;
    private final WsdlDefinition wsdl;
    private final Map<String, String> uriToWsdl = new HashMap<String, String>();

    private int wsdlCount = 0;

    public WsdlDownloader(File baseFolder, WsdlDefinition wsdl) {
        this.baseFolder = baseFolder;
        this.wsdl = wsdl;
    }

    public void download() {
        try {
            Definition wsdlDefinition = readWsdl(wsdl.getUrl());

            String serviceName = wsdl.getServiceName() == null
                    ? extractServiceName(wsdlDefinition)
                    : wsdl.getServiceName();

            uriToWsdl.put(wsdlDefinition.getDocumentBaseURI(), serviceName + ".wsdl");
            write(wsdlDefinition, serviceName, serviceName + ".wsdl");
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    private String extractServiceName(Definition wsdlDefinition) {
        if (wsdlDefinition.getServices().isEmpty()) {
            return "UnknownService";
        }

        return ((Service) wsdlDefinition.getServices().values().iterator().next()).getQName().getLocalPart();
    }

    private Definition readWsdl(String url) throws IllegalArgumentException, WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        Definition wsdlDefinition = reader.readWSDL(url);
        return wsdlDefinition;
    }

    @SuppressWarnings("unchecked")
    private void write(Definition definition, String serviceName, String fileName) throws Exception {
        Map<String, List<Import>> importMap = definition.getImports();
        List<Import> imports = new ArrayList<Import>();
        for (List<Import> value : importMap.values()) {
            imports.addAll(value);
        }

        if (!imports.isEmpty()) {
            for (Import wsdlImport : imports) {
                String wsdlLocation = wsdlImport.getDefinition().getDocumentBaseURI();
                String extension = wsdlLocation.contains("xsd") ? ".xsd" : ".wsdl";
                String fileNamePart = serviceName + wsdlCount++;
                String wsdlName = fileNamePart + extension;
                if (!uriToWsdl.containsKey(wsdlLocation)) {
                    uriToWsdl.put(wsdlLocation, wsdlName);
                    Definition innerDefinition = wsdlImport.getDefinition();
                    write(innerDefinition, serviceName, wsdlName);
                }

                wsdlImport.setLocationURI((String) uriToWsdl.get(wsdlLocation));
            }
        }
        File serviceFolder = getOrCreateServiceFolder(serviceName);

        SchemaDownloader schemaDownloader = new SchemaDownloader(serviceFolder, definition, serviceName);
        schemaDownloader.download();

        WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();

        writeToFile(serviceFolder, fileName, wsdlWriter, definition);
    }

    private void writeToFile(File serviceFolder, String fileName, WSDLWriter wsdlWriter, Definition definition) throws IOException, WSDLException {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(new File(serviceFolder, fileName)), "UTF-8");
            wsdlWriter.writeWSDL(definition, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private File getOrCreateServiceFolder(String serviceName) throws IOException {
        File serviceFolder = new File(baseFolder, serviceName);
        serviceFolder.mkdirs();
        return serviceFolder;
    }

}
