package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.components.UserComponent;
import utils.ElementUtil;

import static constants.WaitConstants.DEFAULT_MEDIUM_WAIT;

public class HomePage {
    WebDriver driver;
    ElementUtil elementUtil;


    public HomePage(WebDriver driver){
        this.driver=driver;
        elementUtil = new ElementUtil(driver);

    }

    private final By userDropDown = By.xpath("//span[@class=\"caret\"]");

    public UserComponent navigateToUserDropDown() {

        elementUtil.clickWhenReady(userDropDown, DEFAULT_MEDIUM_WAIT);
        return new UserComponent(driver);


    }
}
