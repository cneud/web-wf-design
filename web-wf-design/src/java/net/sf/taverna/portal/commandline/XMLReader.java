/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal.commandline;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian
 */
public class XMLReader {
    public static Document readFile(File file) throws ParserConfigurationException, SAXException, IOException{
        //Create instance of DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        DocumentBuilder parser = factory.newDocumentBuilder();
        //Create blank DOM Document
        Document doc = parser.parse(file);
        return doc;
    }
    
     public static Document readFile(String uri) throws ParserConfigurationException, SAXException, IOException{
        //Create instance of DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        DocumentBuilder parser = factory.newDocumentBuilder();
        //Create blank DOM Document
        System.out.println("parsing");
        Document doc = parser.parse(uri);
        System.out.println("parsed");
        return doc;
    }
     
    private static void printTab(int depth){
        for (int i = 0; i < depth; i++){
            System.out.print("  ");
        }
    }
    
    private static void showNode(Node node, int depth) throws SAXException{
        //system.out.println(node.getNodeType());
        NodeList children;
        switch (node.getNodeType()){
            case Node.ATTRIBUTE_NODE:
                printTab(depth);
                Attr attr = (Attr)node;
                System.out.println(attr.getName() + ":" + attr.getValue());
                break;
            case Node.ELEMENT_NODE:
                printTab(depth);
                System.out.print("<" + node.getNodeName() + ">");
                children = node.getChildNodes();
                if (children.getLength() == 1 && children.item(0).getNodeType()==Node.TEXT_NODE){
                    System.out.println(children.item(0).getNodeValue());
                } else {
                    System.out.println();
                }
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++){
                    showNode(attributes.item(i), depth+1);
                }
                if (children.getLength() != 1 || children.item(0).getNodeType()!=Node.TEXT_NODE){
                    for (int i = 0; i< children.getLength(); i++){
                        showNode(children.item(i),depth+1);
                    }
                }
                break;
            case Node.TEXT_NODE:
                String text = node.getNodeValue();
                if (!text.trim().isEmpty()){
                    printTab(depth);
                    System.out.print(node);
                    System.out.print(":");
                    System.out.print(text);
                    System.out.println(":");
                }
                break;
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
                printTab(depth);
                System.out.println(node);
                children = node.getChildNodes();
                for (int i = 0; i< children.getLength(); i++){
                    showNode(children.item(i),depth+1);
                }
                break;
            default:
                throw new SAXException ("Unexpected node type: " + node.getNodeType() + " in showNode");
        }
    }
    
    public static List<Node> getDirectChildrenByName(Node parent, String name){
        if (name == null){
            throw new NullPointerException("Illegal call with null name");
        }
        ArrayList<Node> found = new ArrayList<Node>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i< children.getLength(); i++){
            if (children.item(i).getNodeName().equals(name)){
                found.add(children.item(i));
            }
        }        
        return found;
    }
    
    public static Element getDirectOnlyChildrenByName(Node parent, String name) throws SAXException{
        if (name == null){
            throw new NullPointerException("Illegal call with null name");
        }
        Element found = null;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i< children.getLength(); i++){
            if (children.item(i).getNodeName().equals(name)){
                if (found != null){
                    throw new SAXException ("Found more than one child "+ name);
                }
                found = (Element)children.item(i);
            }
        }   
        if (found == null){
            throw new SAXException ("Did not find child "+ name + " in " + parent);            
        }
        return found;
    }

    public static String getTextFromTree(Document document, String[] tree) throws SAXException{
        Element element = document.getDocumentElement();
        List<Node> childrenList;
        for (String aTree : tree) {
            childrenList = getDirectChildrenByName(element, aTree);
            if (childrenList.size() == 0) {
                showNode(element, 0);
                throw new SAXException("No element of name " + aTree + " found in path ");
            }
            if (childrenList.size() > 1) {
                showNode(element, 0);
                throw new SAXException("More than one element of name " + aTree + "found in path ");
            }
            element = (Element) childrenList.get(0);
        }
        return getText(element);
    }
    
    public static String getText(Node element) throws SAXException{
        NodeList children = element.getChildNodes();
        if (children.getLength() == 0){
            return null;
        }
        if (children.getLength() > 1){
               throw new SAXException("Element " + element + " has more than one child.");
        }
        Node child = children.item(0);
        if (child.getNodeType()!=Node.TEXT_NODE){
            throw new SAXException("Element " + element + " has a none text child.");
        }
        return child.getNodeValue();
    }
    
    public static List<String> getTextsFromTree( List<Node> nodeList, List<String> tree) throws SAXException{
        List<Node> children = new ArrayList<Node>();
        for (Node aNodeList : nodeList) {
            List<Node> newChildren = getDirectChildrenByName(aNodeList, tree.get(0));
            children.addAll(newChildren);
        }
        if (tree.size() == 1){
            List<String> results = new ArrayList<String>();
            //system.out.println("****");
            //system.out.println(children);
            for (Node aChildren : children) {
                results.add(getText(aChildren));
            }
            return results;
        } else {
            return getTextsFromTree(children, tree.subList(1, tree.size()));
        }
    }

    public static List<String> getTextsFromTree(Document document, List<String> tree) throws SAXException{
        List<Node> children = new ArrayList<Node>();
        children.add(document.getDocumentElement());
        return  getTextsFromTree(children, tree);
    }
    
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        File file = new File("D:\\taverna\\Echo.t2flow");
//        File file = new File("D:\\taverna\\output.xml");
        Document test = readFile(file);
        //trimEmptyTextNodes(test);
        showNode(test,0);
        NodeList inputPorts = test.getElementsByTagName("inputPorts");
        System.out.println(inputPorts.getLength());
    }

}
