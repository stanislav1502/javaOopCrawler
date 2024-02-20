package org.magistraturaSGI.crawler.dataobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Site {
    private String name;
    private String url;
    @Setter
    private boolean searched;
}
