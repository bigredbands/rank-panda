package org.bigredbands.mb.controllers;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bigredbands.mb.models.DrillInfo;
import org.w3c.dom.Document;

/**
 * 
 * Generates XML file to save information from current drillInfo in program.
 *
 */
public class XMLGenerator {

    /**
     * Default constructor.
     */
    public XMLGenerator() {

    }

    /**
     * Saves drill information into XML file that can later be accessed
     * and edited in RankPanda 2.0.
     * @param drillInfo - contains information from currently written drill.
     * @param file - file where the drill information will be saved.
     */
    public void save(DrillInfo drillInfo, File file) {
        try {
            // create the document builder
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // create the document
            Document document = docBuilder.newDocument();
            
            //recursively create the xml file
            document.appendChild(drillInfo.convertToXML(document));
            
            // From:  http://stackoverflow.com/questions/8865099/xml-file-generator-in-java
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file.getAbsolutePath());

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
            
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
