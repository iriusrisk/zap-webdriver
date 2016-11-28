package net.continuumsecurity;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;


public class Config {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Config.class.getName());

    private File XmlFile = new File("Config.xml");
    private static Document doc;

    public Config() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(XmlFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            log.info(e);
        }
    }

    public String GetNodeByName(String nodeName) {
        try {
            return doc.getElementsByTagName(nodeName).item(0).getTextContent();
        }
        catch (Exception e){
            log.info(String.format("Can't find node by nodeName: [%1$]", nodeName));
            return null;
        }
    }
}