package crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magistraturaSGI.crawler.Crawler;
import org.magistraturaSGI.crawler.dataobjects.Site;

import static org.junit.jupiter.api.Assertions.*;


public class CrawlerTest {

    private Crawler crawler;
    @BeforeEach
    void before() {
        crawler = new Crawler();
        crawler.addSitesToSearch();

    }

    @Test
    void crawlerShouldLoadWithDefaultConfig() {
        assertNotNull(crawler.getConfig());
        assertEquals(1, crawler.getConfig().getThreadCount());
        assertEquals(10, crawler.getConfig().getDeathTimer());
    }

    @Test
    void addSitesToSearchShouldPopulateQueue() {
        assertNotNull(crawler.getSitesForSearching());
        assertFalse(crawler.getSitesForSearching().isEmpty());
    }

    @Test
    void setSiteSelectorsTest() {
        Site jobsBgSite = new Site("JOBS.BG", "https://www.jobs.bg", true);
        crawler.setSiteSelectors(jobsBgSite);

        assertEquals("h2[class*=\"job-view-title\"]", crawler.getJobTitleSelector());
        assertEquals("a[href^=\"https://www.jobs.bg/job/\"]", crawler.getJobPageSelector());
        assertEquals("", crawler.getNextPageSelector());
    }

    @Test
    void runShouldExecuteWithoutExceptions() {
        assertDoesNotThrow(() -> crawler.run());
    }


}