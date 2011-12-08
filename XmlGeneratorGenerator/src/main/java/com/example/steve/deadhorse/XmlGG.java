package com.example.steve.deadhorse;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;

public class XmlGG {

    private final String xml;
    private final String className;
    private final Map<String, String> namespaceMap = new HashMap<String, String>();
    private DocumentLoader documentLoader;

    public XmlGG(String xml, String className) {
        this.xml = xml;
        this.className = className;
        documentLoader = new DocumentLoader();
    }

    public String generateSrc() throws ParserConfigurationException, IOException, SAXException {
        Element element = documentElement();
        StringWriter src = new StringWriter();

        classDeclarationStart(src);
        documentRoot(element, src);
        elements(element, src);
        classEnd(src);

        return src.toString();
    }

    private Element documentElement() throws ParserConfigurationException, SAXException, IOException {
        Document document = documentLoader.document(xml);
        Element element = document.getDocumentElement();
        removeEmptyTextNodes(element);
        return element;
    }

    private void documentRoot(Element element, StringWriter src) {
        String namespaceURI = namespaceUriFromXmlnsAttribute(element);
        String defaultNamespace = defaultNamespace(element);
        String prefix = prefix(element);

        if (defaultNamespace != null && !defaultNamespace.trim().equals("") && null == prefix) {
            documentRootWithoutPrefixNamespace(element, src);
            documentDefaultNamespace(src, defaultNamespace);
        } else if (element.getTagName().contains(":")) {
            documentRootInNamespace(element, src, namespaceURI, prefix);
        } else {
            documentRootWithoutPrefixNamespace(element, src);
        }

        attributes(element, src);
    }

    private void documentRootInNamespace(Element element, StringWriter src, String namespaceURI, String prefix) {
        namespaceURI = getUriForPrefix(prefix, namespaceURI);
        src.append(String.format("document(\"%s\",NamespaceUriPrefixMapping.namespace(\"%s\",\"%s\"))", elementName(element), namespaceURI, prefix));
    }

    private void documentDefaultNamespace(StringWriter src, String defaultNamespace) {
        src.append(".withDefaultNamespace(").append("\"").append(defaultNamespace).append("\"")
                .append(")");
    }

    private String namespaceUriFromXmlnsAttribute(Element element) {
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

    private String defaultNamespace(Element element) {
        String xmlnsAttribute = element.getAttribute(XMLNS_ATTRIBUTE);
        if (!xmlnsAttribute.contains(":")) {
            return xmlnsAttribute;
        }
        return null;
    }

    private void documentRootWithoutPrefixNamespace(Element element, StringWriter src) {
        src.append(String.format("document(\"%s\")", elementName(element)));
    }

    private void elements(Element element, StringWriter src) {
        if (element.hasChildNodes()) {
            src.append(".with(");
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                node(src, childNodes.item(i));
                if (i + 1 < childNodes.getLength()) {
                    src.append(", \n");
                }
            }
            src.append(")\n");
        }
    }

    private String prefix(Node element) {
        String tagName = element.getNodeName();
        if (tagName.contains(":")) {
            int colonIndex = tagName.indexOf(":");
            return tagName.substring(0, colonIndex);
        }
        return element.getPrefix();
    }

    private String elementName(Element element) {
        String tagName = element.getTagName();
        if (tagName.contains(":")) {
            int colonIndex = tagName.indexOf(":");
            return tagName.substring(colonIndex + 1);
        }
        return tagName;
    }

    private void classDeclarationStart(StringWriter src) {
        src.append("import org.w3c.dom.Document;\n").append(
                "import org.w3c.dom.Element;\n").append(
                "import uk.co.mrmarkb.xmlbuild.*;\n").append(
                "import static uk.co.mrmarkb.xmlbuild.NamespaceUriPrefixMapping.namespace;\n").append(
                "import static uk.co.mrmarkb.xmlbuild.XmlBuilderFactory.*;\n").append(
                "import static uk.co.mrmarkb.xmlbuild.XmlRenderer.render;\n\n").append(
                "public class ").append(
                className).append(
                " {\n").append(
                "\tpublic Document build() {\n").append(
                "return ");
    }

    private void classEnd(StringWriter src) {
        src.append(".build();}\n").append("}");
    }

    private void children(Element node, StringWriter src) {
        String uri = namespaceUriFromXmlnsAttribute(node);
        String prefix = prefix(node);

        if (null == prefix) {
            src.append(String.format("element(\"%s\")", node.getNodeName()));
        } else {
            uri = getUriForPrefix(prefix, uri);
            src.append(String.format("element(NamespaceUriPrefixMapping.namespace(\"%s\",\"%s\"),\"%s\")", uri, prefix, elementName(node)));

        }
        attributes(node, src);
        removeEmptyTextNodes(node);
        elements(node, src);
    }

    private void removeEmptyTextNodes(Element node) {
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.TEXT_NODE && (child.getNodeValue() == null || child.getNodeValue().trim().equals(""))) {
                    node.removeChild(child);
                }
            }
        }
    }

    private String getUriForPrefix(String prefix, String uri) {
        if (null == uri) {
            uri = namespaceMap.get(prefix);
        }
        return uri;
    }

    private void attributes(Node node, StringWriter src) {
        if (node.hasAttributes()) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String attributeName = attribute.getNodeName();
                if (attributeName.startsWith(XMLNS_ATTRIBUTE)) {
                    attributes.removeNamedItem(attributeName);
                }
            }
            if (attributes.getLength() > 0) {
                src.append(".with(");
                for (int i = 0; i < attributes.getLength(); i++) {
                    attribute(attributes.item(i), src);
                    if (i + 1 < attributes.getLength()) {
                        src.append(", \n");
                    }
                }
                src.append(")\n");
            }
        }
    }

    private void node(StringWriter src, Node node) {
        short nodeType = node.getNodeType();
        switch (nodeType) {
            case Node.ELEMENT_NODE:
                children((Element) node, src);
                break;
            case Node.TEXT_NODE:
                src.append(textNode(node));
                break;
            case Node.ATTRIBUTE_NODE:
                attribute(node, src);
                break;
            default:
                System.out.println("nodeType = " + nodeType);
        }
    }

    private void attribute(Node node, StringWriter src) {
        src.append("attribute(")
                .append("\"").append(node.getNodeName()).append("\"")
                .append(", \"").append(node.getNodeValue()).append("\"")
                .append(")");

    }

    private CharSequence textNode(Node node) {
        String value = node.getNodeValue().trim();
        return String.format("text(\"%s\")", value);
    }
}
