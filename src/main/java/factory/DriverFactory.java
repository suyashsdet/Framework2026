package factory;

import constants.FileLocationConstants;
import exceptions.BrowserExceptions;
import exceptions.EnvironmentExceptions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
// NEW vs D11: Duration import added to support the new pageLoadTimeout call below.
import java.time.Duration;
import java.util.Properties;

public class DriverFactory {


    WebDriver driver;
    Properties prop;
    FileInputStream fileInputStream;
    public static ThreadLocal<WebDriver> threadLocal = new ThreadLocal<WebDriver>();



    public WebDriver initBrowser(Properties prop) {

        // -------------------------------------------------------
        // NEW vs D11: Browser resolution now checks the JVM system
        // property -Dbrowser first (passed by GitHub Actions via
        // the workflow's "browser" input parameter).
        //
        // D11 code was simply:
        //   String browser = prop.getProperty("browser");
        //
        // Framework2026 code:
        //   If -Dbrowser is set (e.g. GitHub Actions passes
        //   -Dbrowser=firefox), use that value.
        //   If -Dbrowser is NOT set (local run), fall back to
        //   whatever is written in the .properties config file.
        //
        // This allows the GitHub Actions workflow to control the
        // browser via a dropdown without editing any config file.
        // -------------------------------------------------------
        // prop.getProperty("browser") is the single source of truth.
        // Priority:
        //   1. XML <parameter name="browser"> set by BaseTest via properties.setProperty()
        //      which already accounts for -Dbrowser system property override in BaseTest.
        //   2. System.getProperty("browser") as final fallback (local runs, no XML param)
        String browser = prop.getProperty("browser") != null
                ? prop.getProperty("browser")
                : System.getProperty("browser");

        OptionsFactory optionsFactory = new OptionsFactory(prop);

        // -------------------------------------------------------
        // NEW vs VM workflows: Remote WebDriver support for Docker.
        //
        // When tests run against a Selenium Docker container, a
        // SELENIUM_REMOTE_URL system property is passed by the
        // GitHub Actions workflow instead of launching a local browser.
        //
        // Single-browser Docker workflow passes:
        //   -DSELENIUM_REMOTE_URL=http://localhost:4444/wd/hub
        //
        // Multi-browser Docker workflow passes per-browser URLs:
        //   -DSELENIUM_REMOTE_URL_CHROME=http://localhost:4444/wd/hub
        //   -DSELENIUM_REMOTE_URL_FIREFOX=http://localhost:4445/wd/hub
        //   -DSELENIUM_REMOTE_URL_EDGE=http://localhost:4446/wd/hub
        //
        // Resolution order:
        //   1. Per-browser URL (SELENIUM_REMOTE_URL_CHROME etc.)
        //   2. Generic URL    (SELENIUM_REMOTE_URL)
        //   3. null           → fall through to local driver below
        // -------------------------------------------------------
        // browser is already resolved above (system property → config file → XML parameter via BaseTest)
        // so we use it directly here — do NOT re-read System.getProperty("browser") again
        String remoteUrl = System.getProperty("SELENIUM_REMOTE_URL_" + browser.trim().toUpperCase());
        if (remoteUrl == null) remoteUrl = System.getProperty("SELENIUM_REMOTE_URL");

        if (remoteUrl != null) {
            try {
                switch (browser.trim().toLowerCase()) {
                    case "chrome":
                        threadLocal.set(new RemoteWebDriver(new URL(remoteUrl), optionsFactory.getChromeOptions()));
                        break;
                    case "firefox":
                        threadLocal.set(new RemoteWebDriver(new URL(remoteUrl), optionsFactory.getFirefoxOptions()));
                        break;
                    case "edge":
                        threadLocal.set(new RemoteWebDriver(new URL(remoteUrl), optionsFactory.getEdgeOptions()));
                        break;
                    default:
                        throw new BrowserExceptions("Invalid Browser");
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid SELENIUM_REMOTE_URL: " + remoteUrl, e);
            }
        } else {
            switch (browser.trim().toLowerCase()) {
                case "chrome":
                    threadLocal.set(new ChromeDriver(optionsFactory.getChromeOptions()));
                    break;
                case "firefox":
                    threadLocal.set(new FirefoxDriver(optionsFactory.getFirefoxOptions()));
                    break;
                case "edge":
                    threadLocal.set(new EdgeDriver(optionsFactory.getEdgeOptions()));
                    break;
                default:
                    throw new BrowserExceptions("Invalid Browser");
            }
        }
        getDriver().manage().window().maximize();
        getDriver().manage().deleteAllCookies();

        // -------------------------------------------------------
        // NEW vs D11: Page load timeout was not set in D11 at all.
        //
        // Without this, Selenium waits up to 300 seconds (5 min)
        // by default for a page to load. In a parallel CI run that
        // means one slow/hanging page can block an entire thread
        // for 5 minutes before failing.
        //
        // Setting it to 60 seconds means: if the URL does not
        // finish loading within 60 seconds, Selenium throws a
        // TimeoutException immediately. The test fails fast and
        // the thread is freed for the next test.
        //
        // This is especially important in GitHub Actions where
        // runner resources are shared and timeouts are costly.
        // -------------------------------------------------------
        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        getDriver().get(prop.getProperty("url"));
        return getDriver();
    }


    public static  WebDriver getDriver(){
        return threadLocal.get();
    }


    public Properties initProperties() {

       String envName =  System.getProperty("env");

       if(envName==null){
           try {
               fileInputStream = new FileInputStream(FileLocationConstants.DEFAULT_CONFIG_FILE_PATH);
           } catch (FileNotFoundException e) {
               throw new RuntimeException(e);
           }
       }
       else {
           switch(envName){
               case "qa":
                   try {
                       fileInputStream = new FileInputStream(FileLocationConstants.QA_CONFIG_FILE_PATH);
                   } catch (FileNotFoundException ex) {
                       throw new RuntimeException(ex);
                   }
                   break;
               case "dev":
                   try {
                       fileInputStream = new FileInputStream(FileLocationConstants.DEV_CONFIG_FILE_PATH);
                   } catch (FileNotFoundException ex) {
                       throw new RuntimeException(ex);
                   }
                   break;
               case "stage":
                   try {
                       fileInputStream = new FileInputStream(FileLocationConstants.STAGE_CONFIG_FILE_PATH);
                   } catch (FileNotFoundException ex) {
                       throw new RuntimeException(ex);
                   }
                   break;
               case "uat":
                   try {
                       fileInputStream = new FileInputStream(FileLocationConstants.UAT_CONFIG_FILE_PATH);
                   } catch (FileNotFoundException ex) {
                       throw new RuntimeException(ex);
                   }
                   break;
               default:
                   throw new EnvironmentExceptions("Invalid Environment");
           }
           }


        prop = new Properties();

        try {
            prop.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return prop;
    }

    public static File getScreenshotFile(){
        return  ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

    }

    public static byte[] getScreenshotBytes(){
        return  ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BYTES);

    }
    public static String getScreenshotBase64(){
        return  ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BASE64);

    }

}
