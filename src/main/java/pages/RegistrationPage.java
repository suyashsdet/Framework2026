package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.components.UserComponent;
import utils.ElementUtil;
import static constants.WaitConstants.*;

public class RegistrationPage {


    WebDriver driver;
    ElementUtil elementUtils;

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        elementUtils = new ElementUtil(driver);
    }

    private final By name = By.xpath("//input[@name='firstname']");
    private final By lastName = By.xpath("//input[@name='lastname']");
    private final By email = By.xpath("//input[@name='email']");
    private final By telephone = By.xpath("//input[@name='telephone']");

    private final By password = By.xpath("//input[@name='password']");
    private final By confirmPassword = By.xpath("//input[@name='confirm']");

    private final By subscribeYes = By.xpath("//input[@name='newsletter' and @value='1']");
    private final By subscribeNo = By.xpath("//input[@name='newsletter' and @value='0']");

    private final By agreeCheckbox = By.xpath("//input[@name='agree']");

    private final By continueBtn = By.xpath("//input[@value='Continue']");




    private final By logoutLink = By.xpath("//a[text()='Logout' and @class]");
    private final By registerLink = By.xpath("//a[text()='Register' and @class]");


    private final By userDropDown = By.xpath("//span[@class=\"caret\"]");


    public AccountsPage doRegister(String name,
                              String lastName,
                              String email,
                              String telephone,
                              String password,
                              String confirmPassword,
                              String subscribe,
                              String agree) {

        elementUtils.waitForVisibilityOfElement(this.name, DEFAULT_MEDIUM_WAIT).sendKeys(name);
        elementUtils.waitForVisibilityOfElement(this.lastName, DEFAULT_MEDIUM_WAIT).sendKeys(lastName);
        elementUtils.waitForVisibilityOfElement(this.email, DEFAULT_MEDIUM_WAIT).sendKeys(email);
        elementUtils.waitForVisibilityOfElement(this.telephone, DEFAULT_MEDIUM_WAIT).sendKeys(telephone);
        elementUtils.waitForVisibilityOfElement(this.password, DEFAULT_MEDIUM_WAIT).sendKeys(password);
        elementUtils.waitForVisibilityOfElement(this.confirmPassword, DEFAULT_MEDIUM_WAIT).sendKeys(confirmPassword);

        if (subscribe.equalsIgnoreCase("Yes")) {
            elementUtils.click(subscribeYes);
        } else {
            elementUtils.click(subscribeNo);
        }

        if (agree.equalsIgnoreCase("Yes")) {
            elementUtils.click(agreeCheckbox);
        }

        elementUtils.clickWhenReady(continueBtn,DEFAULT_MEDIUM_WAIT);

        return new AccountsPage(driver);
    }

    public UserComponent navigateToUserDropDown() {

        elementUtils.clickWhenReady(userDropDown, DEFAULT_MEDIUM_WAIT);
        return new UserComponent(driver);


    }
}
