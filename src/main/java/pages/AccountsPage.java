package pages;
import constants.WaitConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import pages.components.UserComponent;
import utils.ElementUtil;
import static constants.WaitConstants.*;
import static constants.AppConstants.*;
import java.util.ArrayList;
import java.util.List;


public class AccountsPage {

    WebDriver driver;
    ElementUtil elementUtil;

    public AccountsPage(WebDriver driver) {
        this.driver = driver;
        elementUtil = new ElementUtil(driver);
    }


    public static final List<String> expHeaders = List.of(
            "My Account",
            "My Orders",
            "My Affiliate Account",
            "Newsletter");

    public static int expHeadersCount = 4;


    private final By headers = By.xpath("//h2");
    private final By logout = By.xpath("//a[@class='logout']");
    private final By search = By.xpath("//input[@name='search']");
    private final By searchBtn = By.xpath("//button[@class='btn btn-default btn-lg']");

    private final By accountCreatedHeader = By.xpath("//div[@id='content']//h1");
    private final By userDropDown = By.xpath("//span[@class=\"caret\"]");
    List<String> headers_list;

    public String getAccountsPageURL() {
        return elementUtil.waitForFullURL(ACCOUNTS_PAGE_URL, DEFAULT_WAIT);
    }

    public String getAccountsPageTitle() {
        return elementUtil.waitForFullTitle(ACCOUNTS_PAGE_TITLE, DEFAULT_LONG_WAIT);
    }

    public List<String> getAccountsPageHeaders() {
        headers_list = new ArrayList<String>();
        List<WebElement> elements = elementUtil.waitForVisibilityOfElements(headers, DEFAULT_LONG_WAIT);
        for (WebElement element : elements) {
            headers_list.add(element.getText());
        }

        return headers_list;
    }

    public SearchResultsPage doSearch(String productName) {
        elementUtil.typeWhenVisible(search, productName, DEFAULT_WAIT);
        elementUtil.clickWhenReady(searchBtn, DEFAULT_MEDIUM_WAIT);
        return new SearchResultsPage(driver);
    }


    // FIX: StaleElementReferenceException was occurring on Edge browser.
    // After form submission, the page navigates to Your Account Has Been Created page.
    // Edge is slower in committing the DOM so by the time .isDisplayed() was called on the
    // returned WebElement, the DOM had already been rebuilt making the reference stale.
    // waitForVisibilityOfElement internally uses ExpectedConditions.visibilityOfElementLocated
    // which already confirms visibility before returning - no need to call .isDisplayed() again.
    // Retry analyzer was retrying the test but marking attempts as SKIPPED in Allure since
    // retry is for environment flakiness, not code bugs. Same broken code = same failure every time.

    public boolean validateAccountCreation() {
        return elementUtil.waitForVisibilityOfElement(accountCreatedHeader, DEFAULT_MEDIUM_WAIT) != null;
    }


    public UserComponent navigateToUserDropDown() {

        elementUtil.clickWhenReady(userDropDown, DEFAULT_MEDIUM_WAIT);
        return new UserComponent(driver);


    }
}
