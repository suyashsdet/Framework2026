package base;

import com.aventstack.chaintest.plugins.ChainTestListener;
import factory.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.*;
import pages.components.UserComponent;

import java.util.Properties;


@Listeners(ChainTestListener.class)
public class BaseTest {

    WebDriver driver;
    DriverFactory driverFactory;
    protected Properties properties;


    protected UserComponent userComponent;

    protected LoginPage loginPage;
    protected AccountsPage accountsPage;
    protected SearchResultsPage searchResultsPage;
    protected ProductInformationPage productInformationPage;
    protected RegistrationPage registrationPage;
    protected LogoutPage logoutPage;
    protected HomePage homePage;




    @Parameters("browser")
    @BeforeTest
    public void setup(@Optional("chrome") String browser){
        driverFactory = new DriverFactory();


        properties = driverFactory.initProperties();

        if(browser!=null){
            properties.setProperty("browser", browser);
        }

        driver = driverFactory.initBrowser(properties);
        loginPage = new LoginPage(driver);
    }


    @AfterMethod(alwaysRun = true)
    public void attachScreenshot(ITestResult result){
        if(!result.isSuccess() && DriverFactory.getDriver() != null){
            ChainTestListener.embed(DriverFactory.getScreenshotFile(), "image/png");
        }
    }

    @AfterTest
    public void tearDown(){
        if(DriverFactory.getDriver() != null){
            driver.quit();
        }
    }
}
