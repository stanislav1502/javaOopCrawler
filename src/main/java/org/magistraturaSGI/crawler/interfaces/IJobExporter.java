
package org.magistraturaSGI.crawler.interfaces;
import org.magistraturaSGI.crawler.dataobjects.JobListing;
import java.io.File;
import java.util.List;

public interface IJobExporter {
    static File export(List<JobListing> jobListings) {
        return null;
    }
}
