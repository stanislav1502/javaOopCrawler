package org.magistraturaSGI.crawler.interfaces;

public interface INavigableConsoleMenu {
    static boolean chooseOption(char c) {
        return false;
    };
    static void startCrawler(){};
    static void viewConfig(){};
    static void viewSites(){}
    static void viewJobs(){}
}
