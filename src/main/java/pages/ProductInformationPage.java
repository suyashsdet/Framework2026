package pages;

import constants.WaitConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.ElementUtil;

import java.util.*;

public class ProductInformationPage {

    WebDriver driver;
    ElementUtil elementUtilities;

    HashMap<String,String > productInformationHashMap  = new HashMap<String,String>();

    LinkedHashMap<String,String> productInformationLinkedHashMap = new LinkedHashMap<String,String>();

    TreeMap<String,String>  productInformationTreeMap = new TreeMap<String,String>();





    public ProductInformationPage(WebDriver driver) {
        this.driver = driver;
        elementUtilities = new ElementUtil(driver);

    }

        private final By productDetails = By.xpath("(//div[@class='col-sm-4']//ul)[1]//li");
        private final By productPrice = By.xpath("(//div[@class='col-sm-4']//ul)[2]//li");


        public HashMap<String,String> getProductInformationHashMap(String productName){

            productInformationHashMap = getProductHeaderHashMap(productName);
            productInformationHashMap = getProductDetailsHashMap();
            productInformationHashMap = getProductPriceHashMap();
            return productInformationHashMap;
        }

        public LinkedHashMap<String,String> getProductInformationLinkedHashMap(String productName){

        productInformationLinkedHashMap = getProductHeaderLinkedHashMap(productName);
        productInformationLinkedHashMap = getProductDetailsLinkedHashMap();
        productInformationLinkedHashMap = getProductPriceLinkedHashMap();
        return productInformationLinkedHashMap;
    }

        public TreeMap<String, String> getProductInformationTreeMap(String productName){

        productInformationTreeMap = getProductHeaderTreeMap(productName);
        productInformationTreeMap = getProductDetailsTreeMap();
        productInformationTreeMap = getProductPriceTreeMap();
        return productInformationTreeMap;
    }




        public HashMap<String,String> getProductHeaderHashMap(String productName){
            By productHeader = By.xpath("//h1[text()='" + productName + "']");
            String productHeaderName  = elementUtilities.waitForVisibilityOfElement(productHeader, WaitConstants.DEFAULT_MEDIUM_WAIT).getText();
            productInformationHashMap.put("Product Name : ", productHeaderName);
            return productInformationHashMap;
        }


    public LinkedHashMap<String,String> getProductHeaderLinkedHashMap(String productName){
        By productHeader = By.xpath("//h1[text()='" + productName + "']");
        String productHeaderName  = elementUtilities.waitForVisibilityOfElement(productHeader, WaitConstants.DEFAULT_MEDIUM_WAIT).getText();
        productInformationLinkedHashMap.put("Product Name : ", productHeaderName);
        return productInformationLinkedHashMap;
    }

    public TreeMap<String,String> getProductHeaderTreeMap(String productName){
        By productHeader = By.xpath("//h1[text()='" + productName + "']");
        String productHeaderName  = elementUtilities.waitForVisibilityOfElement(productHeader, WaitConstants.DEFAULT_MEDIUM_WAIT).getText();
        productInformationTreeMap.put("Product Name : ", productHeaderName);
        return productInformationTreeMap;
    }






        private HashMap<String,String> getProductDetailsHashMap(){
            List<WebElement> productDetailsListHashMap = elementUtilities.waitForVisibilityOfElements(productDetails, WaitConstants.DEFAULT_MEDIUM_WAIT);
            for(WebElement e: productDetailsListHashMap){
                String[] productDetails = e.getText().split(":");
                productInformationHashMap.put(productDetails[0].trim(),productDetails[1].trim());
            }
            return productInformationHashMap;
        }


    private LinkedHashMap<String,String> getProductDetailsLinkedHashMap(){
        List<WebElement> productDetailsListLinkedHashMap = elementUtilities.waitForVisibilityOfElements(productDetails, WaitConstants.DEFAULT_MEDIUM_WAIT);
        for(WebElement e: productDetailsListLinkedHashMap){
            String[] productDetails = e.getText().split(":");
            productInformationLinkedHashMap.put(productDetails[0].trim(),productDetails[1].trim());
        }
        return productInformationLinkedHashMap;
    }

    private TreeMap<String, String> getProductDetailsTreeMap(){
        List<WebElement> productDetailsListTreeMap = elementUtilities.waitForVisibilityOfElements(productDetails, WaitConstants.DEFAULT_MEDIUM_WAIT);
        for(WebElement e: productDetailsListTreeMap){
            String[] productDetails = e.getText().split(":");
            productInformationTreeMap.put(productDetails[0].trim(),productDetails[1].trim());
        }
        return productInformationTreeMap;
    }





















        private HashMap<String,String> getProductPriceHashMap(){
            List<WebElement> productPriceListHashMap = elementUtilities.waitForVisibilityOfElements(productPrice, WaitConstants.DEFAULT_MEDIUM_WAIT);
            productInformationHashMap.put("Price Of Product", productPriceListHashMap.get(0).getText().trim());
            String[] exTax = productPriceListHashMap.get(1).getText().split(":");
            productInformationHashMap.put(exTax[0].trim(), exTax[1].trim());
            return productInformationHashMap;
        }

    private LinkedHashMap<String,String> getProductPriceLinkedHashMap(){
        List<WebElement> productPriceListLinkedHashMap = elementUtilities.waitForVisibilityOfElements(productPrice, WaitConstants.DEFAULT_MEDIUM_WAIT);
        productInformationLinkedHashMap.put("Price Of Product", productPriceListLinkedHashMap.get(0).getText().trim());
        String[] exTax = productPriceListLinkedHashMap.get(1).getText().split(":");
        productInformationLinkedHashMap.put(exTax[0].trim(), exTax[1].trim());
        return productInformationLinkedHashMap;
    }

    private TreeMap<String, String> getProductPriceTreeMap(){
        List<WebElement> productPriceListTreeMap = elementUtilities.waitForVisibilityOfElements(productPrice, WaitConstants.DEFAULT_MEDIUM_WAIT);
        productInformationTreeMap.put("Price Of Product", productPriceListTreeMap.get(0).getText().trim());
        String[] exTax = productPriceListTreeMap.get(1).getText().split(":");
        productInformationTreeMap.put(exTax[0].trim(), exTax[1].trim());
        return productInformationTreeMap;
    }

    }

