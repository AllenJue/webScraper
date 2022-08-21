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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    private final WebClient webClient;
    private String date;

    /**
     * Constructor for a WebScraper class that gathers the share info
     * for the Fortune 500 and generates a CSV file with the share data
     */
    public WebScraper() {
        webClient = new WebClient(BrowserVersion.CHROME);
        setWebClientPreferences();
        try {
            HtmlPage page = webClient.getPage("https://markets.businessinsider.com/index/components/s&p_500?p=1");
            setDate(page);
            // create a file in user home documents directory
            createDir();
            File CSVFile = new File(getDataPath() + File.separator + getDate() + ".csv");
            if(CSVFile.exists()) {
                System.out.println("File already exists. Nothing written");
            } else {
                createCSVFile(page, CSVFile);
            }
            webClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a CSV File from a page of share data
     *
     * @param page of share information
     * @param CSVFile CSVFile to write to
     */
    private void createCSVFile(HtmlPage page, File CSVFile) {
        try {
            List<HtmlElement> anchors = new ArrayList<>();
            // get first 10 pages
            populateAnchors(anchors, page);
            CSVWriter fileWriter = new CSVWriter(new FileWriter(CSVFile));
            // for the first 10 anchors, open the page and collect data from those pages
            for(HtmlElement element : anchors) {
                HtmlPage newWindow = (HtmlPage) (((HtmlAnchor) element).openLinkInNewWindow());
                List<HtmlElement> tableRows = newWindow.getByXPath(".//tbody//tr");
                for(HtmlElement tr : tableRows) {
                    Share share = processTr(tr);
                    if(share != null) {
                        fileWriter.writeNext(share.csvFormat().split(","));
                    }
                }
                newWindow.cleanUp();
            }
            fileWriter.close();
            System.out.println("Successfully Written");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables CSS, JavaScript, and utilizes InsecureSSL for a WebClient
     * in order to successfully gather information. Exceptions hidden to
     * make output more visible
     */
    private void setWebClientPreferences() {
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
    }

    /**
     * Create a directory called with the path /User/Documents/Share_Data
     * if it doesn't already exist
     */
    private void createDir() {
        String path = getDataPath();
        File directory = new File(path);
        if(directory.exists()) {
            System.out.println(directory + " exists already.");
        } else if(directory.mkdir()) {
            System.out.println(directory + " has just been created.");
        } else {
            System.out.println(directory + " could not be generated.");
        }
    }

    /**
     * Gets the path from the users home directory to Share_Data
     *
     * @return the concatenated path of User/Documents/Share_Data
     */
    public String getDataPath() {
        return System.getProperty("user.home") + File.separator + "Documents"
                + File.separator + "Share_Data";
    }

    /**
     * Gets the date that the CSV file was created
     *
     * @return this.date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date retrieved from the page. Will be used at the CSV file name
     *
     * @param page from stock market web page to have date retrieved from
     */
    private void setDate(HtmlPage page) {
        List<HtmlElement> elements = page.getByXPath("/html/body/main/div/div[3]/div[1]/div[4]/table/tbody/tr[1]/td[5]/text()");
        date = elements.get(1) + "";
    }

    /**
     * Gets all table data from a table row and creates a Share object from that
     *
     * @param tr row that data is extracted from
     * @return a Share object containing the name, the latest price, percent change over: day, 1 month, 6 months, and 1 year
     */
    private Share processTr(HtmlElement tr) {
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
    private double parseTableData(HtmlElement tableData, boolean needsFormatting) {
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
    private void populateAnchors(List<HtmlElement> anchors, HtmlPage page) {
        // gets first 10 pages of market shares
        final int NUM_PAGES = 10;
        for(int i = 1; i <= NUM_PAGES; i++) {
            String xPath = "//a[@href='?p=" + i + "']";
            anchors.add(page.getFirstByXPath(xPath));
        }
    }
}