package tests;

import base.BaseTest;
import constants.AppConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import static constants.AppConstants.ACCOUNTS_PAGE_TITLE;

public class LoginPageTest extends BaseTest {

    @Test(priority = 2)
    public void testLoginPageTitle() {
        Assert.assertEquals(loginPage.getLoginPageTitle(), AppConstants.LOGIN_PAGE_TITLE);
    }

    @Test(priority = 1)
    public void testLoginPageURL() {
        Assert.assertEquals(loginPage.getLoginPageURL(), AppConstants.LOGIN_PAGE_URL);
    }

    @Test(priority = 3)
    public void forgottenPasswordLinkTest() {
        Assert.assertTrue(loginPage.isForgottenPasswordLinkDisplayed());
    }

    @Test(priority = Integer.MAX_VALUE)
    public void loginTest() throws InterruptedException {
        accountsPage=  loginPage.doLogin(properties.getProperty("username"), properties.getProperty("password"));
        Assert.assertEquals(accountsPage.getAccountsPageTitle(),ACCOUNTS_PAGE_TITLE);
    }
}
