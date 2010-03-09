/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.xml;

import junit.framework.TestCase;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.w3c.dom.UserDataHandler.NODE_CLONED;

/**
 * Construct a DOM and then interrogate it.
 */
public class DomTest extends TestCase {

    private Transformer transformer;
    private DocumentBuilder builder;
    private DOMImplementation domImplementation;

    private final String xml
            = "<!DOCTYPE menu ["
            + "  <!ENTITY sp \"Maple Syrup\">"
            + "  <!NOTATION png SYSTEM \"image/png\">"
            + "]>"
            + "<menu>\n"
            + "  <item xmlns=\"http://food\" xmlns:a=\"http://addons\">\n"
            + "    <name a:standard=\"strawberry\" deluxe=\"&sp;\">Waffles</name>\n"
            + "    <description xmlns=\"http://marketing\">Belgian<![CDATA[ waffles & strawberries (< 5g ]]>of fat)</description>\n"
            + "    <a:option>Whipped Cream</a:option>\n"
            + "    <a:option>&sp;</a:option>\n"
            + "    <?wafflemaker square shape?>\n"
            + "    <nutrition>\n"
            + "      <a:vitamins xmlns:a=\"http://usda\">\n"
            + "        <!-- add other vitamins? --> \n"
            + "        <a:vitaminc>60%</a:vitaminc>\n"
            + "      </a:vitamins>\n"
            + "    </nutrition>\n"
            + "  </item>\n"
            + "</menu>";

    private Document document;
    private DocumentType doctype;
    private Entity sp;
    private Notation png;
    private Element menu;
    private Element item;
    private Attr itemXmlns;
    private Attr itemXmlnsA;
    private Element name;
    private Attr standard;
    private Attr deluxe;
    private Text waffles;
    private Element description;
    private Text descriptionText1;
    private CDATASection descriptionText2;
    private Text descriptionText3;
    private Element option1;
    private Element option2;
    private Node option2Reference; // resolved to Text on RI, an EntityReference on Dalvik
    private ProcessingInstruction wafflemaker;
    private Element nutrition;
    private Element vitamins;
    private Attr vitaminsXmlnsA;
    private Comment comment;
    private Element vitaminc;
    private Text vitamincText;
    private List<Node> allNodes;

    @Override protected void setUp() throws Exception {
        transformer = TransformerFactory.newInstance().newTransformer();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        domImplementation = builder.getDOMImplementation();
        document = builder.parse(new InputSource(new StringReader(xml)));

        // doctype nodes
        doctype = document.getDoctype();
        if (doctype.getEntities() != null) {
            sp = (Entity) doctype.getEntities().item(0);
        }
        if (doctype.getNotations() != null) {
            png = (Notation) doctype.getNotations().item(0);
        }

        // document nodes
        menu = document.getDocumentElement();
        item = (Element) menu.getChildNodes().item(1);
        itemXmlns = item.getAttributeNode("xmlns");
        itemXmlnsA = item.getAttributeNode("xmlns:a");
        name = (Element) item.getChildNodes().item(1);
        standard = name.getAttributeNode("a:standard");
        deluxe = name.getAttributeNode("deluxe");
        waffles = (Text) name.getChildNodes().item(0);
        description = (Element) item.getChildNodes().item(3);
        descriptionText1 = (Text) description.getChildNodes().item(0);
        descriptionText2 = (CDATASection) description.getChildNodes().item(1);
        descriptionText3 = (Text) description.getChildNodes().item(2);
        option1 = (Element) item.getChildNodes().item(5);
        option2 = (Element) item.getChildNodes().item(7);
        option2Reference = option2.getChildNodes().item(0);
        wafflemaker = (ProcessingInstruction) item.getChildNodes().item(9);
        nutrition = (Element) item.getChildNodes().item(11);
        vitamins = (Element) nutrition.getChildNodes().item(1);
        vitaminsXmlnsA = vitamins.getAttributeNode("xmlns:a");
        comment = (Comment) vitamins.getChildNodes().item(1);
        vitaminc = (Element) vitamins.getChildNodes().item(3);
        vitamincText = (Text) vitaminc.getChildNodes().item(0);

        allNodes = new ArrayList<Node>();

        if (sp != null) {
            allNodes.add(sp);
        }
        if (png != null) {
            allNodes.add(png);
        }

        allNodes.addAll(Arrays.asList(document, doctype, menu, item, itemXmlns,
                itemXmlnsA, name, standard, deluxe, waffles, description,
                descriptionText1, descriptionText2, descriptionText3, option1,
                option2, option2Reference, wafflemaker, nutrition, vitamins,
                vitaminsXmlnsA, comment, vitaminc, vitamincText));
    }

    /**
     * Android's parsed DOM doesn't include entity declarations. These nodes will
     * only be tested for implementations that support them.
     */
    public void testEntityDeclarations() {
        assertNotNull("This implementation does not parse entity declarations", sp);
    }

    /**
     * Android's parsed DOM doesn't include notations. These nodes will only be
     * tested for implementations that support them.
     */
    public void testNotations() {
        assertNotNull("This implementation does not parse notations", png);
    }

    public void testLookupNamespaceURIByPrefix() {
        assertEquals(null, doctype.lookupNamespaceURI("a"));
        if (sp != null) {
            assertEquals(null, sp.lookupNamespaceURI("a"));
        }
        if (png != null) {
            assertEquals(null, png.lookupNamespaceURI("a"));
        }
        assertEquals(null, document.lookupNamespaceURI("a"));
        assertEquals(null, menu.lookupNamespaceURI("a"));
        assertEquals("http://addons", item.lookupNamespaceURI("a"));
        assertEquals("http://addons", itemXmlns.lookupNamespaceURI("a"));
        assertEquals("http://addons", itemXmlnsA.lookupNamespaceURI("a"));
        assertEquals("http://addons", name.lookupNamespaceURI("a"));
        assertEquals("http://addons", standard.lookupNamespaceURI("a"));
        assertEquals("http://addons", deluxe.lookupNamespaceURI("a"));
        assertEquals("http://addons", description.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText1.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText2.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText3.lookupNamespaceURI("a"));
        assertEquals("http://addons", option1.lookupNamespaceURI("a"));
        assertEquals("http://addons", option2.lookupNamespaceURI("a"));
        assertEquals("http://addons", option2Reference.lookupNamespaceURI("a"));
        assertEquals("http://addons", wafflemaker.lookupNamespaceURI("a"));
        assertEquals("http://addons", nutrition.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitamins.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitaminsXmlnsA.lookupNamespaceURI("a"));
        assertEquals("http://usda", comment.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitaminc.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitamincText.lookupNamespaceURI("a"));
    }

    public void testLookupNamespaceURIWithNullPrefix() {
        assertEquals(null, document.lookupNamespaceURI(null));
        assertEquals(null, doctype.lookupNamespaceURI(null));
        if (sp != null) {
            assertEquals(null, sp.lookupNamespaceURI(null));
        }
        if (png != null) {
            assertEquals(null, png.lookupNamespaceURI(null));
        }
        assertEquals(null, menu.lookupNamespaceURI(null));
        assertEquals("http://food", item.lookupNamespaceURI(null));
        assertEquals("http://food", itemXmlns.lookupNamespaceURI(null));
        assertEquals("http://food", itemXmlnsA.lookupNamespaceURI(null));
        assertEquals("http://food", name.lookupNamespaceURI(null));
        assertEquals("http://food", standard.lookupNamespaceURI(null));
        assertEquals("http://food", deluxe.lookupNamespaceURI(null));
        assertEquals("http://marketing", description.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText1.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText2.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText3.lookupNamespaceURI(null));
        assertEquals("http://food", option1.lookupNamespaceURI(null));
        assertEquals("http://food", option2.lookupNamespaceURI(null));
        assertEquals("http://food", option2Reference.lookupNamespaceURI(null));
        assertEquals("http://food", wafflemaker.lookupNamespaceURI(null));
        assertEquals("http://food", nutrition.lookupNamespaceURI(null));
        assertEquals("http://food", vitamins.lookupNamespaceURI(null));
        assertEquals("http://food", vitaminsXmlnsA.lookupNamespaceURI(null));
        assertEquals("http://food", comment.lookupNamespaceURI(null));
        assertEquals("http://food", vitaminc.lookupNamespaceURI(null));
        assertEquals("http://food", vitamincText.lookupNamespaceURI(null));
    }

    public void testLookupNamespaceURIWithXmlnsPrefix() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupNamespaceURI("xmlns"));
        }
    }

    public void testLookupPrefixWithShadowedUri() {
        assertEquals(null, document.lookupPrefix("http://addons"));
        assertEquals(null, doctype.lookupPrefix("http://addons"));
        if (sp != null) {
            assertEquals(null, sp.lookupPrefix("http://addons"));
        }
        if (png != null) {
            assertEquals(null, png.lookupPrefix("http://addons"));
        }
        assertEquals(null, menu.lookupPrefix("http://addons"));
        assertEquals("a", item.lookupPrefix("http://addons"));
        assertEquals("a", itemXmlns.lookupPrefix("http://addons"));
        assertEquals("a", itemXmlnsA.lookupPrefix("http://addons"));
        assertEquals("a", name.lookupPrefix("http://addons"));
        assertEquals("a", standard.lookupPrefix("http://addons"));
        assertEquals("a", deluxe.lookupPrefix("http://addons"));
        assertEquals("a", description.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText1.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText2.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText3.lookupPrefix("http://addons"));
        assertEquals("a", option1.lookupPrefix("http://addons"));
        assertEquals("a", option2.lookupPrefix("http://addons"));
        assertEquals("a", option2Reference.lookupPrefix("http://addons"));
        assertEquals("a", wafflemaker.lookupPrefix("http://addons"));
        assertEquals("a", nutrition.lookupPrefix("http://addons"));
        assertEquals(null, vitamins.lookupPrefix("http://addons"));
        assertEquals(null, vitaminsXmlnsA.lookupPrefix("http://addons"));
        assertEquals(null, comment.lookupPrefix("http://addons"));
        assertEquals(null, vitaminc.lookupPrefix("http://addons"));
        assertEquals(null, vitamincText.lookupPrefix("http://addons"));
    }

    public void testLookupPrefixWithUnusedUri() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupPrefix("http://unused"));
        }
    }

    public void testLookupPrefixWithNullUri() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupPrefix(null));
        }
    }

    public void testLookupPrefixWithShadowingUri() {
        assertEquals(null, document.lookupPrefix("http://usda"));
        assertEquals(null, doctype.lookupPrefix("http://usda"));
        if (sp != null) {
            assertEquals(null, sp.lookupPrefix("http://usda"));
        }
        if (png != null) {
            assertEquals(null, png.lookupPrefix("http://usda"));
        }
        assertEquals(null, menu.lookupPrefix("http://usda"));
        assertEquals(null, item.lookupPrefix("http://usda"));
        assertEquals(null, itemXmlns.lookupPrefix("http://usda"));
        assertEquals(null, itemXmlnsA.lookupPrefix("http://usda"));
        assertEquals(null, name.lookupPrefix("http://usda"));
        assertEquals(null, standard.lookupPrefix("http://usda"));
        assertEquals(null, deluxe.lookupPrefix("http://usda"));
        assertEquals(null, description.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText1.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText2.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText3.lookupPrefix("http://usda"));
        assertEquals(null, option1.lookupPrefix("http://usda"));
        assertEquals(null, option2.lookupPrefix("http://usda"));
        assertEquals(null, option2Reference.lookupPrefix("http://usda"));
        assertEquals(null, wafflemaker.lookupPrefix("http://usda"));
        assertEquals(null, nutrition.lookupPrefix("http://usda"));
        assertEquals("a", vitamins.lookupPrefix("http://usda"));
        assertEquals("a", vitaminsXmlnsA.lookupPrefix("http://usda"));
        assertEquals("a", comment.lookupPrefix("http://usda"));
        assertEquals("a", vitaminc.lookupPrefix("http://usda"));
        assertEquals("a", vitamincText.lookupPrefix("http://usda"));
    }

    public void testIsDefaultNamespace() {
        assertFalse(document.isDefaultNamespace("http://food"));
        assertFalse(doctype.isDefaultNamespace("http://food"));
        if (sp != null) {
            assertFalse(sp.isDefaultNamespace("http://food"));
        }
        if (png != null) {
            assertFalse(png.isDefaultNamespace("http://food"));
        }
        assertFalse(menu.isDefaultNamespace("http://food"));
        assertTrue(item.isDefaultNamespace("http://food"));
        assertTrue(itemXmlns.isDefaultNamespace("http://food"));
        assertTrue(itemXmlnsA.isDefaultNamespace("http://food"));
        assertTrue(name.isDefaultNamespace("http://food"));
        assertTrue(standard.isDefaultNamespace("http://food"));
        assertTrue(deluxe.isDefaultNamespace("http://food"));
        assertFalse(description.isDefaultNamespace("http://food"));
        assertFalse(descriptionText1.isDefaultNamespace("http://food"));
        assertFalse(descriptionText2.isDefaultNamespace("http://food"));
        assertFalse(descriptionText3.isDefaultNamespace("http://food"));
        assertTrue(option1.isDefaultNamespace("http://food"));
        assertTrue(option2.isDefaultNamespace("http://food"));
        assertTrue(option2Reference.isDefaultNamespace("http://food"));
        assertTrue(wafflemaker.isDefaultNamespace("http://food"));
        assertTrue(nutrition.isDefaultNamespace("http://food"));
        assertTrue(vitamins.isDefaultNamespace("http://food"));
        assertTrue(vitaminsXmlnsA.isDefaultNamespace("http://food"));
        assertTrue(comment.isDefaultNamespace("http://food"));
        assertTrue(vitaminc.isDefaultNamespace("http://food"));
        assertTrue(vitamincText.isDefaultNamespace("http://food"));
    }

    /**
     * Xerces fails this test. It returns false always for entity, notation,
     * document fragment and document type nodes. This contradicts its own
     * behaviour on lookupNamespaceURI(null).
     */
    public void testIsDefaultNamespaceNull_XercesBugs() {
        String message = "isDefaultNamespace() should be consistent with lookupNamespaceURI(null)";
        assertTrue(message, doctype.isDefaultNamespace(null));
        if (sp != null) {
            assertTrue(message, sp.isDefaultNamespace(null));
        }
        if (png != null) {
            assertTrue(message, png.isDefaultNamespace(null));
        }
    }

    public void testIsDefaultNamespaceNull() {
        assertTrue(document.isDefaultNamespace(null));
        assertTrue(menu.isDefaultNamespace(null));
        assertFalse(item.isDefaultNamespace(null));
        assertFalse(itemXmlns.isDefaultNamespace(null));
        assertFalse(itemXmlnsA.isDefaultNamespace(null));
        assertFalse(name.isDefaultNamespace(null));
        assertFalse(standard.isDefaultNamespace(null));
        assertFalse(deluxe.isDefaultNamespace(null));
        assertFalse(description.isDefaultNamespace(null));
        assertFalse(descriptionText1.isDefaultNamespace(null));
        assertFalse(descriptionText2.isDefaultNamespace(null));
        assertFalse(descriptionText3.isDefaultNamespace(null));
        assertFalse(option1.isDefaultNamespace(null));
        assertFalse(option2.isDefaultNamespace(null));
        assertFalse(option2Reference.isDefaultNamespace(null));
        assertFalse(wafflemaker.isDefaultNamespace(null));
        assertFalse(nutrition.isDefaultNamespace(null));
        assertFalse(vitamins.isDefaultNamespace(null));
        assertFalse(vitaminsXmlnsA.isDefaultNamespace(null));
        assertFalse(comment.isDefaultNamespace(null));
        assertFalse(vitaminc.isDefaultNamespace(null));
        assertFalse(vitamincText.isDefaultNamespace(null));
    }

    public void testDoctypeSetTextContent() throws TransformerException {
        String original = domToString(document);
        doctype.setTextContent("foobar"); // strangely, this is specified to no-op
        assertEquals(original, domToString(document));
    }

    public void testDocumentSetTextContent() throws TransformerException {
        String original = domToString(document);
        document.setTextContent("foobar"); // strangely, this is specified to no-op
        assertEquals(original, domToString(document));
    }

    public void testElementSetTextContent() throws TransformerException {
        String original = domToString(document);
        nutrition.setTextContent("foobar");
        String expected = original.replaceFirst(
                "(?s)<nutrition>.*</nutrition>", "<nutrition>foobar</nutrition>");
        assertEquals(expected, domToString(document));
    }

    public void testEntitySetTextContent() throws TransformerException {
        if (sp == null) {
            return;
        }
        try {
            sp.setTextContent("foobar");
            fail(); // is this implementation-specific behaviour?
        } catch (DOMException e) {
        }
    }

    public void testNotationSetTextContent() throws TransformerException {
        if (png == null) {
            return;
        }
        String original = domToString(document);
        png.setTextContent("foobar");
        String expected = original.replace("image/png", "foobar");
        assertEquals(expected, domToString(document));
    }

    /**
     * Tests setTextContent on entity references. Although the other tests can
     * act on a parsed DOM, this needs to use a programmatically constructed DOM
     * because the parser may have replaced the entity reference with the
     * corresponding text.
     */
    public void testEntityReferenceSetTextContent() throws TransformerException {
        document = builder.newDocument();
        Element root = document.createElement("menu");
        document.appendChild(root);

        EntityReference entityReference = document.createEntityReference("sp");
        root.appendChild(entityReference);

        try {
            entityReference.setTextContent("Lite Syrup");
            fail();
        } catch (DOMException e) {
        }
    }

    public void testAttributeSetTextContent() throws TransformerException {
        String original = domToString(document);
        standard.setTextContent("foobar");
        String expected = original.replace("standard=\"strawberry\"", "standard=\"foobar\"");
        assertEquals(expected, domToString(document));
    }

    public void testTextSetTextContent() throws TransformerException {
        String original = domToString(document);
        descriptionText1.setTextContent("foobar");
        String expected = original.replace(">Belgian<!", ">foobar<!");
        assertEquals(expected, domToString(document));
    }

    public void testCdataSetTextContent() throws TransformerException {
        String original = domToString(document);
        descriptionText2.setTextContent("foobar");
        String expected = original.replace(
                " waffles & strawberries (< 5g ", "foobar");
        assertEquals(expected, domToString(document));
    }

    public void testProcessingInstructionSetTextContent() throws TransformerException {
        String original = domToString(document);
        wafflemaker.setTextContent("foobar");
        String expected = original.replace(" square shape?>", " foobar?>");
        assertEquals(expected, domToString(document));
    }

    public void testCommentSetTextContent() throws TransformerException {
        String original = domToString(document);
        comment.setTextContent("foobar");
        String expected = original.replace("-- add other vitamins? --", "--foobar--");
        assertEquals(expected, domToString(document));
    }

    public void testCoreFeature() {
        assertTrue(domImplementation.hasFeature("Core", null));
        assertTrue(domImplementation.hasFeature("Core", ""));
        assertTrue(domImplementation.hasFeature("Core", "1.0"));
        assertTrue(domImplementation.hasFeature("Core", "2.0"));
        assertTrue(domImplementation.hasFeature("Core", "3.0"));
        assertTrue(domImplementation.hasFeature("CORE", "3.0"));
        assertTrue(domImplementation.hasFeature("+Core", "3.0"));
        assertFalse(domImplementation.hasFeature("Core", "4.0"));
    }

    public void testXmlFeature() {
        assertTrue(domImplementation.hasFeature("XML", null));
        assertTrue(domImplementation.hasFeature("XML", ""));
        assertTrue(domImplementation.hasFeature("XML", "1.0"));
        assertTrue(domImplementation.hasFeature("XML", "2.0"));
        assertTrue(domImplementation.hasFeature("XML", "3.0"));
        assertTrue(domImplementation.hasFeature("Xml", "3.0"));
        assertTrue(domImplementation.hasFeature("+XML", "3.0"));
        assertFalse(domImplementation.hasFeature("XML", "4.0"));
    }

    /**
     * The RI fails this test.
     * http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#Document3-version
     */
    public void testXmlVersionFeature() {
        String message = "This implementation does not support the XMLVersion feature";
        assertTrue(message, domImplementation.hasFeature("XMLVersion", null));
        assertTrue(message, domImplementation.hasFeature("XMLVersion", ""));
        assertTrue(message, domImplementation.hasFeature("XMLVersion", "1.0"));
        assertTrue(message, domImplementation.hasFeature("XMLVersion", "1.1"));
        assertTrue(message, domImplementation.hasFeature("XMLVERSION", "1.1"));
        assertTrue(message, domImplementation.hasFeature("+XMLVersion", "1.1"));
        assertFalse(domImplementation.hasFeature("XMLVersion", "1.2"));
        assertFalse(domImplementation.hasFeature("XMLVersion", "2.0"));
        assertFalse(domImplementation.hasFeature("XMLVersion", "2.0"));
    }

    public void testLsFeature() {
        assertTrue("This implementation does not support the LS feature",
                domImplementation.hasFeature("LS", "3.0"));
    }

    public void testElementTraversalFeature() {
        assertTrue("This implementation does not support the ElementTraversal feature",
                domImplementation.hasFeature("ElementTraversal", "1.0"));
    }

    public void testIsSupported() {
        // we don't independently test the features; instead just assume the
        // implementation calls through to hasFeature (as tested above)
        for (Node node : allNodes) {
            assertTrue(node.isSupported("XML", null));
            assertTrue(node.isSupported("XML", "3.0"));
            assertFalse(node.isSupported("foo", null));
            assertFalse(node.isSupported("foo", "bar"));
        }
    }

    public void testGetFeature() {
        // we don't independently test the features; instead just assume the
        // implementation calls through to hasFeature (as tested above)
        for (Node node : allNodes) {
            assertSame(node, node.getFeature("XML", null));
            assertSame(node, node.getFeature("XML", "3.0"));
            assertNull(node.getFeature("foo", null));
            assertNull(node.getFeature("foo", "bar"));
        }
    }

    public void testNodeEqualsPositive() throws Exception {
        DomTest copy = new DomTest();
        copy.setUp();
        
        for (int i = 0; i < allNodes.size(); i++) {
            Node a = allNodes.get(i);
            Node b = copy.allNodes.get(i);
            assertTrue(a.isEqualNode(b));
        }
    }

    public void testNodeEqualsNegative() throws Exception {
        for (Node a : allNodes) {
            for (Node b : allNodes) {
                assertEquals(a == b, a.isEqualNode(b));
            }
        }
    }

    public void testNodeEqualsNegativeRecursive() throws Exception {
        DomTest copy = new DomTest();
        copy.setUp();
        copy.vitaminc.setTextContent("55%");

        // changing anything about a node should break equality for all parents
        assertFalse(document.isEqualNode(copy.document));
        assertFalse(menu.isEqualNode(copy.menu));
        assertFalse(item.isEqualNode(copy.item));
        assertFalse(nutrition.isEqualNode(copy.nutrition));
        assertFalse(vitamins.isEqualNode(copy.vitamins));
        assertFalse(vitaminc.isEqualNode(copy.vitaminc));

        // but not siblings
        assertTrue(doctype.isEqualNode(copy.doctype));
        assertTrue(description.isEqualNode(copy.description));
        assertTrue(option1.isEqualNode(copy.option1));
    }

    public void testNodeEqualsNull() {
        for (Node node : allNodes) {
            try {
                node.isEqualNode(null);
                fail();
            } catch (NullPointerException e) {
            }
        }
    }

    public void testIsElementContentWhitespaceWithoutDeclaration() throws Exception {
        String xml = "<menu>    <item/>   </menu>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Text text = (Text) factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement().getChildNodes().item(0);
        assertFalse(text.isElementContentWhitespace());
    }

    public void testIsElementContentWhitespaceWithDeclaration() throws Exception {
        String xml = "<!DOCTYPE menu [\n"
                + "  <!ELEMENT menu (item)*>\n"
                + "  <!ELEMENT item (#PCDATA)>\n"
                + "]><menu>    <item/>   </menu>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Text text = (Text) factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement().getChildNodes().item(0);
        assertTrue("This implementation does not recognize element content whitespace",
                text.isElementContentWhitespace());
    }

    public void testGetWholeTextFirst() {
        assertEquals("Belgian waffles & strawberries (< 5g of fat)",
                descriptionText1.getWholeText());
    }

    public void testGetWholeTextMiddle() {
        assertEquals("This implementation doesn't include preceding nodes in getWholeText()",
                "Belgian waffles & strawberries (< 5g of fat)", descriptionText2.getWholeText());
    }

    public void testGetWholeTextLast() {
        assertEquals("This implementation doesn't include preceding nodes in getWholeText()",
                "Belgian waffles & strawberries (< 5g of fat)", descriptionText3.getWholeText());
    }

    public void testGetWholeTextOnly() {
        assertEquals("60%", vitamincText.getWholeText());
    }

    public void testGetWholeTextWithEntityReference() {
        EntityReference spReference = document.createEntityReference("sp");
        description.insertBefore(spReference, descriptionText2);

        assertEquals("This implementation doesn't resolve entity references in getWholeText()",
                "BelgianMaple Syrup waffles & strawberries (< 5g of fat)",
                descriptionText1.getWholeText());
    }

    public void testReplaceWholeTextFirst() throws TransformerException {
        String original = domToString(document);
        Text replacement = descriptionText1.replaceWholeText("Eggos");
        assertSame(descriptionText1, replacement);
        String expected = original.replace(
                "Belgian<![CDATA[ waffles & strawberries (< 5g ]]>of fat)", "Eggos");
        assertEquals(expected, domToString(document));
    }

    public void testReplaceWholeTextMiddle() throws TransformerException {
        String original = domToString(document);
        Text replacement = descriptionText2.replaceWholeText("Eggos");
        assertSame(descriptionText2, replacement);
        String expected = original.replace(
                "Belgian<![CDATA[ waffles & strawberries (< 5g ]]>of fat)", "<![CDATA[Eggos]]>");
        assertEquals("This implementation doesn't remove preceding nodes in replaceWholeText()",
                expected, domToString(document));
    }

    public void testReplaceWholeTextLast() throws TransformerException {
        String original = domToString(document);
        Text replacement = descriptionText3.replaceWholeText("Eggos");
        assertSame(descriptionText3, replacement);
        String expected = original.replace(
                "Belgian<![CDATA[ waffles & strawberries (< 5g ]]>of fat)", "Eggos");
        assertEquals("This implementation doesn't remove preceding nodes in replaceWholeText()",
                expected, domToString(document));
    }

    public void testReplaceWholeTextOnly() throws TransformerException {
        String original = domToString(document);
        Text replacement = vitamincText.replaceWholeText("70%");
        assertEquals(Node.TEXT_NODE, replacement.getNodeType());
        assertSame(vitamincText, replacement);
        String expected = original.replace("60%", "70%");
        assertEquals(expected, domToString(document));
    }

    public void testReplaceWholeTextFirstWithNull() throws TransformerException {
        String original = domToString(document);
        assertNull(descriptionText1.replaceWholeText(null));
        String expected = original.replaceFirst(">.*</description>", "/>");
        assertEquals("This implementation doesn't remove adjacent nodes in replaceWholeText(null)",
                expected, domToString(document));
    }

    public void testReplaceWholeTextMiddleWithNull() throws TransformerException {
        String original = domToString(document);
        assertNull(descriptionText2.replaceWholeText(null));
        String expected = original.replaceFirst(">.*</description>", "/>");
        assertEquals("This implementation doesn't remove adjacent nodes in replaceWholeText(null)",
                expected, domToString(document));
    }

    public void testReplaceWholeTextLastWithNull() throws TransformerException {
        String original = domToString(document);
        assertNull(descriptionText3.replaceWholeText(null));
        String expected = original.replaceFirst(">.*</description>", "/>");
        assertEquals("This implementation doesn't remove adjacent nodes in replaceWholeText(null)",
                expected, domToString(document));
    }

    public void testReplaceWholeTextFirstWithEmptyString() throws TransformerException {
        String original = domToString(document);
        assertNull(descriptionText1.replaceWholeText(""));
        String expected = original.replaceFirst(">.*</description>", "/>");
        assertEquals("This implementation doesn't remove adjacent nodes in replaceWholeText(null)",
                expected, domToString(document));
    }

    public void testReplaceWholeTextOnlyWithEmptyString() throws TransformerException {
        String original = domToString(document);
        assertNull(vitamincText.replaceWholeText(""));
        String expected = original.replaceFirst(">.*</a:vitaminc>", "/>");
        assertEquals(expected, domToString(document));
    }

    public void testUserDataAttachments() {
        Object a = new Object();
        Object b = new Object();
        for (Node node : allNodes) {
            node.setUserData("a", a, null);
            node.setUserData("b", b, null);
        }
        for (Node node : allNodes) {
            assertSame(a, node.getUserData("a"));
            assertSame(b, node.getUserData("b"));
            assertEquals(null, node.getUserData("c"));
            assertEquals(null, node.getUserData("A"));
        }
    }

    public void testUserDataRejectsNullKey() {
        try {
            menu.setUserData(null, "apple", null);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            menu.getUserData(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    /**
     * A shallow clone requires cloning the attributes but not the child nodes.
     */
    public void testUserDataHandlerNotifiedOfShallowClones() {
        RecordingHandler handler = new RecordingHandler();
        name.setUserData("a", "apple", handler);
        name.setUserData("b", "banana", handler);
        standard.setUserData("c", "cat", handler);
        waffles.setUserData("d", "dog", handler);

        Element clonedName = (Element) name.cloneNode(false);
        Attr clonedStandard = clonedName.getAttributeNode("a:standard");

        Set<String> expected = new HashSet<String>();
        expected.add(notification(NODE_CLONED, "a", "apple", name, clonedName));
        expected.add(notification(NODE_CLONED, "b", "banana", name, clonedName));
        expected.add(notification(NODE_CLONED, "c", "cat", standard, clonedStandard));
        assertEquals(expected, handler.calls);
    }

    /**
     * A deep clone requires cloning both the attributes and the child nodes.
     */
    public void testUserDataHandlerNotifiedOfDeepClones() {
        RecordingHandler handler = new RecordingHandler();
        name.setUserData("a", "apple", handler);
        name.setUserData("b", "banana", handler);
        standard.setUserData("c", "cat", handler);
        waffles.setUserData("d", "dog", handler);

        Element clonedName = (Element) name.cloneNode(true);
        Attr clonedStandard = clonedName.getAttributeNode("a:standard");
        Text clonedWaffles = (Text) clonedName.getChildNodes().item(0);

        Set<String> expected = new HashSet<String>();
        expected.add(notification(NODE_CLONED, "a", "apple", name, clonedName));
        expected.add(notification(NODE_CLONED, "b", "banana", name, clonedName));
        expected.add(notification(NODE_CLONED, "c", "cat", standard, clonedStandard));
        expected.add(notification(NODE_CLONED, "d", "dog", waffles, clonedWaffles));
        assertEquals(expected, handler.calls);
    }

    private class RecordingHandler implements UserDataHandler {
        final Set<String> calls = new HashSet<String>();
        public void handle(short operation, String key, Object data, Node src, Node dst) {
            calls.add(notification(operation, key, data, src, dst));
        }
    }

    private String notification(short operation, String key, Object data, Node src, Node dst) {
        return "op:" + operation + " key:" + key + " data:" + data + " src:" + src + " dst:" + dst;
    }

    private String domToString(Document document) throws TransformerException {
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
