package com.hashedvalue.webcrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;

public class XMLSummaryGenerator implements SummaryGenerator {
    private Queue<ProcessedPage> processedPages;
    private String filename;

    XMLSummaryGenerator(Queue<ProcessedPage> processedPages, String filename) {
        this.processedPages = processedPages;
        this.filename = filename;
    }

    public void writeSummaryToFile() {
        try(
                FileWriter fw = new FileWriter(filename);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {

            for(ProcessedPage page: processedPages) {
                bw.write(page.toXML());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
