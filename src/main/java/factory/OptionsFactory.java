package factory;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class OptionsFactory {
    Properties prop;

    public OptionsFactory(Properties prop){
        this.prop =prop;

    }

    // Reads a property by checking -D system property first,
    // then falling back to the config .properties file value.
    // This allows GitHub Actions workflow flags like
    // -Dheadless=true and -Dincognito=true to override
    // whatever is set in default/dev/qa/stage/uat config files.
    private String resolve(String key) {
        String systemValue = System.getProperty(key);
        return systemValue != null ? systemValue : prop.getProperty(key);
    }

    public ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        // --no-sandbox and --disable-dev-shm-usage are required on
        // GitHub Actions Linux runners to prevent Chrome from crashing
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        if (Boolean.parseBoolean(resolve("headless"))) {
            chromeOptions.addArguments("--headless=new");
        }
        if (Boolean.parseBoolean(resolve("incognito"))) {
            chromeOptions.addArguments("--incognito");
        }
        return chromeOptions;
    }

    public FirefoxOptions getFirefoxOptions() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        if (Boolean.parseBoolean(resolve("headless"))) {
            firefoxOptions.addArguments("--headless");
        }
        if (Boolean.parseBoolean(resolve("incognito"))) {
            firefoxOptions.addArguments("-private");
        }
        return firefoxOptions;
    }

    public EdgeOptions getEdgeOptions() {
        EdgeOptions edgeOptions = new EdgeOptions();
        // --no-sandbox and --disable-dev-shm-usage are required on
        // GitHub Actions Linux runners to prevent Edge from crashing
        edgeOptions.addArguments("--no-sandbox");
        edgeOptions.addArguments("--disable-dev-shm-usage");
        if (Boolean.parseBoolean(resolve("headless"))) {
            edgeOptions.addArguments("--headless=new");
        }
        if (Boolean.parseBoolean(resolve("incognito"))) {
            edgeOptions.addArguments("-inprivate");
        }
        return edgeOptions;
    }



}
