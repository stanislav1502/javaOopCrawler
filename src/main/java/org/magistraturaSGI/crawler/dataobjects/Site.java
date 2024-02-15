package org.magistraturaSGI.crawler.dataobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Site {
    private String name;
    private String url;
    private boolean search;
}
