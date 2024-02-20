package crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magistraturaSGI.crawler.Config;
import org.magistraturaSGI.crawler.dataobjects.Site;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    private Config config;

    @BeforeEach
    void before() {
        config = new Config();
    }

    @Test
    void defaultConstructorLoadFromFile() {
        assertNotNull(config.getSiteList());
        assertFalse(config.getSiteList().isEmpty());
        assertEquals(1, config.getThreadCount());
        assertEquals(10, config.getDeathTimer());
    }

    @Test
    void parameterizedConstructorSetValues() {
        List<Site> sites = List.of(new Site("ExampleSite", "http://example.com", true));
        Config customConfig = new Config(3, 15, sites);

        assertEquals(3, customConfig.getThreadCount());
        assertEquals(15, customConfig.getDeathTimer());
        assertEquals(sites, customConfig.getSiteList());
    }

    @Test
    void loadFromFileShouldPopulateConfig() {
        Config customConfig = new Config();

        assertNotNull(customConfig.getSiteList());
        assertFalse(customConfig.getSiteList().isEmpty());
        assertTrue(customConfig.getThreadCount() > 0);
        assertTrue(customConfig.getDeathTimer() > 0);
    }
}