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
            // Apply -Dbrowser system property override if present
            // (VM single-browser workflow passes -Dbrowser=firefox etc.)
            // This ensures prop.getProperty("browser") is always the
            // correct final value that DriverFactory should use.
            if (System.getProperty("browser") != null) {
                browser = System.getProperty("browser");
            }
            properties.setProperty("browser", browser);
        }

        driver = driverFactory.initBrowser(properties);
        loginPage = new LoginPage(driver);
    }


    @AfterMethod(alwaysRun = true)
    public void attachScreenshot(ITestResult result){
        // -------------------------------------------------------
        // NEW vs D11: Added null check — DriverFactory.getDriver() != null
        //
        // D11 code was:
        //   if(!result.isSuccess()){
        //       ChainTestListener.embed(...)
        //   }
        //
        // The problem: if @BeforeTest setup() failed (e.g. browser
        // could not launch, or the page load timed out), the driver
        // was never initialised. ThreadLocal.get() returns null in
        // that case. Calling getScreenshotFile() on a null driver
        // would throw a NullPointerException, crashing the @AfterMethod
        // itself and hiding the real failure message in the report.
        //
        // The fix: only attempt to take a screenshot when the driver
        // is actually alive. If it is null, we skip silently — the
        // test is already marked as failed by TestNG anyway.
        // -------------------------------------------------------
        if(!result.isSuccess() && DriverFactory.getDriver() != null){
            ChainTestListener.embed(DriverFactory.getScreenshotFile(), "image/png");
        }
    }

    @AfterTest
    public void tearDown(){
        // -------------------------------------------------------
        // NEW vs D11: Added null check before calling driver.quit()
        //
        // D11 code was:
        //   driver.quit();
        //
        // The problem: if @BeforeTest setup() threw an exception
        // (e.g. invalid browser name, config file not found, or
        // page load timeout), the driver field was never assigned.
        // Calling driver.quit() on a null reference throws
        // NullPointerException in teardown, which pollutes the
        // test report with a secondary error that is unrelated to
        // the actual root cause.
        //
        // The fix: guard with a null check so teardown is always
        // safe to call regardless of whether setup succeeded.
        // -------------------------------------------------------
        if(DriverFactory.getDriver() != null){
            driver.quit();
        }
    }
}
