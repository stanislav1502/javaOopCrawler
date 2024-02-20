package org.magistraturaSGI.crawler;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.magistraturaSGI.crawler.dataobjects.JobListing;
import org.magistraturaSGI.crawler.dataobjects.Site;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crawler class responsible for fetching job listings from various job search sites.
 */
@Getter
public class Crawler implements Runnable {
    private static final Logger logger = Logger.getLogger(Crawler.class.getName());
    private final Config config; // Configuration settings for the crawler
    private  final List<JobListing> jobListings = new LinkedList<>(); // List to store job listings
    private  final ConcurrentLinkedDeque<Site> sitesForSearching = new ConcurrentLinkedDeque<>(); // Queue of sites to be searched
    private final ReentrantLock lock = new ReentrantLock();

    private String jobTitleSelector = "";
    private String jobPageSelector = "";
    private String nextPageSelector = "";

    /**
     * Default constructor initializing the crawler with default configuration settings.
     */
    public Crawler() {
        config = new Config();
    }

    /**
     * Queues the sites to be searched from the configuration settings.
     */
    public void addSitesToSearch() {
        lock.lock();
        try {
            for (Site site : config.getSiteList()) {
                if (site.isSearched()) {
                    sitesForSearching.push(site);
                    logger.log(Level.INFO, "Added site to search queue: {0}", site.getUrl());
                }
            }
        } finally {
            logger.log(Level.INFO,"Loaded sites to search from the config.");
            lock.unlock();
        }
    }

    /**
     * Sets selectors based on the provided site, specifying how to extract job titles, job page links, and next page links.
     *
     * @param site The site for which selectors are being set.
     */
    public void setSiteSelectors(Site site) {
        switch (site.getName()) {
            case "JOBS.BG":
                jobTitleSelector = "h2[class*=job-view-title]";
                jobPageSelector = "a[href^=https://www.jobs.bg/job/]";
                nextPageSelector = "";
                break;
            case "OLX":
                jobTitleSelector = "h1[class*=css-tcqyb]";
                jobPageSelector = "a[href^=https://www.olx.bg/ad/job/]";
                nextPageSelector = "li > a[href*=/rabota/?page=";
                break;
            case "Yox":
                jobTitleSelector = "h1[data-job-component*=title]";
                jobPageSelector = "a[href^=https://yox.bg/jobs/]";
                nextPageSelector = "a[href*=/search?o=]";
                break;
            case "RabotniMesta":
                jobTitleSelector = "h3[class*=title]";
                jobPageSelector = "a[href*=/обява/]";
                nextPageSelector = "a[href*=/работа/?&p=]";
                break;
        }
    }

    /**
     * Finds and adds links to other pages in the queue of sites for searching.
     * @param site The site from which to find page links.
     */
    public void findPageLinks(Site site) {
        lock.lock();
        try {
            if (site.isSearched()) {
                setSiteSelectors(site);
                Document document;
                Elements pageLinks;
                try {
                    document = Jsoup.connect(site.getUrl()).userAgent("Mozilla 5.0").get();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error connecting to site: " + site.getUrl(), e);
                    return;
                }
                try {
                    pageLinks = document.select(nextPageSelector);
                } catch (Selector.SelectorParseException e) {
                    logger.log(Level.WARNING, "Error parsing selector on site: " + site.getUrl(), e);
                    return;
                }
                // add all links to other pages
                for (Element link : pageLinks) {
                    Site newSite = new Site(site.getName(), link.attr("abs:href"), true);
                    if (!sitesForSearching.contains(newSite)) {
                        sitesForSearching.push(newSite);
                        logger.log(Level.INFO, "Added site for searching: {0}", newSite.getUrl());
                    }
                }
                site.setSearched(false);
                logger.log(Level.INFO,"Finished searching site: {0}", site.getUrl());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finds and adds job listings from the site queue to the jobs list.
     */
    public void findJobListings() {
        lock.lock();
        try {
            Site site;
            Document document;
            do {
                site = sitesForSearching.poll();
                if (site != null) {
                    try {
                        document = Jsoup.connect(site.getUrl()).userAgent("Mozilla 5.0").get();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error connecting to site: " + site.getUrl(), e);
                        return;
                    }
                    Elements jobLinks;
                    try {
                        jobLinks = document.select(jobPageSelector);
                    } catch (Selector.SelectorParseException e) {
                        logger.log(Level.WARNING, "Error parsing selector on site: " + site.getUrl(), e);
                        return;
                    }
                    // add all links to other pages
                    for (Element link : jobLinks) {
                        try {
                            document = Jsoup.connect(link.attr("abs:href")).get();
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Error connecting to site: " + site.getUrl(), e);
                            return;
                        }
                        String title;
                        try {
                            title = document.select(jobTitleSelector).text();
                        } catch (Selector.SelectorParseException e) {
                            logger.log(Level.WARNING, "Error parsing selector on site: " + site.getUrl(), e);
                            return;
                        }
                        JobListing newListing = new JobListing(title, link.attr("abs:href"));
                        jobListings.add(newListing);
                        logger.log(Level.INFO, "Added job listing: {0}", newListing.getUrl());
                    }
                }
            } while (sitesForSearching.isEmpty());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Continuously waits for a random period of time, then searches for page links and fetches job listings
     * until all sites in the queue are searched.
     * While there are sites in the queue.
     */
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        try {
            Random random = new Random();
            while (!sitesForSearching.isEmpty()) {
                // Wait for a random period to prevent sites from IP blocking
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Thread sleep interrupted", e);
                    Thread.currentThread().interrupt();
                }
                // Iterate through sites in the queue and find page links
                for (Site site : sitesForSearching){
                    findPageLinks(site);
                    logger.log(Level.INFO, "Found page links for site: {0}", site.getUrl());
                }
            }
            // Fetch all job listings from the sites in the queue
            findJobListings();
            logger.log(Level.INFO, "Finished processing job listings.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
        }
    }
}