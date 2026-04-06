package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SearchResultsPageTest extends BaseTest {



    @BeforeClass
    public void searchResultsPageSetup() throws InterruptedException {
        accountsPage = loginPage.doLogin(properties.getProperty("username"), properties.getProperty("password"));

    }

    @DataProvider
    public Object[][] getSearchProductTestData() {
        return new Object[][] {
                {"MacBook",3},
                {"iPhone", 1},
                {"Samsung",2},
                {"HP",1},
                {"Apple",1}
        };
    }

    @Test(dataProvider = "getSearchProductTestData", enabled = true, description = "Search Results Page Test")
    public void searchResultsCountTest(String productName, int searchResultsCount) {
        searchResultsPage = accountsPage.doSearch(productName);
        int actCount = searchResultsPage.getProductsCount();
        Assert.assertEquals(actCount,searchResultsCount);
    }
}
