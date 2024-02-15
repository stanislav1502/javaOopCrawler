package org.magistraturaSGI.crawler;

import org.magistraturaSGI.crawler.dataobjects.Site;

import java.io.IOException;

public class MenuController {

    private static final Crawler crawler = new Crawler() ;
    public static void main(String[] args) throws IOException {

        char key ;
        do {
            System.out.print("\033[H\033[2J");
            System.out.flush();

            System.out.println("Crawler Menu");
            System.out.println("----------------------");
            System.out.println("1 - Run Default");
            System.out.println("2 - View Settings");
            System.out.println("3 - View Sites");
            System.out.println("----------------------");
            System.out.println("0 - Quit application");
            key = (char) System.in.read();
        }while (run(key));
    }

    private static boolean run(char c){
        switch (c) {
            case '0': return false;
            case '1': crawler.Start(); break;
            case '2': viewConfig(); break;
            case '3': viewSites(); break;

        }
        return true;
    }

    private static void viewConfig() {

Config config = crawler.getConfig();
        String msg =
                "Config" +
                        "\n{" +
                        "\n\tThreadCount=" + config.getThreadCount() +
                        ", \n\tDeathTimer=" + config.getDeathTimer() +
                        "\n}";

        System.out.println(msg);
    }

    private static void viewSites() {
        String msg = "Sites to search: ";
        var sites = crawler.getConfig().getSiteList();
        int i=0;
        for (Site site : sites) {
            msg += "\n" + ++i + " - [ ] - " + sites.iterator() + site.getName();
        }
        System.out.println(msg);
    }
}
