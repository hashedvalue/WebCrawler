package com.hashedvalue.webcrawler;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProcessedPage{
    private String currentUrl;
    private String startHost;
    private HashSet<String> internalUrls;
    private HashSet<String> externalUrls;
    private HashSet<String> internalStaticUrls;
    private HashSet<String> externalStaticUrls;

    ProcessedPage(String startHost, String currentUrl) {
        this.currentUrl = currentUrl;
        this.startHost = startHost;
        this.internalUrls = new HashSet<>();
        this.externalUrls = new HashSet<>();
        this.internalStaticUrls = new HashSet<>();
        this.externalStaticUrls = new HashSet<>();
    }

    HashSet<String> getInternalUrls() {
        return internalUrls;
    }

    HashSet<String> getExternalUrls() {
        return externalUrls;
    }

    HashSet<String> getInternalStaticUrls() {
        return internalStaticUrls;
    }

    HashSet<String> getExternalStaticUrls() {
        return externalStaticUrls;
    }

    private void addInternalUrl(String url) {
        internalUrls.add(url);
    }

    private void addExternalUrl(String url) {
        externalUrls.add(url);
    }

    private void addInternalStaticUrl(String url) {
        internalStaticUrls.add(url);
    }

    private void addExternalStaticUrl(String url) {
        externalStaticUrls.add(url);
    }

    void addUrl(String linkUrl) {
        boolean isStatic = false;

        String hostRegex = "^https?://([-a-zA-Z0-9.]+)[-a-zA-Z0-9.&/=_?:#]*$";
        Pattern hostPattern = Pattern.compile(hostRegex);

        String staticExtRegex = "^.*[.](bmp|class|css|csv|doc|docx|ejs|eot|eps|gif|ico|jar|jpeg|jpg|js|json|mid|midi|otf|pdf|pict|pls|png|ppt|pptx|ps|svg|svgz|swf|tif|tiff|ttf|txt|webp|woff|woff2|xls|xlsx)(\\?.*)?$";
        Pattern staticExtPattern = Pattern.compile(staticExtRegex);

        Matcher staticExtMatcher = staticExtPattern.matcher(linkUrl);
        if (staticExtMatcher.matches()) {
            isStatic = true;
        }

        Matcher linkUrlMatcher = hostPattern.matcher(linkUrl);
        if (linkUrlMatcher.find()) {
            String host = linkUrlMatcher.group(1);
            if (startHost.equals(host) || host.endsWith(startHost)) {
                if (isStatic) {
                    if(internalStaticUrls.contains(linkUrl)) {
                        System.out.println("Url already in internal static urls: " + linkUrl);
                    } else {
                        addInternalStaticUrl(linkUrl);
                        System.out.println("Url added to internal static urls: " + linkUrl);
                    }
                } else {
                    if (internalUrls.contains(linkUrl)) {
                        System.out.println("Url already in internal urls: " + linkUrl);
                    } else {
                        addInternalUrl(linkUrl);
                        System.out.println("Url added to internal urls: " + linkUrl);
                    }
                }
            } else {
                if (isStatic) {
                    if (externalStaticUrls.contains(linkUrl)) {
                        System.out.println("Url already in external static urls: " + linkUrl);
                    } else {
                        addExternalStaticUrl(linkUrl);
                        System.out.println("Url added to external static urls: " + linkUrl);
                    }
                } else {
                    if (externalUrls.contains(linkUrl)) {
                        System.out.println("Url already in external urls: " + linkUrl);
                    } else {
                        addExternalUrl(linkUrl);
                        System.out.println("Url added to external urls: " + linkUrl);
                    }
                }
            }
        }
    }

    String toXML() {
        StringBuilder builder = new StringBuilder();

        builder.append("<page>\n");
        builder.append("\t<url>").append(currentUrl).append("</url>\n");

        if(internalUrls != null && internalUrls.size() > 0) {
            builder.append("\t<internalurls>\n");
            for (String internalUrl : internalUrls) {
                builder.append("\t\t<internalurl>").append(internalUrl).append("</internalurl>\n");
            }
            builder.append("\t</internalurls>\n");
        }
        if(externalUrls != null && externalUrls.size() > 0) {
            builder.append("\t<externalurls>\n");
            for (String externalUrl : externalUrls) {
                builder.append("\t\t<externalurl>").append(externalUrl).append("</externalurl>\n");
            }
            builder.append("\t</externalurls>\n");
        }

        if(internalStaticUrls != null && internalStaticUrls.size() > 0) {
            builder.append("\t<internalstaticurls>\n");
            for (String staticUrl : internalStaticUrls) {
                builder.append("\t\t<internalstaticurl>").append(staticUrl).append("</internalstaticurl>\n");
            }
            builder.append("\t</internalstaticurls>\n");
        }

        if(externalStaticUrls != null && externalStaticUrls.size() > 0) {
            builder.append("\t<externalstaticurls>\n");
            for (String staticUrl : externalStaticUrls) {
                builder.append("\t\t<externalstaticurl>").append(staticUrl).append("</externalstaticurl>\n");
            }
            builder.append("\t</externalstaticurls>\n");
        }

        builder.append("</page>\n");
        return builder.toString();
    }
}