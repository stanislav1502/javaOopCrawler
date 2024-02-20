
package org.magistraturaSGI.crawler;

import lombok.Getter;
import org.magistraturaSGI.crawler.dataobjects.JobListing;
import org.magistraturaSGI.crawler.dataobjects.Site;
import org.magistraturaSGI.crawler.interfaces.IJobExporter;
import org.magistraturaSGI.crawler.interfaces.INavigableConsoleMenu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for managing the menu and user interactions in the Crawler application.
 */
public class MenuController implements INavigableConsoleMenu, IJobExporter {
    private static final Logger logger = Logger.getLogger(MenuController.class.getName());
    @Getter
    private static String menu = ""; // String to store the menu text
    private static final Crawler crawler = new Crawler(); // Crawler instance for fetching job listings

    public static void main(String[] args) {
        logger.log(Level.INFO, "Program started.");
        char key;
        System.out.println("\nNavigate through menus by typing the corresponding option number and pressing enter.\n");
        do {
            System.out.flush();
            System.out.print("\033[H\033[2J");

            setStartMenuText();
            System.out.println(getMenu());

            System.out.flush();
            key = new Scanner(System.in).next().trim().charAt(0);
        } while (chooseOption(key));
    }

    /**
     * Reads which menu item was selected and takes the appropriate action.
     * If 0 was inputted, returns false and terminates the program.
     * @param c The key pressed.
     * @return Default true.
     */
    protected static boolean chooseOption(char c) {
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
        logger.log(Level.INFO, "User selected menu option - Start Crawler");
        try {
            crawler.addSitesToSearch();
            // Number of threads to run (adjust as needed)
            int numberOfThreads = crawler.getConfig().getThreadCount();
            // Create multiple threads
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                Thread thread = new Thread(crawler);
                threads.add(thread);
            }
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
                logger.log(Level.INFO, "Started Crawler thread {0}", thread.threadId());
            }
            // Stop threads after death
            Timer timer = new Timer("MenuTimer");
            TimerTask stopThreads = new TimerTask() {
                public void run() {
                    threads.forEach(Thread::interrupt);
                }
            };
            long delay = crawler.getConfig().getDeathTimer()* 1000L;
            timer.schedule(stopThreads, delay);

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
            // Fetch all job listings from the sites in the queue

            crawler.findJobListings();
            logger.log(Level.INFO, "Finished processing job listings.");
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
            System.out.println(menu);
            System.out.flush();
            key = new Scanner(System.in).next().charAt(0);
            switch (key) {
                case '1': {
                    System.out.print("ThreadCount = ");
                    System.out.flush();
                    int n = Integer.parseInt(new Scanner(System.in).next());
                    config.setThreadCount(n);
                    logger.log(Level.INFO,"ThreadCount set to {0}",n);
                }
                break;
                case '2': {
                    System.out.print("DeathTimer = ");
                    System.out.flush();
                    int t = Integer.parseInt(new Scanner(System.in).next());
                    config.setDeathTimer(t);
                    logger.log(Level.INFO,"DeathTimer set to {0}",t);
                }
                break;
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
            System.out.flush();
            key = new Scanner(System.in).next().charAt(0);
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
            System.out.flush();
            key = new Scanner(System.in).next().charAt(0);
            switch (key) {
                case '1': {
                    showAll = !showAll;
                    logger.log(Level.INFO, "ShowAllJobs set to {0}", showAll);
                }
                break;
                case '2': {
                    System.out.println("Export jobs as txt");
                    File file = export(jobs);
                    if (file != null) {
                        logger.log(Level.INFO, "Jobs exported to: {0}", file.getAbsolutePath());
                    }
                }
                break;
            }
        } while (key != '0');
        logger.log(Level.INFO, "Exited View Jobs menu");
    }

    /**
     *
     * @param list - the jobs to be exported
     * @return the created file
     */
    public static File export(List<JobListing> list) {
        // Create new file
        URL path = Config.class.getClassLoader().getResource("config.xml");
        if (path == null) {
            logger.log(Level.SEVERE, "Could not find config.xml file.");
            return null;
        }
        String newPath = path.getPath().replace("config.xml","jobOutput.txt");
        File file = new File(newPath);
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            // Write to file and close it
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            String line;
            for (JobListing job : list) {
                line = "Title: " + job.getTitle() + " | Found at: " + job.getUrl()+"\n";
                bw.write(line);
            }
            bw.close();
        } catch (IOException e){
            logger.log(Level.SEVERE, "An error occurred while writing to the file.", e);
        }
        return file;
    }

    /**
     * Sets the text for the start menu.
     */
    protected static void setStartMenuText(){
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
        menu += ("\n---------------------------------");
        menu += ("\nEnter the number of the site you want to switch searching on/off.");
        menu += ("\n0 - Back to Start Menu");
        logger.log(Level.INFO,"Set menu shown to be Sites menu");
    }

    private static void setJobsMenuText(List<JobListing> jobs, boolean showAll){
        menu  = ("\nJobs found");
        menu += ("\n--------------------------");
        menu += ("\nNumber of jobs = " + jobs.size());
        menu += ("\n--------------------------");
        menu += ("\n1 - Show all job listings");
        menu += ("\n2 - Export job listings");
        menu += ("\n0 - Back to Start Menu");
        menu += ("\n--------------------------");
        // Will print all jobs
        if (showAll){
            for (JobListing job : jobs) {
                menu = menu.concat("Title: " + job.getTitle() + ", URL: " + job.getUrl());
            }
        }
        logger.log(Level.INFO,"Set menu shown to be Jobs menu, allJobs = {0}",showAll);
    }
}
