package com.hashedvalue.webcrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

class UrlParser {
    private Document htmlDocument;
    private String startUrl;
    private String url;
    private String user_agent;
    private boolean validateSSLCerts;

    UrlParser(String startUrl, String url, String user_agent, boolean validateSSLCerts) {
        this.startUrl = startUrl;
        this.url = url;
        this.user_agent = user_agent;
        this.validateSSLCerts = validateSSLCerts;
    }

    private boolean getPage(String url, String user_agent) {
        Connection conn;
        try {
            conn = Jsoup.connect(url).userAgent(user_agent).validateTLSCertificates(validateSSLCerts);
            htmlDocument = conn.get();
        } catch (IOException e) {
            return false;
        }

        return conn.response().statusCode() == 200 && conn.response().contentType().contains("text/html");
    }

    ProcessedPage processPage() {
        ProcessedPage page = new ProcessedPage(startUrl, url);

        if(getPage(url, user_agent)) {
            Elements links = htmlDocument.select("a[href],link[href]");
            for (Element el : links) {
                page.addUrl(el.absUrl("href"));
            }
            Elements other = htmlDocument.select("img[src],script[src]");
            for (Element el : other) {
                page.addUrl(el.absUrl("src"));
            }
        }
        return page;
    }
}
