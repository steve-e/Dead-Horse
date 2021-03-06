package com.example.steve.deadhorse;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.tools.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

public class XmlGGTest {

    public static final String CLASS_NAME = "Test";

    @Test
    public void testSimple() throws Exception {
        String xml = "<root/>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\")"));
        canCompile(src);
    }

    @Test
    public void testChild() throws Exception {
        String xml = "<root>" +
                "   <child/>" +
                "</root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(\"child\")" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testTwoChildren() throws Exception {
        String xml = "<root><child/><child/></root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(\"child\"), \n" +
                "element(\"child\")" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testAttributeInChild() throws Exception {
        String xml = "<root><child foo=\"bar\"/></root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(\"child\").with(" +
                "attribute(\"foo\", \"bar\")" +
                ")\n" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testAttributeInRoot() throws Exception {
        String xml = "<root foo=\"bar\"/>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "attribute(\"foo\", \"bar\")" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testElementInDefaultNamespace() throws Exception {
        String xml = "<root xmlns=\"def\" />";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").withDefaultNamespace(" +
                "\"def\"" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testElementWithNamespace() throws Exception {
        String xml = "<root xmlns:foo=\"bar\" />";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").build()"));
        canCompile(src);
    }


    @Test
    public void testRootElementInNamespace() throws Exception {
        String xml = "<foo:root xmlns:foo=\"bar\" />";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\",NamespaceUriPrefixMapping.namespace(\"bar\",\"foo\")).build()"));
        canCompile(src);
    }

    @Test
    public void testElementInNamespace() throws Exception {
        String xml = "<root><baz:child xmlns:baz=\"buzz\" /></root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(NamespaceUriPrefixMapping.namespace(\"buzz\",\"baz\"),\"child\"))"));
        canCompile(src);
    }

    @Test
    public void testDescendantElementInNamespace() throws Exception {
        String xml = "<root><baz:child xmlns:baz=\"buzz\" ><baz:foo/></baz:child></root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(NamespaceUriPrefixMapping.namespace(\"buzz\",\"baz\"),\"child\").with(" +
                "element(NamespaceUriPrefixMapping.namespace(\"buzz\",\"baz\"),\"foo\")" +
                ""));
        canCompile(src);
    }

    @Test
    public void testChildWithText() throws Exception {
        String xml = "<root><child>child text</child></root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "element(\"child\").with(" +
                "text(\"child text\")" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testTextNode() throws Exception {
        String xml = "<root>child</root>";
        String src = generateSrc(xml);
        assertThat(src, containsString("document(\"root\").with(" +
                "text(\"child\")" +
                ")"));
        canCompile(src);
    }

    @Test
    public void testBig() throws Exception {
        String xml = "<root><child a='b' c='d'><e/><e/><d d1='1'/></child></root>";
        String src = generateSrc(xml);

        canCompile(src);
    }

    private String generateSrc(String xml) throws ParserConfigurationException, IOException, SAXException {
        XmlGG xmlGG = new XmlGG(CLASS_NAME);
        return xmlGG.generateSrc(xml);
    }

    @Test
    public void testBigger() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!-- Edited by XMLSpy® -->\n" +
                "<html xsl:version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "  <body style=\"font-family:Arial;font-size:12pt;background-color:#EEEEEE\">\n" +
                "    <xsl:for-each select=\"breakfast_menu/food\">\n" +
                "      <div style=\"background-color:teal;color:white;padding:4px\">\n" +
                "        <span style=\"font-weight:bold\"><xsl:value-of select=\"name\"/></span>\n" +
                "        - <xsl:value-of select=\"price\"/>\n" +
                "      </div>\n" +
                "      <div style=\"margin-left:20px;margin-bottom:1em;font-size:10pt\">\n" +
                "        <xsl:value-of select=\"description\"/>\n" +
                "        <span style=\"font-style:italic\">\n" +
                "          <xsl:value-of select=\"calories\"/> (calories per serving)\n" +
                "        </span>\n" +
                "      </div>\n" +
                "    </xsl:for-each>\n" +
                "  </body>\n" +
                "</html>";
        String src = generateSrc(xml);

        canCompile(src);
    }

    private void canCompile(String src) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        JavaSourceFromString javaSource = new JavaSourceFromString(CLASS_NAME, src);
        Iterable<? extends JavaFileObject> javaFileObjects = Arrays.asList(javaSource);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, javaFileObjects);
        Boolean result = task.call();
        assertTrue("compilation should pass", result);
        new File(CLASS_NAME+".class").delete();
    }
}

