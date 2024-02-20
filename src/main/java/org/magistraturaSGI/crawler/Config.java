
package org.magistraturaSGI.crawler;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.magistraturaSGI.crawler.dataobjects.Site;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for the crawler, with getters and setters for various settings.
 */
@Getter
public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());

    @Setter
    private int threadCount = 1;
    @Setter
    private int deathTimer = 10;

    private final List<Site> siteList = new ArrayList<>();

    /**
     * Default constructor for Config, loads configuration settings from the config.xml file.
     */
    @SneakyThrows
    public Config() {
        loadFromFile();
    }

    /**
     * Parameterized Config constructor for custom starting settings.
     *
     * @param threads The number of threads.
     * @param timer   The time each thread lives for.
     * @param sites   The sites to be searched.
     */
    public Config(int threads, int timer, List<Site> sites) {
        threadCount = threads;
        deathTimer = timer;
        siteList.addAll(sites);
    }

    /***
     * Finds the config.xml file located in the resources folder and loads the settings for the crawler
     */
    private void loadFromFile() {
        try {
            // Find the "config.xml" file and load it
            URL path = this.getClass().getClassLoader().getResource("config.xml");
            if (path == null) {
                logger.log(Level.SEVERE, "Could not find config.xml file.");
                return;
            }
            File file = new File(path.toURI());

            // Create a DocumentBuilder and parse the XML file
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            // Normalize the XML document
            Element root = doc.getDocumentElement();
            root.normalize();
            getAllChildElements(root);
            logger.log(Level.INFO,"Loaded the default configuration from file {0}",file.getName());
        } catch (URISyntaxException | ParserConfigurationException | IOException | SAXException e) {
            logger.log(Level.SEVERE, "Error loading config.xml file.", e);
        }
    }

    /**
     * Recursively traverses all child nodes of the provided DOM tree node and extracts relevant application settings.
     * If the node is an ELEMENT_NODE and has child nodes, it processes the node's name and value.
     * If the name matches predefined settings, updates the corresponding configuration values.
     * For "DeathTimer" and "ThreadCount," it parses the value to an integer.
     * For "Site," extracts child elements "Name" and "URL" to create a new Site object and adds it to the siteList.
     *
     * @param node The starting node of the DOM tree traversal.
     */
    private void getAllChildElements(Node node) {
        try {
            if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
                // Extract node name and value and process them
                String name = node.getNodeName();
                String value = node.getTextContent();
                switch (name) {
                    case "DeathTimer":
                        deathTimer = Integer.parseInt(value);
                        break;
                    case "ThreadCount":
                        threadCount = Integer.parseInt(value);
                        break;
                    case "Site":
                        Element element = (Element) node;
                        String siteName = element.getElementsByTagName("Name").item(0).getTextContent();
                        String siteURL = element.getElementsByTagName("URL").item(0).getTextContent();
                        siteList.add(new Site(siteName, siteURL, true));
                        break;
                }
                // Get the child nodes of the current node
                NodeList children = node.getChildNodes();
                // Recursively process each child node
                for (int i = 0; i < children.getLength(); i++) {
                    Node childNode = children.item(i);
                    getAllChildElements(childNode);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing XML node: " + node.getNodeName(), e);
        }
    }
}