package org.magistraturaSGI.crawler.interfaces;

import org.magistraturaSGI.crawler.dataobjects.Site;

import java.util.List;

public interface IConfigurable {
    int getThreadCount();
    int getDeathTimer();
    List<Site> getSiteList();

    void loadFromFile() ;
}
