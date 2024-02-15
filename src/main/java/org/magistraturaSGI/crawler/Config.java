package org.magistraturaSGI.crawler;

import lombok.Getter;
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


@Getter
public class Config {

    private int threadCount = 1;
    private int deathTimer = 10;
    private final List<Site> siteList = new ArrayList<Site>();

    @SneakyThrows
    public Config() {
        loadFromFile();
    }

    public Config(int threads, int timer, List<Site> sites) {
        threadCount = threads;
        deathTimer = timer;
        siteList.addAll(sites);
    }

    private void loadFromFile()
            throws URISyntaxException, ParserConfigurationException, IOException, SAXException {
        // opens the config.xml file
        URL path = this.getClass().getClassLoader().getResource("config.xml");
        assert path != null;
        File file = new File(path.toURI());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Element root = doc.getDocumentElement();
        root.normalize();

        getAllChildElements(root);
//        System.out.println("Threads: " + ThreadCount);
//        System.out.println("Timer: " + DeathTimer);
    }

    /***
     * Function that recursively searches all child nodes of the DOM tree
     * of the config.xml file for the defined application settings
     * @param node - the starting node of the search
     */
    private void getAllChildElements(Node node) {

        if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {

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
                    siteList.add(new Site(siteName,siteURL,false));
                    break;
            }

            NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);
                getAllChildElements(childNode);
            }
        }
    }


}


