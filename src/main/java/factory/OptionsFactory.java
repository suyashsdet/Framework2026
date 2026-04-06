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

    public ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();


        if (Boolean.parseBoolean(prop.getProperty("headless"))) {
            chromeOptions.addArguments("--headless");
        }
        if (Boolean.parseBoolean(prop.getProperty("incognito"))) {
            chromeOptions.addArguments("--incognito");
        }
        return chromeOptions;

    }

    public FirefoxOptions getFirefoxOptions() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();




        if (Boolean.parseBoolean(prop.getProperty("headless"))) {
            firefoxOptions.addArguments("-headless");
        }
        if (Boolean.parseBoolean(prop.getProperty("incognito"))) {
            firefoxOptions.addArguments("-private");
        }

        return firefoxOptions;
    }

    public EdgeOptions getEdgeOptions() {
        EdgeOptions edgeOptions = new EdgeOptions();

        if (Boolean.parseBoolean(prop.getProperty("headless"))) {
            edgeOptions.addArguments("--headless=new");
        }
        if (Boolean.parseBoolean(prop.getProperty("incognito"))) {
            edgeOptions.addArguments("-inprivate");
        }

        return edgeOptions;
    }



}
