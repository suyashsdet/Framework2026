package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.AccountsPage;

import static constants.WaitConstants.*;
import static constants.AppConstants.*;


import java.util.List;

public class AccountsPageTest extends BaseTest {







    @BeforeClass
    public void accountPageSetup()  {
        accountsPage=loginPage.doLogin(properties.getProperty("username"), properties.getProperty("password"));
    }


    @Test
    public void accountPageTitleTest() {
        String title=accountsPage.getAccountsPageTitle();
        Assert.assertEquals(title,ACCOUNTS_PAGE_TITLE);
    }


    @Test
    public void accountPageHeaderTest() {
        List<String> actHeaders = accountsPage.getAccountsPageHeaders();
        Assert.assertEquals(actHeaders, AccountsPage.expHeaders);
    }

    @Test
    public void accountPageHeadersCountTest() {
        Assert.assertEquals(accountsPage.getAccountsPageHeaders().size(), AccountsPage.expHeadersCount);
    }
}
