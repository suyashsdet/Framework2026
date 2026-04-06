package pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.LogoutPage;
import pages.RegistrationPage;
import utils.ElementUtil;

public class UserComponent {

    WebDriver driver;
    ElementUtil elementUtil;

    public UserComponent(WebDriver driver) {
        this.driver = driver;
        elementUtil = new ElementUtil(driver);
    }


    private final By logoutOption = By.xpath("(//a[text()=\"Logout\"])[1]");
    private final By registerOption = By.xpath("(//a[text()=\"Register\"])[1]");

    public LogoutPage navigateToLogoutPage(){
        elementUtil.clickWhenReady(logoutOption,30);
        return new LogoutPage(driver);
    }

    public RegistrationPage navigateToRegistrationPage(){
        elementUtil.clickWhenReady(registerOption,30);
        return new RegistrationPage(driver);
    }


}
