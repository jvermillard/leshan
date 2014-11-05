/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.impl.objectspec;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import leshan.server.impl.objectspec.ResourceSpec.Operations;
import leshan.server.impl.objectspec.ResourceSpec.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A parser for Object DDF files.
 */
public class DDFFileParser {

    private static final Logger LOG = LoggerFactory.getLogger(DDFFileParser.class);

    private final DocumentBuilderFactory factory;

    public DDFFileParser() {
        factory = DocumentBuilderFactory.newInstance();
    }

    public ObjectSpec parse(File ddfFile) {
        LOG.debug("Parsing DDF file {}", ddfFile.getName());

        ObjectSpec result = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(ddfFile);

            Node object = document.getDocumentElement().getElementsByTagName("Object").item(0);
            result = this.parseObject(object);

        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error("Could not parse the resource definition file " + ddfFile.getName(), e);
        }

        return result;
    }

    private ObjectSpec parseObject(Node object) {

        Integer id = null;
        String name = null;
        String description = null;
        boolean multiple = false;
        boolean mandatory = false;
        Map<Integer, ResourceSpec> resources = new HashMap<>();

        for (int i = 0; i < object.getChildNodes().getLength(); i++) {
            Node field = object.getChildNodes().item(i);
            switch (field.getNodeName()) {
            case "ObjectID":
                id = Integer.valueOf(field.getTextContent());
                break;
            case "Name":
                name = field.getTextContent();
                break;
            case "Description1":
                description = field.getTextContent();
                break;
            case "MultipleInstances":
                multiple = "Multiple".equals(field.getTextContent());
                break;
            case "Mandatory":
                mandatory = "Mandatory".equals(field.getTextContent());
                break;
            case "Resources":
                for (int j = 0; j < field.getChildNodes().getLength(); j++) {
                    Node item = field.getChildNodes().item(j);
                    if (item.getNodeName().equals("Item")) {
                        ResourceSpec res = this.parseResource(item);
                        resources.put(res.id, res);
                    }
                }
                break;
            }
        }

        return new ObjectSpec(id, name, description, multiple, mandatory, resources);

    }

    private ResourceSpec parseResource(Node item) {

        Integer id = Integer.valueOf(item.getAttributes().getNamedItem("ID").getTextContent());
        String name = null;
        Operations operations = Operations.NONE;
        boolean multiple = false;
        boolean mandatory = false;
        Type type = Type.STRING;
        String rangeEnumeration = null;
        String units = null;
        String description = null;

        for (int i = 0; i < item.getChildNodes().getLength(); i++) {
            Node field = item.getChildNodes().item(i);
            switch (field.getNodeName()) {
            case "Name":
                name = field.getTextContent();
                break;
            case "Operations":
                String strOp = field.getTextContent();
                if (strOp != null && !strOp.isEmpty()) {
                    operations = Operations.valueOf(strOp);
                }
                break;
            case "MultipleInstances":
                multiple = "Multiple".equals(field.getTextContent());
                break;
            case "Mandatory":
                mandatory = "Mandatory".equals(field.getTextContent());
                break;
            case "Type":
                switch (field.getTextContent()) {
                case "String":
                    type = Type.STRING;
                    break;
                case "Integer":
                    type = Type.INTEGER;
                    break;
                case "Float":
                    type = Type.FLOAT;
                    break;
                case "Boolean":
                    type = Type.BOOLEAN;
                    break;
                case "Opaque":
                    type = Type.OPAQUE;
                    break;
                case "Time":
                    type = Type.TIME;
                    break;
                }
                break;
            case "RangeEnumeration":
                rangeEnumeration = field.getTextContent();
                break;
            case "Units":
                units = field.getTextContent();
                break;
            case "Description":
                description = field.getTextContent();
                break;
            }

        }

        return new ResourceSpec(id, name, operations, multiple, mandatory, type, rangeEnumeration, units, description);
    }

}
