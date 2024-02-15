package crawler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.magistraturaSGI.crawler.Config;
import org.magistraturaSGI.crawler.Crawler;
import org.magistraturaSGI.crawler.dataobjects.Site;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CrawlerTest {

    @Test
    public void LoadConfig() {
        Config config = new Config();
        System.out.println(config.toString());
    }

    @Test
    public void StartCrawler() {
        Crawler crawler = new Crawler();
        crawler.Start();
    }

    @Test
    public void ViewSites() {
        List<Site> sites = new ArrayList<>();

        Site site1 = new Site("Test Site 1", "www.test.com", false);
        sites.add(site1);
        Site site2 = new Site("TestSite 2", "www.test2.org", true);
        sites.add(site2);

        Config config = new Config(0, 5, sites);

        assertEquals(site1.getUrl(), config.getSiteList().get(0).getUrl());
        assertEquals(site2.getUrl(), config.getSiteList().get(1).getUrl());
    }

    @Test
    public void ViewConfig() {
        Site site = new Site("Test Site 1", "www.test.com", false);
        List<Site> sites = new ArrayList<>();
        sites.add(site);

        Config config = new Config(0, 5, sites);
        Crawler crawler = new Crawler(config);

        assertEquals(config, crawler.getConfig());
    }



}