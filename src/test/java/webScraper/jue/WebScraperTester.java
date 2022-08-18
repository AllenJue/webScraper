package webScraper.jue;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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


}
