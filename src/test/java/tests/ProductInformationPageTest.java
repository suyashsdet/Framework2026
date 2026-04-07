package tests;

import java.util.*;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ProductInformationPageTest extends BaseTest {

    @BeforeClass
    public void searchResultsPageSetup() throws InterruptedException {
        accountsPage = loginPage.doLogin(properties.getProperty("username"), properties.getProperty("password"));

    }


    @DataProvider(name = "productData")
    public static Object[][] productData() {
        HashMap<String, String> macBookInfo = new HashMap<>();
        macBookInfo.put("Product Name : ", "MacBook");
        macBookInfo.put("Brand", "Apple");
        macBookInfo.put("Product Code", "Product 16");
        macBookInfo.put("Reward Points", "600");
        macBookInfo.put("Availability", "Out Of Stock");
        macBookInfo.put("Price Of Product", "$602.00");
        macBookInfo.put("Ex Tax", "$500.00");
        return new Object[][]{{"MacBook", macBookInfo}};
    }

    @DataProvider(name = "productDataLinkedHashMap")
    public static Object[][] productDataLinkedHashMap() {
        LinkedHashMap<String, String> macBookInfo = new LinkedHashMap<>();
        macBookInfo.put("Product Name : ", "MacBook");
        macBookInfo.put("Brand", "Apple");
        macBookInfo.put("Product Code", "Product 16");
        macBookInfo.put("Reward Points", "600");
        macBookInfo.put("Availability", "Out Of Stock");
        macBookInfo.put("Price Of Product", "$602.00");
        macBookInfo.put("Ex Tax", "$500.00");
        return new Object[][]{{"MacBook", macBookInfo}};
    }

    @DataProvider(name = "productDataTreeMap")
    public static Object[][] productDataTreeMap() {
        TreeMap<String, String> macBookInfo = new TreeMap<>();
        macBookInfo.put("Product Name : ", "MacBook");
        macBookInfo.put("Brand", "Apple");
        macBookInfo.put("Product Code", "Product 16");
        macBookInfo.put("Reward Points", "600");
        macBookInfo.put("Availability", "Out Of Stock");
        macBookInfo.put("Price Of Product", "$602.00");
        macBookInfo.put("Ex Tax", "$500.00");
        return new Object[][]{{"MacBook", macBookInfo}};
    }


    @Test(dataProvider = "productData")
    public void productInformationTestHashMap(String productName,
                                              HashMap<String, String> expectedProductInfo)
            throws InterruptedException {
        searchResultsPage = accountsPage.doSearch(productName);
        productInformationPage = searchResultsPage.selectProductFromSearchResults(productName);

        HashMap<String, String> actualProductInfo = productInformationPage.getProductInformationHashMap(productName);
        for (Map.Entry<String, String> entry : expectedProductInfo.entrySet()) {
            Assert.assertEquals(actualProductInfo.get(entry.getKey()), entry.getValue());
        }
    }

    @Test(dataProvider = "productDataLinkedHashMap")
    public void productInformationTestLinkedHashMap(String productName,
                                                   LinkedHashMap<String, String> expectedProductInfo)
            throws InterruptedException {
        searchResultsPage = accountsPage.doSearch(productName);
        productInformationPage = searchResultsPage.selectProductFromSearchResults(productName);
        LinkedHashMap<String, String> actualProductInfo =
                productInformationPage.getProductInformationLinkedHashMap(productName);
        for (Map.Entry<String, String> entry : expectedProductInfo.entrySet()) {
            Assert.assertEquals(actualProductInfo.get(entry.getKey()), entry.getValue());
        }
    }

    @Test(dataProvider = "productDataTreeMap")
    public void productInformationTestTreeMap(String productName,
                                             TreeMap<String, String> expectedProductInfo)
            throws InterruptedException {
        searchResultsPage = accountsPage.doSearch(productName);
        productInformationPage = searchResultsPage.selectProductFromSearchResults(productName);
        TreeMap<String, String> actualProductInfo = productInformationPage.getProductInformationTreeMap(productName);
        for (Map.Entry<String, String> entry : expectedProductInfo.entrySet()) {
            Assert.assertEquals(actualProductInfo.get(entry.getKey()), entry.getValue());
        }
    }

}


