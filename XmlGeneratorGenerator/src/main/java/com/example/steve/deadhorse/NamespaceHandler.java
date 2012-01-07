package com.example.steve.deadhorse;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;

public class NamespaceHandler {
        private final Map<String, String> namespaceMap = new HashMap<String, String>();

    public String getUriForPrefix(String prefix, String uri) {
        if (null == uri) {
            uri = namespaceMap.get(prefix);
        }
        return uri;
    }

    public String namespaceUriFromXmlnsAttribute(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String attributeName = attribute.getNodeName();
            if (attributeName.startsWith(XMLNS_ATTRIBUTE + ":")) {
                int colonIndex = attributeName.indexOf(":");
                String prefix = attributeName.substring(colonIndex + 1);
                String uri = attribute.getNodeValue();
                namespaceMap.put(prefix, uri);
            }
        }
        if (element.hasAttribute(XMLNS_ATTRIBUTE)) {
            return element.getAttributeNode(XMLNS_ATTRIBUTE).getValue();
        }
        return null;
    }

    public String defaultNamespace(Element element) {
        String xmlnsAttribute = element.getAttribute(XMLNS_ATTRIBUTE);
        if (!xmlnsAttribute.contains(":")) {
            return xmlnsAttribute;
        }
        return null;
    }
}
