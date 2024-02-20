package org.magistraturaSGI.crawler;

import org.magistraturaSGI.crawler.dataobjects.JobListing;
import org.magistraturaSGI.crawler.dataobjects.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for managing the menu and user interactions in the Crawler application.
 */
public class MenuController {
    private static final Logger logger = Logger.getLogger(MenuController.class.getName());
    private static String menu = ""; // String to store the menu text
    private static final Crawler crawler = new Crawler(); // Crawler instance for fetching job listings

    /**
     * Main method to run the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args)  {
        logger.log(Level.INFO,"Program started.");
        char key;
        System.out.println("\nNavigate through menus by typing the corresponding option number and pressing enter.\n");
        do {
            System.out.flush();
            System.out.print("\033[H\033[2J");

            setStartMenuText();
            System.out.println(menu);

            try {
                key = (char) System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (run(key));
    }

    /**
     * Reads which menu item was selected and takes the appropriate action.
     * If 0 was inputted, returns false and terminates the program.
     * @param c The key pressed.
     * @return Default true.
     */
    private static boolean run(char c) {
        switch (c) {
            case '0': return false;
            case '1': startCrawler(); break;
            case '2': viewConfig(); break;
            case '3': viewSites(); break;
            case '4': viewJobs(); break;
        }
        return true;
    }

    /**
     * Starts the crawler to fetch job listings from configured sites.
     */
    public static void startCrawler() {
        logger.log(Level.INFO,"User selected menu option - Start Crawler");
        try {
            // Number of threads to run (adjust as needed)
            int numberOfThreads = crawler.getConfig().getThreadCount();
            // Create and start multiple threads
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                Thread thread = new Thread(crawler);
                thread.start();
                threads.add(thread);
                logger.log(Level.INFO, "Started Crawler thread {0}", i + 1);
            }
            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                    logger.log(Level.INFO, "Crawler thread {0} finished", thread.threadId());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error waiting for Crawler thread to finish", e);
                }
            }
            logger.log(Level.INFO, "All Crawler threads have finished.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred in startCrawler", e);
        }
    }

    /**
     * Displays and allows modification of configuration settings.
     */
    public static void viewConfig() {
        logger.log(Level.INFO,"User selected menu option - View Config ");
        char key;
        do {
            System.out.flush();
            System.out.print("\033[H\033[2J");

            Config config = crawler.getConfig();
            setConfigMenuText(config);
            try {
                key = (char) System.in.read();
                switch (key) {
                    case '1': {
                        System.out.println("ThreadCount = ");
                        int n = System.in.read();
                        config.setThreadCount(n);
                        logger.log(Level.INFO,"ThreadCount set to {0}",n);
                    }
                    break;
                    case '2': {
                        System.out.println("DeathTimer = ");
                        int t = System.in.read();
                        config.setDeathTimer(t);
                        logger.log(Level.INFO,"DeathTimer set to {0}",t);
                    }
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (key != '0');
        logger.log(Level.INFO,"Exited View Config menu");
    }

    /**
     * Displays sites to be searched and allows for choosing which.
     */
    public static void viewSites() {
        logger.log(Level.INFO,"User selected menu option - View Sites");
        char key;
        do {
            System.out.flush();
            System.out.print("\033[H\033[2J");

            var sites = crawler.getConfig().getSiteList();
            setSitesMenuText(sites);
            System.out.println(menu);

            try {
                key = (char) System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int index = Integer.parseInt(String.valueOf(key)) - 1;
            if (0 <= index && index < sites.size() )
            {
                Site site = sites.get(index);
                site.setSearched(!site.isSearched());
                sites.set(index,site);
            }
        } while (key != '0');
        logger.log(Level.INFO,"Exited View Sites menu");
    }
    public static void viewJobs() {
        logger.log(Level.INFO, "User selected menu option - View Jobs");
        char key;
        boolean showAll = false;
        do {
            System.out.flush();
            System.out.print("\033[H\033[2J");

            var jobs = crawler.getJobListings();
            setJobsMenuText(jobs, showAll);
            System.out.println(menu);

            try {
                key = (char) System.in.read();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading user input.", e);
                throw new RuntimeException(e);
            }
            if (key == '1') {
                showAll = !showAll;
            }
        } while (key != '0');
        logger.log(Level.INFO,"Exited View Jobs menu");
    }

    /**
     * Sets the text for the start menu.
     */
    private static void setStartMenuText(){
        menu  = ("\nCrawler Menu");
        menu += ("\n----------------------");
        menu += ("\n1 - Start Crawler");
        menu += ("\n2 - View Settings");
        menu += ("\n3 - View Sites");
        menu += ("\n4 - View Jobs");
        menu += ("\n----------------------");
        menu += ("\n0 - Quit application");
        logger.log(Level.INFO,"Set menu shown to be Start menu");
    }

    /**
     * Sets the text for the configuration menu.
     * @param config The configuration settings.
     */
    private static void setConfigMenuText(Config config) {
        menu  = ("\nConfig");
        menu += ("\n--------------------------------");
        menu += ("\n\tNumber of threads to run = " + config.getThreadCount());
        menu += ("\n\tTime for thread to die = " + config.getDeathTimer());
        menu += ("\n--------------------------------");
        menu += ("\n1 - Change number of threads");
        menu += ("\n2 - Change how long to run");
        menu += ("\n--------------------------------");
        menu += ("\n0 - Back to Start Menu");
        logger.log(Level.INFO,"Set menu shown to be Config menu");
    }

    /**
     * Sets the text for the site selection menu.
     * @param sites The list of sites.
     */
    private static void setSitesMenuText(List<Site> sites) {
        menu  = ("\nSites to search");
        menu += ("\n-------------------------------");
        int i = 0;
        for (Site site : sites) {
            menu = menu.concat("\n" + ++i + " - [" + (site.isSearched() ? 'X' : ' ') + "] - " + site.getName());
        }
        menu += ("---------------------------------");
        menu += ("\nEnter the number of the site you want to switch searching on/off.");
        menu += ("\n0 - Back to Start Menu");
        logger.log(Level.INFO,"Set menu shown to be Sites menu");
    }

    private static void setJobsMenuText(List<JobListing> jobs, boolean full){
        menu  = ("\nJobs found");
        menu += ("\n--------------------------");
        menu += ("\nNumber of jobs = " + jobs.size());
        menu += ("\n--------------------------");
        menu += ("\n1 - Show all job listings");
        menu += ("\n0 - Back to Start Menu");
        menu += ("\n--------------------------");
        // Print all jobs to System.out
        if (full){
            for (JobListing job : jobs) {
                menu = menu.concat("Title: " + job.getTitle() + ", URL: " + job.getUrl());
            }
        }
        logger.log(Level.INFO,"Set menu shown to be Jobs menu");
    }

}
