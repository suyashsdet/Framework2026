package pages;

import constants.AppConstants;
import constants.WaitConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementUtil;

import javax.swing.plaf.PanelUI;

import static constants.WaitConstants.*;
import static constants.AppConstants.*;



public class  LoginPage {

    WebDriver driver;
    ElementUtil elementUtil;


    public LoginPage(WebDriver driver){
        this.driver=driver;
        elementUtil = new ElementUtil(driver);

    }


    private final By username = By.xpath("//input[@name='email']");
    private final By password = By.xpath("//input[@name='password']");
    private final By loginButton = By.xpath("//input[@value='Login']");
    private final By forgottenPasswordLink = By.linkText("Forgotten Password");
    private final By registerLink = By.linkText("Register");

   


    public String getLoginPageTitle(){
        return elementUtil.waitForFullTitle(AppConstants.LOGIN_PAGE_TITLE, WaitConstants.DEFAULT_WAIT);
    }

    public String getLoginPageURL(){
        return elementUtil.waitForFullURL(LOGIN_PAGE_URL,DEFAULT_WAIT);
    }

    public boolean isForgottenPasswordLinkDisplayed(){
        return elementUtil.isDisplayed(forgottenPasswordLink);
    }




    public AccountsPage doLogin(String username, String password) {
        elementUtil.waitForVisibilityOfElement(this.username,DEFAULT_WAIT).sendKeys(username);
        elementUtil.waitForVisibilityOfElement(this.password,DEFAULT_WAIT).sendKeys(password);
        elementUtil.waitForElementToBeClickable(this.loginButton,DEFAULT_WAIT).click();

        //return elementUtil.waitForFullTitle(ACCOUNTS_PAGE_TITLE, DEFAULT_WAIT);
        return new AccountsPage(driver);

    }






    public RegistrationPage goToRegisterPage() {
        elementUtil.waitForElementToBeClickable(registerLink, WaitConstants.DEFAULT_LONG_WAIT).click();
        return new RegistrationPage(driver);
    }

}
