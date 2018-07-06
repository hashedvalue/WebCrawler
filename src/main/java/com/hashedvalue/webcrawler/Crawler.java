package com.hashedvalue.webcrawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    private Integer maxProcessedPagesDefault=10;
    private String userAgentDefault="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private boolean validateSSLCertsDefault = true;

    private Integer maxProcessedPages;
    private String userAgent;
    private String reportFilename;
    private String startUrl;
    private boolean validateSSLCerts;

    private Queue<String> urls;
    private Queue<ProcessedPage> processedPages;
    private HashSet<String> processedUrls;
    private Map<String,String> environment;

    private Crawler() {
        urls = new LinkedList<>();
        processedPages = new LinkedList<>();
        processedUrls = new HashSet<>();
    }

    public static void main(String[] args) {
        String url;
        UrlParser parser;
        Integer processedPages = 0;
        ProcessedPage page;

        Crawler crawler = new Crawler();
        crawler.fetchConfiguration();

        crawler.urls.add(crawler.startUrl);
        do {
            url = crawler.urls.poll();
            if(!crawler.processedUrls.contains(url)) {
                parser = new UrlParser(crawler.startUrl, url, crawler.userAgent, crawler.validateSSLCerts);
                page = parser.processPage();
                crawler.processedUrls.add(url);

                if (page.getInternalUrls().size() > 0) {
                    crawler.urls.addAll(page.getInternalUrls());
                }

                if (page.getInternalUrls().size() > 0 || page.getExternalUrls().size() > 0 || page.getStaticUrls().size() > 0) {
                    crawler.processedPages.add(page);
                }
            }
            processedPages++;
        } while(!crawler.urls.isEmpty() && processedPages <= crawler.maxProcessedPages);

        SummaryGenerator summary = new XMLSummaryGenerator(crawler.processedPages, crawler.reportFilename);
        summary.writeSummaryToFile();
    }

    private void fetchConfiguration() {
        String proxyRegex = "^https?://([-a-zA-Z0-9.]+):([0-9]{1,5})$";
        String httpProxyHost;
        String httpProxyPort;
        String httpsProxyHost;
        String httpsProxyPort;
        Pattern hostPattern = Pattern.compile(proxyRegex);

        environment = System.getenv();

        if (environment.keySet().contains("CWR_MAX_PROCESSED_PAGES") && !environment.get("CWR_MAX_PROCESSED_PAGES").equals("")) {
            maxProcessedPages = Integer.parseInt(environment.get("CWR_MAX_PROCESSED_PAGES"));
        } else {
            maxProcessedPages = maxProcessedPagesDefault;
        }

        if (environment.keySet().contains("CWR_USER_AGENT") && !environment.get("CWR_USER_AGENT").equals("")) {
            userAgent = environment.get("CWR_USER_AGENT");
        } else {
            userAgent = userAgentDefault;
        }

        if (environment.keySet().contains("CWR_VALIDATE_SSL_CERTS") && !environment.get("CWR_VALIDATE_SSL_CERTS").equals("")) {
            validateSSLCerts = Boolean.parseBoolean(environment.get("CWR_VALIDATE_SSL_CERTS"));
        } else {
            validateSSLCerts = validateSSLCertsDefault;
        }

        if (environment.keySet().contains("CWR_REPORT_FILENAME") && !environment.get("CWR_REPORT_FILENAME").equals("")) {
            reportFilename = environment.get("CWR_REPORT_FILENAME");
        }

        if (environment.keySet().contains("CWR_START_URL") && !environment.get("CWR_START_URL").equals("")) {
            startUrl = environment.get("CWR_START_URL");
        }

        if (environment.keySet().contains("HTTP_PROXY") && !environment.get("HTTP_PROXY").equals("")) {
            Matcher proxyUrlMatcher = hostPattern.matcher(environment.get("HTTP_PROXY"));

            if (proxyUrlMatcher.find()) {
                httpProxyHost = proxyUrlMatcher.group(1);
                httpProxyPort = proxyUrlMatcher.group(2);
            } else {
                httpProxyHost = null;
                httpProxyPort = null;
            }

            System.setProperty("http.proxyHost", httpProxyHost);
            System.setProperty("http.proxyPort", httpProxyPort);
        }

        if (environment.keySet().contains("HTTPS_PROXY") && !environment.get("HTTPS_PROXY").equals("")) {
            Matcher proxyUrlMatcher = hostPattern.matcher(environment.get("HTTPS_PROXY"));

            if (proxyUrlMatcher.find()) {
                httpsProxyHost = proxyUrlMatcher.group(1);
                httpsProxyPort = proxyUrlMatcher.group(2);
            } else {
                httpsProxyHost = null;
                httpsProxyPort = null;
            }

            System.setProperty("https.proxyHost", httpsProxyHost);
            System.setProperty("https.proxyPort", httpsProxyPort);
        }

    }
}
