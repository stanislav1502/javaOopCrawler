package crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.magistraturaSGI.crawler.Config;
import org.magistraturaSGI.crawler.Crawler;
import org.magistraturaSGI.crawler.MenuController;
import org.magistraturaSGI.crawler.dataobjects.Site;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class MenuControllerTest {

    @Mock
    private Crawler crawler;
    @Mock
    private Config config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startCrawlerTest() {

        MenuController.startCrawler();

        verify(crawler, times(1)).addSitesToSearch();
        verify(crawler, times(1)).run();
    }

    @Test
    void viewConfigShouldUpdateConfig() {

        when(crawler.getConfig()).thenReturn(config);
        InputStream inputStream = new ByteArrayInputStream("1\n5\n0\n".getBytes());
        System.setIn(inputStream);
        MenuController.viewConfig();

        verify(config, times(1)).setThreadCount(5);
        verify(config, times(1)).setDeathTimer(5);
    }

    @Test
    void viewSitesShouldToggleSearchedStatus()  {
        when(crawler.getConfig()).thenReturn(config);
        when(config.getSiteList()).thenReturn(List.of(new Site("Test Site", "www.test.com", false)));
        InputStream inputStream = new ByteArrayInputStream("1\n0\n".getBytes());
        System.setIn(inputStream);
        MenuController.viewSites();

        assertTrue(config.getSiteList().getFirst().isSearched());
    }


}