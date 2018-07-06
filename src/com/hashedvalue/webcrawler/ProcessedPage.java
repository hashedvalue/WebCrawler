package com.hashedvalue.webcrawler;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProcessedPage{
    private String currentUrl;
    private String startUrl;
    private HashSet<String> internalUrls;
    private HashSet<String> externalUrls;
    private HashSet<String> staticUrls;

    ProcessedPage(String startUrl, String currentUrl) {
        this.currentUrl = currentUrl;
        this.startUrl = startUrl;
        this.internalUrls = new HashSet<>();
        this.externalUrls = new HashSet<>();
        this.staticUrls = new HashSet<>();
    }

    HashSet<String> getInternalUrls() {
        return internalUrls;
    }

    HashSet<String> getExternalUrls() {
        return externalUrls;
    }

    HashSet<String> getStaticUrls() {
        return staticUrls;
    }

    private void addInternalUrl(String url) {
        internalUrls.add(url);
    }

    private void addExternalUrl(String url) {
        externalUrls.add(url);
    }

    private void addStaticUrl(String url) {
        staticUrls.add(url);
    }

    void addUrl(String url) {
        String hostRegex = "^https?://([-a-zA-Z0-9.]+)[-a-zA-Z0-9.&/=_?:#]*$";
        String host;
        boolean isStatic = false;
        String staticExtRegex = "^.*[.](bmp|class|css|csv|doc|docx|ejs|eot|eps|gif|ico|jar|jpeg|jpg|js|mid|midi|otf|pdf|pict|pls|png|ppt|pptx|ps|svg|svgz|swf|tif|tiff|ttf|txt|webp|woff|woff2|xls|xlsx)$";
        String startHost = null;

        Pattern hostPattern = Pattern.compile(hostRegex);

        Matcher startUrlMatcher = hostPattern.matcher(startUrl);
        if (startUrlMatcher.find()) {
            startHost = startUrlMatcher.group(1);
        }

        Pattern staticExtPattern = Pattern.compile(staticExtRegex);
        Matcher staticExtMatcher = staticExtPattern.matcher(url);
        if (staticExtMatcher.matches()) {
            isStatic = true;
        }

        Matcher urlMatcher = hostPattern.matcher(url);
        if (urlMatcher.find()) {
            host = urlMatcher.group(1);
            if (startHost.equals(host) || host.endsWith(startHost)) {
                if (isStatic) {
                    addStaticUrl(url);
                } else {
                    addInternalUrl(url);
                }
            } else {
                if (isStatic) {
                    addStaticUrl(url);
                } else {
                    addExternalUrl(url);
                }
            }
        }
    }

    String toXML() {
        StringBuilder builder = new StringBuilder();

        builder.append("<page>\n");
        builder.append("\t<url>" + currentUrl + "</url>\n");

        if(internalUrls != null && internalUrls.size() > 0) {
            builder.append("\t<internalurls>\n");
            for (String internalUrl : internalUrls) {
                builder.append("\t\t<internalurl>" + internalUrl + "</internalurl>\n");
            }
            builder.append("\t</internalurls>\n");
        }
        if(externalUrls != null && externalUrls.size() > 0) {
            builder.append("\t<externalurls>\n");
            for (String externalUrl : externalUrls) {
                builder.append("\t\t<externalurl>" + externalUrl + "</externalurl>\n");
            }
            builder.append("\t</externalurls>\n");
        }

        if(staticUrls != null && staticUrls.size() > 0) {
            builder.append("\t<staticurls>\n");
            for (String staticUrl : staticUrls) {
                builder.append("\t\t<staticurl>" + staticUrl + "</staticurl>\n");
            }
            builder.append("\t</staticurls>\n");
        }

        builder.append("</page>\n");
        return builder.toString();
    }
}
