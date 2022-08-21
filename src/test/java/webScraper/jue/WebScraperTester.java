package webScraper.jue;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class WebScraperTester {
    @Test
    public void homePage() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);
            final HtmlPage page = webClient.getPage("https://htmlunit.sourceforge.io/");

            Assertions.assertEquals("HtmlUnit – Welcome to HtmlUnit", page.getTitleText());

            final String pageAsXml = page.asXml();
            Assertions.assertTrue(pageAsXml.contains("<body class=\"topBarDisabled\">"));

            final String pageAsText = page.asNormalizedText();
            Assertions.assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));
        }
    }

    @Test
    public void webScrapeFromSiteIsFunctional() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        try {
            HtmlPage page = webClient.getPage("https://markets.businessinsider.com/index/components/s&p_500?p=1");
            Assertions.assertTrue(page.isHtmlPage());
            Assertions.assertTrue(page.asNormalizedText().contains("Note: This page shows the holdings of the iShares Core S&P 500 UCITS ETF\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}