package com.hashedvalue.webcrawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Crawler {

    private Integer maxProcessedPages;
    private String userAgent;
    private String reportFilename;
    private String startHost;
    private String startUrl;
    private boolean validateSSLCerts;

    private Queue<String> urls;
    private Queue<ProcessedPage> processedPages;
    private HashSet<String> processedUrls;

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
        if (!crawler.fetchConfiguration()) {
            System.exit(1);
        }

        crawler.urls.add(crawler.startUrl);
        do {
            url = crawler.urls.poll();
            System.out.println("Url fetched from queue: " + url);
            if(!crawler.processedUrls.contains(url)) {
                parser = new UrlParser(crawler.startHost, url, crawler.userAgent, crawler.validateSSLCerts);
                page = parser.processPage();
                crawler.processedUrls.add(url);

                if (page.getInternalUrls().size() > 0) {
                    crawler.urls.addAll(page.getInternalUrls());
                }

                if (page.getInternalUrls().size() > 0 || page.getExternalUrls().size() > 0 || page.getInternalStaticUrls().size() > 0 || page.getExternalStaticUrls().size() > 0) {
                    crawler.processedPages.add(page);
                }
            } else {
                System.out.println("Url was processed earlier: " + url);
            }
            processedPages++;
        } while(!crawler.urls.isEmpty() && processedPages <= crawler.maxProcessedPages);

        SummaryGenerator summary = new XMLSummaryGenerator(crawler.processedPages, crawler.reportFilename);
        summary.writeSummaryToFile();
    }

    private boolean fetchConfiguration() {
        boolean configFetched = true;
        String hostRegex = "^https?://([-a-zA-Z0-9.]+)[-a-zA-Z0-9.&/=_?:#]*$";
        Pattern hostPattern = Pattern.compile(hostRegex);
        String startUrlRegex = "^https?://([-a-zA-Z0-9.]+[-a-zA-Z0-9.&/=_?:#]*)$";
        Pattern startUrlPattern = Pattern.compile(startUrlRegex);
        String httpProxyHost;
        String httpProxyPort;
        String httpsProxyHost;
        String httpsProxyPort;
        String proxyRegex = "^https?://([-a-zA-Z0-9.]+):([0-9]{1,5})$";
        Pattern proxyPattern = Pattern.compile(proxyRegex);

        Map<String, String> environment = System.getenv();

        if (environment.keySet().contains("CWR_MAX_PROCESSED_PAGES") && !environment.get("CWR_MAX_PROCESSED_PAGES").equals("")) {
            maxProcessedPages = Integer.parseInt(environment.get("CWR_MAX_PROCESSED_PAGES"));
            System.out.println("maxProcessedPages set from CWR_MAX_PROCESSED_PAGES: " + maxProcessedPages);
        } else {
            Integer maxProcessedPagesDefault = 10;
            maxProcessedPages = maxProcessedPagesDefault;
            System.out.println("maxProcessedPages set from default: " + maxProcessedPages);
        }


        if (environment.keySet().contains("CWR_USER_AGENT") && !environment.get("CWR_USER_AGENT").equals("")) {
            userAgent = environment.get("CWR_USER_AGENT");
            System.out.println("userAgent set from CWR_USER_AGENT: " + userAgent);
        } else {
            String userAgentDefault = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
            userAgent = userAgentDefault;
            System.out.println("userAgent set from default: " + userAgent);
        }

        if (environment.keySet().contains("CWR_VALIDATE_SSL_CERTS") && !environment.get("CWR_VALIDATE_SSL_CERTS").equals("")) {
            validateSSLCerts = Boolean.parseBoolean(environment.get("CWR_VALIDATE_SSL_CERTS"));
            System.out.println("validateSSLCerts set from CWR_VALIDATE_SSL_CERTS: " + validateSSLCerts);
        } else {
            boolean validateSSLCertsDefault = true;
            validateSSLCerts = validateSSLCertsDefault;
            System.out.println("validateSSLCerts set from default: " + validateSSLCerts);
        }

        if (environment.keySet().contains("CWR_REPORT_FILENAME") && !environment.get("CWR_REPORT_FILENAME").equals("")) {
            reportFilename = environment.get("CWR_REPORT_FILENAME");
            System.out.println("reportFilename set from CWR_REPORT_FILENAME: " + reportFilename);
        } else {
            System.err.println("CWR_REPORT_FILENAME not set");
            configFetched = false;
        }

        if (environment.keySet().contains("CWR_START_URL") && !environment.get("CWR_START_URL").equals("")) {
            Matcher startUrlMatcher = startUrlPattern.matcher(environment.get("CWR_START_URL"));
            if (startUrlMatcher.find()) {
                startUrl = startUrlMatcher.group(0);
                System.out.println("startUrl set from CWR_START_URL: " + startUrl);

                Matcher startHostMatcher = hostPattern.matcher(startUrl);
                if (startHostMatcher.find()) {
                    startHost = startHostMatcher.group(1);
                }
            } else {
                System.err.println("CWR_START_URL not set properly: " + environment.get("CWR_START_URL"));
                configFetched = false;
            }
        } else {
            System.err.println("CWR_START_URL not set");
            configFetched = false;
        }

        if (environment.keySet().contains("HTTP_PROXY") && !environment.get("HTTP_PROXY").equals("")) {
            Matcher proxyUrlMatcher = proxyPattern.matcher(environment.get("HTTP_PROXY"));

            if (proxyUrlMatcher.find()) {
                httpProxyHost = proxyUrlMatcher.group(1);
                httpProxyPort = proxyUrlMatcher.group(2);

                System.setProperty("http.proxyHost", httpProxyHost);
                System.setProperty("http.proxyPort", httpProxyPort);

                System.out.println("http.proxyHost and http.proxyPort set from HTTP_PROXY: " + environment.get("HTTP_PROXY"));
            } else {
                System.err.println("HTTP_PROXY not set properly: " + environment.get("HTTP_PROXY"));
                configFetched = false;
            }
        } else {
            System.out.println("HTTP_PROXY not set");
        }

        if (environment.keySet().contains("HTTPS_PROXY") && !environment.get("HTTPS_PROXY").equals("")) {
            Matcher proxyUrlMatcher = proxyPattern.matcher(environment.get("HTTPS_PROXY"));

            if (proxyUrlMatcher.find()) {
                httpsProxyHost = proxyUrlMatcher.group(1);
                httpsProxyPort = proxyUrlMatcher.group(2);

                System.setProperty("https.proxyHost", httpsProxyHost);
                System.setProperty("https.proxyPort", httpsProxyPort);

                System.out.println("https.proxyHost and https.proxyPort set from HTTPS_PROXY: " + environment.get("HTTPS_PROXY"));
            } else {
                System.err.println("HTTPS_PROXY not set properly: " +  environment.get("HTTPS_PROXY"));
                configFetched = false;
            }
        }  else {
            System.out.println("HTTPS_PROXY not set");
        }

        return configFetched;
    }
}
