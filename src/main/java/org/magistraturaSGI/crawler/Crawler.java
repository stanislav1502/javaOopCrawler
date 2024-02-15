package org.magistraturaSGI.crawler;

import lombok.Getter;
import org.magistraturaSGI.crawler.dataobjects.JobListing;
import org.magistraturaSGI.crawler.dataobjects.Site;

import java.util.LinkedList;
import java.util.List;

public class Crawler {

    @Getter
    private final Config config;

    @Getter
    private final List<JobListing> jobListings = new LinkedList<>();


    public Crawler() {
        config = new Config();
    }

    public Crawler(Config config) {
        this.config = config;
    }


    public void Start() {


    }



}
