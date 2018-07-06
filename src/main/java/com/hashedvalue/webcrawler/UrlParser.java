package com.hashedvalue.webcrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

class UrlParser {
    private Document htmlDocument;
    private String startHost;
    private String url;
    private String user_agent;
    private boolean validateSSLCerts;

    UrlParser(String startHost, String url, String user_agent, boolean validateSSLCerts) {
        this.startHost = startHost;
        this.url = url;
        this.user_agent = user_agent;
        this.validateSSLCerts = validateSSLCerts;
    }

    private boolean getPage(String url, String user_agent) {
        Connection conn = null;
        try {
            conn = Jsoup.connect(url).userAgent(user_agent).validateTLSCertificates(validateSSLCerts);
            htmlDocument = conn.get();
        } catch (IOException e) {
            System.err.println("Page for url " + url + "not fetched correctly: " + e.getMessage());
        }

        if (conn.response().statusCode() == 200) {
            String documentContentType = conn.response().contentType();
            System.out.println("Page for url " + url + "fetched correctly with ContentType: " + documentContentType);
            return true;
        } else {
            System.err.println("Page for url " + url + "not fetched incorrectly with status code: " + conn.response().statusCode());
            return false;
        }
    }

    ProcessedPage processPage() {
        ProcessedPage page = new ProcessedPage(startHost, url);

        if (getPage(url, user_agent)) {
            Elements links = htmlDocument.select("a[href],link[href]");
            for (Element el : links) {
                String link = el.absUrl("href");
                page.addUrl(link);
            }

            Elements otherLinks = htmlDocument.select("img[src],script[src]");
            for (Element el : otherLinks) {
                String otherLink = el.absUrl("src");
                page.addUrl(otherLink);
            }
        }

        return page;
    }
}
