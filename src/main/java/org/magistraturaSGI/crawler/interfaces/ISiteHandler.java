package org.magistraturaSGI.crawler.interfaces;

import org.magistraturaSGI.crawler.dataobjects.Site;

// Interface for handling site-related operations
public interface ISiteHandler {
    void addSitesToSearch();
    void setSiteSelectors(Site site);
    void findPageLinks(Site site);
    void findJobListings();
}
