package pages;
import constants.WaitConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementUtil;
import static constants.WaitConstants.*;
import static constants.AppConstants.*;


public class SearchResultsPage {

    WebDriver driver;
    ElementUtil elementUtilities;
    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        elementUtilities = new ElementUtil(driver);

    }

    private final By numberOfProducts = By.xpath("//div[@class='product-thumb']");

    public int getProductsCount() {
        return elementUtilities.waitForVisibilityOfElements(numberOfProducts,DEFAULT_MEDIUM_WAIT).size();
    }

    public ProductInformationPage selectProductFromSearchResults(String productName) {
        By product = By.xpath("//div[@class='product-thumb']//a[text()='" + productName + "']");
        elementUtilities.clickWhenVisible(product,DEFAULT_LONG_WAIT);
        return new ProductInformationPage(driver);
    }
}
