/**
 * The WebScraper class gathers share information from: https://markets.businessinsider.com/index/components/s&p_500
 * and TODO: transcribes this data to a CSV, where TODO: data is sorted by some criteria.
 *
 * @Author: Allen Jue
 * @Since: 2022-08-17
 */
package webScraper.jue;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {

    public static void main(String[] args) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        try {
            HtmlPage page = webClient.getPage("https://markets.businessinsider.com/index/components/s&p_500?p=1");
            List<HtmlElement> anchors = new ArrayList<>();

            // get date
            String fileName = getDate(page) + ".csv";

            try {
                // get first 10 pages
                populateAnchors(anchors, page);
                CSVWriter fileWriter = new CSVWriter(new FileWriter(fileName, true));
                // for the first 10 anchors, open the page and collect data from those pages
                for(HtmlElement element : anchors) {
                    HtmlAnchor anchor = (HtmlAnchor) element;
                    HtmlPage newWindow = (HtmlPage) anchor.openLinkInNewWindow();
                    List<HtmlElement> tableRows = newWindow.getByXPath(".//tbody//tr");
                    List<Share> shares = new ArrayList<>();
                    for(HtmlElement tr : tableRows) {
                        Share share = processTr(tr);
                        if(share != null) {
                            shares.add(share);
                            fileWriter.writeNext(share.csvFormat().split(","));
                        }
                    }
                    // System.out.println(shares);
                    newWindow.cleanUp();
                }
                fileWriter.close();
            } catch (FileAlreadyExistsException e) {
                System.err.println("already exists: " + e.getMessage());
            }
            webClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the date retrieved from the page. Will be used at the CSV file name
     *
     * @param page from stock market web page to have date retrieved from
     * @return date of stock market data retrieval
     */
    private static String getDate(HtmlPage page) {
        List<HtmlElement> elements = page.getByXPath("/html/body/main/div/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[5]/text()");
        return elements.get(1) + "";
    }

    /**
     * Gets all table data from a table row and creates a Share object from that
     *
     * @param tr row that data is extracted from
     * @return a Share object containing the name, the latest price, percent change over: day, 1 month, 6 months, and 1 year
     */
    private static Share processTr(HtmlElement tr) {
        List<HtmlElement> elements = tr.getByXPath(".//td");
        String name = ((HtmlAnchor) elements.get(0).getFirstByXPath(".//a")).getTextContent();
        if(name.length() == 0) {
            return null;
        }
        try {
            double latestPrice = parseTableData(elements.get(1), true);
            double change = parseTableData(elements.get(3), false);
            double change_3Mo = parseTableData(elements.get(5), false);
            double change_6Mo = parseTableData(elements.get(6), false);
            double change_1Y = parseTableData(elements.get(7), false);
            return new Share(name, latestPrice, change, change_3Mo, change_6Mo, change_1Y);
        } catch (NumberFormatException e) {
            System.out.println("Problem with: " + name);
        }
        return null; // exception occurred
    }

    /**
     * Gets the double value from a table entry
     *
     * @param tableData entry where double is to be extracted from
     * @param needsFormatting needs formatting if data has a comma (deletes comma to allow for parsing)
     * @return the double value of a table entry
     */
    private static double parseTableData(HtmlElement tableData, boolean needsFormatting) {
        String formatted;
        if(needsFormatting) {
            formatted = tableData.getFirstChild().getTextContent();
            formatted = formatted.replace(",","");
        } else {
            HtmlElement entry = tableData.getFirstByXPath(".//span");
            formatted = entry.getTextContent();
        }
        return Double.parseDouble(formatted);
    }

    /**
     * Populates anchors with the first n pages of market shares
     *
     * @param anchors to other pages from the current page
     * @param page starting page
     */
    private static void populateAnchors(List<HtmlElement> anchors, HtmlPage page) {
        // gets first 10 pages of market shares
        final int NUM_PAGES = 10;
        for(int i = 1; i <= NUM_PAGES; i++) {
            String xPath = "//a[@href='?p=" + i + "']";
            anchors.add(page.getFirstByXPath(xPath));
        }
    }
}