package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementUtil;

public class LogoutPage {


    WebDriver driver;
    ElementUtil elementUtil;

    public LogoutPage(WebDriver driver) {
        this.driver = driver;
        elementUtil = new ElementUtil(driver);
    }

    private final By continueBtn = By.xpath("(//a[text()=\"Continue\"])[1]");

    public HomePage clickOnContinue() {
        elementUtil.clickWhenReady(continueBtn, 30);
        return new HomePage(driver);
    }
}
