package com.github.dkschlos.wsdlget.internal;

import com.ibm.wsdl.util.xml.DOM2Writer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchemaDownloader {

    private static final String IMPORT_TAG = "import";
    private static final String INCLUDE_TAG = "include";
    private static final String SCHEMA_LOCATION = "schemaLocation";

    private final File outputFolder;
    private final Definition definition;
    private final String serviceName;
    private final Map<String, String> changedSchemaLocations = new HashMap<String, String>();
    private final Set<String> processedSchemas = new HashSet<String>();

    private int schemaCount = 0;

    public SchemaDownloader(File outputFolder, Definition definition, String serviceName) {
        this.outputFolder = outputFolder;
        this.definition = definition;
        this.serviceName = serviceName;
    }

    public void download() {
        Types wsdlTypes = definition.getTypes();
        if (wsdlTypes != null) {
            List extensibilityElements = wsdlTypes.getExtensibilityElements();
            for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
                Object currentObject = iter.next();
                if (currentObject instanceof Schema) {
                    Schema schema = (Schema) currentObject;
                    processSchema(definition, schema, serviceName, null);
                }
            }
        }
    }

    private void processSchema(Definition definition, Schema schema, String serviceName, String fileName) {
        processedSchemas.add(schema.getDocumentBaseURI());

        try {
            if (schema.getIncludes() != null) {
                for (SchemaReference ref : (List<SchemaReference>) schema.getIncludes()) {
                    Schema includedSchema = ref.getReferencedSchema();
                    if (includedSchema == null || processedSchemas.contains(includedSchema.getDocumentBaseURI())) {
                        continue;
                    }

                    String fileNameChild = getSchemaFileName(serviceName, ref.getSchemaLocationURI());
                    processSchema(definition, includedSchema, serviceName, fileNameChild);

                }
            }
            if (schema.getImports() != null && schema.getImports().values() != null) {
                for (List<SchemaImport> schemaImportList : (Collection<List<SchemaImport>>) schema.getImports().values()) {
                    for (SchemaImport imp : schemaImportList) {
                        Schema importedSchema = imp.getReferencedSchema();
                        if (importedSchema == null || processedSchemas.contains(importedSchema.getDocumentBaseURI())) {
                            continue;
                        }
                        String fileNameChild = getSchemaFileName(serviceName, imp.getSchemaLocationURI());
                        processSchema(definition, importedSchema, serviceName, fileNameChild);

                    }
                }
            }
            changeLocations(schema.getElement(), changedSchemaLocations);
            if (fileName != null) {
                OutputStreamWriter writer = null;

                try {
                    writer = new OutputStreamWriter(new FileOutputStream(new File(outputFolder, fileName)), "UTF-8");
                    DOM2Writer.serializeAsXML(schema.getElement(), definition.getNamespaces(), writer);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error", ex);
        }

    }

    private String getSchemaFileName(String serviceName, String schemaLocationUrl) {
        String fileNameChild = changedSchemaLocations.get(schemaLocationUrl);
        if (fileNameChild == null) {
            fileNameChild = serviceName + schemaCount++ + ".xsd";
        }
        changedSchemaLocations.put(schemaLocationUrl, fileNameChild);
        return fileNameChild;
    }

    private void changeLocations(Element element, Map<String, String> changedSchemaLocations) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i), changedSchemaLocations);
            }
        }
    }

    private void processImport(Node importNode, Map<String, String> changedSchemaLocations) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals(SCHEMA_LOCATION)) {
                String attributeValue = attribute.getNodeValue();
                attributeValue = changedSchemaLocations.get(attributeValue);
                if (attributeValue != null) {
                    attribute.setNodeValue(attributeValue);
                }
            }
        }
    }
}
