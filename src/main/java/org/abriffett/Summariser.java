package org.abriffett;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Summariser service
 *
 */
public class Summariser {

    SummariserConfig summariserConfig;
    public Summariser() {
        //load config
        try {
            summariserConfig = loadSummariserConfig();
        }
        catch (IOException ioe) {
            throw new RuntimeException("Could not read input files: " + ioe);
        }
    }

    /**
     * Summarise a URL based on the config.
     * Fields to be extracted and the metadata attributes to check for the fields
     * can be configured in summariser-fields.txt and summariser-meta-attrs.txt
     *
     * @param url The URL to summarise
     * @return A plain map, so additional fields can be added. I considered a POJO to start with but that requires
     * a code change to add new fields.
     * @throws IOException
     */
    public Map<String, Object> summariseURL(String url, int numNestedPages) throws IOException {
        Map<String, Object> summary = new HashMap<>();
        try {
            Document doc = Jsoup.connect(url).get();
            summary.putAll(extractMeta(doc, numNestedPages));
        }
        catch (IOException ioe) {
            throw new IOException("Unable to reach URL: ", ioe);
        }
        return summary;
    }

    /* This will find nested pages within the parent page. The spec is
     * (I assume deliberately) vague about what criteria to use to pick them,
     * for now it only looks for links outside the current page (i.e. doesn't start with hash)
     *
     * Given more time I could check for www. and non www.
     * variants of the page to ensure it doesn't visit
     * https://www.bbc.co.uk/news from https://bbc.co.uk/news, and also check
     * it doesn't go up to the parent (e.g. https://www.bbc.co.uk)
     */
    private List<Map<String, Object>> findNestedPages(Document doc, int numLinksToSummarise) {
        Elements onwardLinks = doc.select("a");
        List<Map<String, Object>> nestedPages = new ArrayList<>();
        for (int i = 0; (nestedPages.size() < numLinksToSummarise) && (i < onwardLinks.size()); i++) {
            Element e = onwardLinks.get(i);
            String url = e.attr("href");
            if (!url.startsWith("#")) {
                System.out.println("found link: " + url);
                try {
                    // Don't follow any onward-links from this onward link, although this could be made recursive to crawl
                    // the entire site - additional checks to keep a list of pages already visited would be required to avoid
                    // a never-ending cycle.
                    Map<String, Object> summary = summariseURL(url, 0);
                    if (summary.size() > 0) {
                        nestedPages.add(summary);
                    }
                }
                catch (IOException ioe) {
                    // Unable to follow link, try next one
                }
            }
        }
        return nestedPages;
    }

    /*
     * Use the Jsoup CSS select to pick out the relevant metadata tags.
     */
    private Map<String, Object> extractMeta(Document doc, int numNestedPages) {
        Map<String, Object> summary = new HashMap<>();
        summariserConfig.getMetaFields().forEach(field -> {
            summariserConfig.getMetaAttributes().forEach( attr -> {
                String css = "meta[" + attr + "*=" + field + "]";
                String value = doc.select(css).attr("content");
                // Only add if the field isn't already populated
                if (!summary.containsKey(field) && value.length() > 0) {
                    summary.put(field, value);
                }
            });
        });
        if (numNestedPages > 0) {
            summary.put("nested_pages", findNestedPages(doc, numNestedPages));
        }
        return summary;
    }

    /* Load the config from text files. Given more time, I'd implement some sensible default behaviour, like loading
     * all metadata fields found on the page if there are no specific ones in config.
     */
    private SummariserConfig loadSummariserConfig() throws IOException{
        InputStream is = getClass().getClassLoader().getResourceAsStream("summariser-fields.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        List<String> fields = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            fields.add(line);
        }
        br.close();
        is = getClass().getClassLoader().getResourceAsStream("summariser-meta-attrs.txt");
        br = new BufferedReader(new InputStreamReader(is));
        List<String> metaAttrs = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            metaAttrs.add(line);
        }

        return new SummariserConfig(fields, metaAttrs);
    }

}
