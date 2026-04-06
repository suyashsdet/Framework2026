package utils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElementUtil{
    WebDriver driver;

    public ElementUtil(WebDriver driver){
        this.driver = driver;
    }

    /**
     * Finds and returns a single WebElement using the given locator.
     *
     * @param locator By locator of the element
     * @return WebElement found on the page
     * @throws NoSuchElementException if element is not found
     */
    public WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    /**
     * Finds and returns a list of WebElements using the given locator.
     *
     * @param locator By locator of the elements
     * @return List of WebElements (empty list if none found)
     */
    public List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Returns the count of elements matching the given locator.
     *
     * @param locator By locator of the elements
     * @return number of elements found
     */
    public int getElementsCount(By locator) {
        return findElements(locator).size();
    }

    /**
     * Clicks on the element identified by the given locator.
     *
     * @param locator By locator of the element to click
     */
    public void click(By locator) {
        findElement(locator).click();
    }

    /**
     * Clears the input field and sends the given value.
     *
     * @param locator By locator of the input field
     * @param value text to be entered
     */
    public void sendKeys(By locator, String value) {
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Returns the visible text of the element.
     *
     * @param locator By locator of the element
     * @return visible text of the element
     */
    public String getText(By locator) {
        return findElement(locator).getText();
    }

    /**
     * Returns the value of the given DOM attribute of the element.
     *
     * @param locator By locator of the element
     * @param attributeName name of the attribute
     * @return attribute value or null if attribute does not exist
     */
    public String getAttribute(By locator, String attributeName) {
        return findElement(locator).getAttribute(attributeName);
    }

    /**
     * Returns the value of the given DOM property of the element.
     * Useful for modern JavaScript-based applications.
     *
     * @param locator By locator of the element
     * @param propertyName name of the DOM property
     * @return DOM property value
     */
    public String getDomProperty(By locator, String propertyName) {
        return findElement(locator).getDomProperty(propertyName);
    }


    /**
     * Checks whether the element is displayed on the page.
     *
     * @param elementLocator By locator of the element
     * @return true if element is displayed, false otherwise
     */
    public boolean isDisplayed(By elementLocator) {
        try {
            return findElement(elementLocator).isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Checks whether exactly one element exists for the given locator.
     *
     * @param elementsLocator By locator of the elements
     * @return true if exactly one element is found, false otherwise
     */
    public boolean isUniqueElement(By elementsLocator) {
        return findElements(elementsLocator).size() == 1;
    }

    /**
     * Checks whether the number of elements matches the expected count.
     *
     * @param elementsLocator By locator of the elements
     * @param expectedCount expected number of elements
     * @return true if element count matches expected count, false otherwise
     */
    public boolean isElementCountMatching(By elementsLocator, int expectedCount) {
        return findElements(elementsLocator).size() == expectedCount;
    }

    /**
     * Checks whether the element is enabled.
     *
     * @param elementLocator By locator of the element
     * @return true if element is enabled, false otherwise
     */
    public boolean isEnabled(By elementLocator) {
        try {
            return findElement(elementLocator).isEnabled();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Checks whether the element is selected.
     *
     * @param elementLocator By locator of the element
     * @return true if element is selected, false otherwise
     */
    public boolean isSelected(By elementLocator) {
        try {
            return findElement(elementLocator).isSelected();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }


    /**
     * Returns the non-empty visible text of all elements within a given section.
     *
     * @param sectionElementsLocator By locator identifying elements inside a section
     * @return list of non-empty visible texts
     */
    public List<String> getTextsFromSection(By sectionElementsLocator) {
        List<WebElement> sectionElements = findElements(sectionElementsLocator);
        List<String> visibleTexts = new ArrayList<>();

        for (WebElement element : sectionElements) {
            String text = element.getText();
            if (!text.isEmpty()) {
                visibleTexts.add(text);
            }
        }
        return visibleTexts;
    }

    /**
     * Returns the count of links (href attributes) within a given section.
     *
     * @param sectionLinksLocator By locator identifying link elements in a section
     * @return number of links found in the section
     */
    public int getLinksCountInSection(By sectionLinksLocator) {
        List<WebElement> linkElements = findElements(sectionLinksLocator);

        for (WebElement element : linkElements) {
            element.getDomAttribute("href");
        }
        return linkElements.size();
    }

    /**
     * Returns the count of all links present on the page.
     *
     * @return number of links on the page
     */
    public int getLinksCountInPage() {
        List<WebElement> linkElements = findElements(By.tagName("a"));

        for (WebElement element : linkElements) {
            element.getDomAttribute("href");
        }
        return linkElements.size();
    }

    /**
     * Returns the total number of images present on the page.
     *
     * @return number of images on the page
     */
    public int getImagesCountInPage() {
        return findElements(By.tagName("img")).size();
    }

    /**
     * Returns the alt and src attributes of all images present on the page.
     *
     * @return list containing alt and src values in the format "alt : src"
     */
    public List<String> getImagesAltAndSrcInPage() {
        List<WebElement> imageElements = findElements(By.tagName("img"));
        List<String> altAndSrcValues = new ArrayList<>();

        for (WebElement imageElement : imageElements) {
            String altAndSrc =
                    imageElement.getDomAttribute("alt") + " : " +
                            imageElement.getDomAttribute("src");
            altAndSrcValues.add(altAndSrc);
        }
        return altAndSrcValues;
    }

    /**
     * Searches through a list of elements and clicks the one
     * whose visible text matches the given value (case-insensitive).
     *
     * @param optionsLocator By locator identifying the list of options
     * @param valueToSelect visible text value to search and click
     */
    public void selectValueFromDynamicList(By optionsLocator, String valueToSelect) {
        List<WebElement> optionElements = findElements(optionsLocator);

        for (WebElement optionElement : optionElements) {
            if (optionElement.getText().equalsIgnoreCase(valueToSelect)) {
                optionElement.click();
                break;
            }
        }
    }

    /**
     * Returns the visible text of all elements found using the given locator.
     *
     * @param optionsLocator By locator identifying the list of options
     * @return list of visible texts for all options
     */
    public List<String> getAllOptionsFromDynamicList(By optionsLocator) {
        List<WebElement> optionElements = findElements(optionsLocator);
        List<String> optionTexts = new ArrayList<>();

        for (WebElement optionElement : optionElements) {
            optionTexts.add(optionElement.getText());
        }
        return optionTexts;
    }

    /**
     * Selects a dropdown option by index.
     *
     * @param dropDownLocator By locator of the dropdown element
     * @param optionIndex index of the option to select
     * @return true if selection is successful, false otherwise
     */
    public boolean selectDropDownByIndex(By dropDownLocator, int optionIndex) {
        try {
            Select select = new Select(findElement(dropDownLocator));
            select.selectByIndex(optionIndex);
            return true;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Selects a dropdown option by visible text.
     *
     * @param dropDownLocator By locator of the dropdown element
     * @param visibleText visible text of the option to select
     * @return true if selection is successful, false otherwise
     */
    public boolean selectDropDownByVisibleText(By dropDownLocator, String visibleText) {
        try {
            Select select = new Select(findElement(dropDownLocator));
            select.selectByVisibleText(visibleText);
            return true;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Selects a dropdown option by value attribute.
     *
     * @param dropDownLocator By locator of the dropdown element
     * @param valueAttribute value attribute of the option to select
     * @return true if selection is successful, false otherwise
     */
    public boolean selectDropDownByValue(By dropDownLocator, String valueAttribute) {
        try {
            Select select = new Select(findElement(dropDownLocator));
            select.selectByValue(valueAttribute);
            return true;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Returns the visible text of all options in a select dropdown.
     *
     * @param dropDownLocator By locator of the dropdown element
     * @return list of visible texts of all dropdown options
     */
    public List<String> getAllSelectDropDownOptions(By dropDownLocator) {
        Select select = new Select(findElement(dropDownLocator));
        List<WebElement> optionElements = select.getOptions();
        List<String> optionTexts = new ArrayList<>();

        for (WebElement optionElement : optionElements) {
            optionTexts.add(optionElement.getText());
        }
        return optionTexts;
    }

    /**
     * Selects one or more values from a jQuery-style dropdown.
     * Use "all" to select all available choices.
     *
     * @param dropDownOpenerLocator By locator to open the dropdown
     * @param choicesLocator By locator identifying dropdown choices
     * @param valuesToSelect values to select (or "all")
     */
    public void selectChoicesFromJQueryDropdown(
            By dropDownOpenerLocator,
            By choicesLocator,
            String... valuesToSelect) {

        findElement(dropDownOpenerLocator).click();
        List<WebElement> choiceElements = findElements(choicesLocator);

        if (valuesToSelect.length > 0 &&
                valuesToSelect[0].trim().equalsIgnoreCase("all")) {

            for (WebElement choiceElement : choiceElements) {
                choiceElement.click();
            }

        } else {
            for (WebElement choiceElement : choiceElements) {
                for (String value : valuesToSelect) {
                    if (choiceElement.getText().trim().equalsIgnoreCase(value)) {
                        choiceElement.click();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Selects a dropdown value by visible text using Select class.
     *
     * @param dropDownLocator By locator of the dropdown element
     * @param valueToSelect visible text value to select
     * @return true if value is found and selected, false otherwise
     */
    public boolean selectDropDownValue(By dropDownLocator, String valueToSelect) {
        boolean isSelected = false;

        Select select = new Select(findElement(dropDownLocator));
        List<WebElement> optionElements = select.getOptions();

        for (WebElement optionElement : optionElements) {
            if (optionElement.getText().equalsIgnoreCase(valueToSelect)) {
                optionElement.click();
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }

    /**
     * Selects a value from a non-select dropdown (custom HTML dropdown).
     *
     * @param optionsLocator By locator identifying dropdown options
     * @param valueToSelect visible text value to select
     * @return true if value is found and selected, false otherwise
     */
    public boolean selectNonSelectDropDownValue(By optionsLocator, String valueToSelect) {
        boolean isSelected = false;

        List<WebElement> optionElements = findElements(optionsLocator);
        for (WebElement optionElement : optionElements) {
            if (optionElement.getText().equalsIgnoreCase(valueToSelect)) {
                optionElement.click();
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }


    /**
     * Performs the given Selenium Action.
     *
     * @param action Selenium Action to be performed
     */
    public void performAction(Action action) {
        action.perform();
    }




    /**
     * Moves the mouse pointer to the specified element.
     *
     * @param elementLocator By locator of the element
     */
    public void moveToElement(By elementLocator) {
        Actions actions = new Actions(driver);
        actions.moveToElement(findElement(elementLocator)).perform();
    }

    /**
     * Handles a parent menu and clicks on its sub-menu using mouse actions.
     *
     * @param parentMenuLocator By locator of the parent menu
     * @param subMenuLocator By locator of the sub-menu
     */
    public void handleParentSubMenu(By parentMenuLocator, By subMenuLocator) {
        Actions actions = new Actions(driver);
        click(parentMenuLocator);
        actions.moveToElement(findElement(subMenuLocator))
                .click()
                .perform();
    }

    /**
     * Handles a four-level menu structure by sequential mouse interactions.
     *
     * <p>Note: This method uses hard waits (Thread.sleep) intentionally,
     * as provided in the original implementation.</p>
     *
     * @param levelOneLocator By locator of first-level menu
     * @param levelTwoLocator By locator of second-level menu
     * @param levelThreeLocator By locator of third-level menu
     * @param levelFourLocator By locator of fourth-level menu
     * @throws InterruptedException if the thread sleep is interrupted
     */
    public void handleFourLevelMenu(
            By levelOneLocator,
            By levelTwoLocator,
            By levelThreeLocator,
            By levelFourLocator) throws InterruptedException {

        click(levelOneLocator);
        Thread.sleep(3000);
        moveToElement(levelTwoLocator);
        Thread.sleep(3000);
        moveToElement(levelThreeLocator);
        Thread.sleep(3000);
        click(levelFourLocator);
    }

    /**
     * Performs a click action on the element using Selenium Actions.
     *
     * @param elementLocator By locator of the element
     */
    public void actionsClick(By elementLocator) {
        Actions actions = new Actions(driver);
        actions.click(findElement(elementLocator)).perform();
    }

    /**
     * Sends keys to an element using Selenium Actions.
     *
     * @param elementLocator By locator of the element
     * @param textToEnter text to be entered
     */
    public void actionsSendKeys(By elementLocator, String textToEnter) {
        Actions actions = new Actions(driver);
        actions.sendKeys(findElement(elementLocator), textToEnter).perform();
    }

    /**
     * Scrolls the page up partially using keyboard actions.
     */
    public void scrollPageUp() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.PAGE_UP).perform();
    }

    /**
     * Scrolls the page down partially using keyboard actions.
     */
    public void scrollPageDown() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.PAGE_DOWN).perform();
    }

    /**
     * Scrolls to the footer of the page.
     */
    public void scrollToFooter() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.COMMAND).sendKeys(Keys.END).perform();
    }

    /**
     * Scrolls to the header of the page.
     */
    public void scrollToHeader() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.COMMAND).sendKeys(Keys.HOME).perform();
    }

    /**
     * Scrolls to the specified element using Selenium Actions.
     *
     * @param elementLocator By locator of the element
     */
    public void scrollToElement(By elementLocator) {
        Actions actions = new Actions(driver);
        actions.scrollToElement(findElement(elementLocator)).perform();
    }

    /**
     * Sends keys to an element character-by-character with a pause between each key.
     *
     * @param elementLocator By locator of the element
     * @param textToEnter text to be entered
     * @param pauseInMillis pause duration between keystrokes (in milliseconds)
     */
    public void actionsSendKeysWithPause(
            By elementLocator,
            String textToEnter,
            int pauseInMillis) {

        Actions actions = new Actions(driver);
        for (char character : textToEnter.toCharArray()) {
            actions.sendKeys(findElement(elementLocator), String.valueOf(character))
                    .pause(pauseInMillis)
                    .perform();
        }
    }

    /**
     * Waits until the element is present in the DOM.
     *
     * @param elementLocator By locator of the element
     * @param timeoutInSeconds maximum wait time in seconds
     * @return WebElement once present in DOM
     */
    public WebElement waitForPresenceOfElement(By elementLocator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.presenceOfElementLocated(elementLocator));
    }

    /**
     * Waits until the element is visible on the page.
     *
     * @param elementLocator By locator of the element
     * @param timeoutInSeconds maximum wait time in seconds
     * @return WebElement once visible
     */
    public WebElement waitForVisibilityOfElement(By elementLocator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(elementLocator));
    }

    /**
     * Waits until the element is clickable.
     *
     * @param elementLocator By locator of the element
     * @param timeoutInSeconds maximum wait time in seconds
     * @return WebElement once clickable
     */
    public WebElement waitForElementToBeClickable(By elementLocator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(elementLocator));
    }

    /**
     * Waits until the element is visible on the page and returns it.
     *
     * Use this method when you want to perform custom actions
     * (click, sendKeys, getText, etc.) after the wait.
     *
     * @param locator By locator of the element
     * @param timeoutInSeconds maximum time to wait in seconds
     * @return visible WebElement
     */
    public WebElement getVisibleElement(By locator, int timeoutInSeconds) {
        return waitForVisibilityOfElement(locator, timeoutInSeconds);
    }

    /**
     * Waits until the element becomes visible and then clicks on it.
     *
     * This method combines explicit wait and click into a single action
     * to avoid visibility-related issues.
     *
     * @param locator By locator of the element
     * @param timeoutInSeconds maximum time to wait in seconds
     */
    public void clickWhenVisible(By locator, int timeoutInSeconds) {
        waitForVisibilityOfElement(locator, timeoutInSeconds).click();
    }

    /**
     * Waits until the element becomes visible, clears any existing value,
     * and types the given text into the element.
     *
     * @param locator By locator of the element
     * @param text text to be entered
     * @param timeoutInSeconds maximum time to wait in seconds
     */
    public void typeWhenVisible(By locator, String text, int timeoutInSeconds) {
        WebElement element = waitForVisibilityOfElement(locator, timeoutInSeconds);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Waits until the element becomes clickable and then clicks on it.
     *
     * Use this method when visibility alone is not enough,
     * such as for dynamically enabled buttons.
     *
     * @param locator By locator of the element
     * @param timeoutInSeconds maximum time to wait in seconds
     */
    public void clickWhenReady(By locator, int timeoutInSeconds) {
        waitForElementToBeClickable(locator, timeoutInSeconds).click();
    }


    /**
     * Waits until all matching elements are present in the DOM.
     *
     * @param elementsLocator By locator of the elements
     * @param timeoutInSeconds maximum wait time in seconds
     * @return list of WebElements or empty list if timeout occurs
     */
    public List<WebElement> waitForPresenceOfElements(
            By elementsLocator, int timeoutInSeconds) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        try {
            return wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(elementsLocator));
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    /**
     * Waits until all matching elements are visible.
     *
     * @param elementsLocator By locator of the elements
     * @param timeoutInSeconds maximum wait time in seconds
     * @return list of visible WebElements or empty list if timeout occurs
     */
    public List<WebElement> waitForVisibilityOfElements(
            By elementsLocator, int timeoutInSeconds) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(elementsLocator));
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    /**
     * Waits until an alert is present.
     *
     * @param timeoutInSeconds maximum wait time in seconds
     * @return Alert once it is present
     */
    public Alert waitForAlert(int timeoutInSeconds) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    /**
     * Accepts an alert after waiting for it to be present.
     *
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void acceptAlert(int timeoutInSeconds) {
        waitForAlert(timeoutInSeconds).accept();
    }

    /**
     * Dismisses an alert after waiting for it to be present.
     *
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void dismissAlert(int timeoutInSeconds) {
        waitForAlert(timeoutInSeconds).dismiss();
    }

    /**
     * Returns the text of an alert after waiting for it to be present.
     *
     * @param timeoutInSeconds maximum wait time in seconds
     * @return alert message text
     */
    public String getAlertText(int timeoutInSeconds) {
        return waitForAlert(timeoutInSeconds).getText();
    }

    /**
     * Sends keys to an alert after waiting for it to be present.
     *
     * @param textToEnter text to send to the alert
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void sendKeysToAlert(String textToEnter, int timeoutInSeconds) {
        waitForAlert(timeoutInSeconds).sendKeys(textToEnter);
    }

    /**
     * Waits for a frame to be available using a locator and switches to it.
     *
     * @param frameLocator By locator identifying the frame
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void waitForFrameAndSwitch(By frameLocator, int timeoutInSeconds) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * Waits for a frame to be available using a WebElement and switches to it.
     *
     * @param frameElement WebElement representing the frame
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void waitForFrameAndSwitch(WebElement frameElement, int timeoutInSeconds) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameElement));
    }

    /**
     * Waits for a frame to be available using its index and switches to it.
     *
     * @param frameIndex index of the frame
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void waitForFrameAndSwitch(int frameIndex, int timeoutInSeconds) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameIndex));
    }

    /**
     * Waits for a frame to be available using its id or name and switches to it.
     *
     * @param frameIdOrName id or name attribute of the frame
     * @param timeoutInSeconds maximum wait time in seconds
     */
    public void waitForFrameAndSwitch(String frameIdOrName, int timeoutInSeconds) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameIdOrName));
    }

    /**
     * Waits until the page title contains the given partial text.
     *
     * @param partialTitle expected partial title text
     * @param timeoutInSeconds maximum wait time in seconds
     * @return current page title if condition is met, null otherwise
     */
    public String waitForPartialTitle(String partialTitle, int timeoutInSeconds) {
        try {
            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.titleContains(partialTitle));
            return driver.getTitle();
        } catch (TimeoutException exception) {
            return null;
        }
    }

    /**
     * Waits until the page URL contains the given partial text.
     *
     * @param partialUrl expected partial URL text
     * @param timeoutInSeconds maximum wait time in seconds
     * @return current page URL if condition is met, null otherwise
     */
    public String waitForPartialURL(String partialUrl, int timeoutInSeconds) {
        try {
            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.urlContains(partialUrl));
            return driver.getCurrentUrl();   // ✅ FIXED
        } catch (TimeoutException exception) {
            return null;
        }
    }

    /**
     * Waits until the page title exactly matches the given text.
     *
     * @param expectedTitle expected full page title
     * @param timeoutInSeconds maximum wait time in seconds
     * @return current page title if condition is met, null otherwise
     */
    public String waitForFullTitle(String expectedTitle, int timeoutInSeconds) {
        try {
            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.titleIs(expectedTitle));
            return driver.getTitle();
        } catch (TimeoutException exception) {
            return null;
        }
    }

    /**
     * Waits until the page URL exactly matches the given URL.
     *
     * @param expectedUrl expected full URL
     * @param timeoutInSeconds maximum wait time in seconds
     * @return current page URL if condition is met, null otherwise
     */
    public String waitForFullURL(String expectedUrl, int timeoutInSeconds) {
        try {
            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.urlToBe(expectedUrl));
            return driver.getCurrentUrl();   // ✅ FIXED
        } catch (TimeoutException exception) {
            return null;
        }
    }


    /**
     * Waits for an element to become visible using FluentWait.
     *
     * @param elementLocator By locator of the element
     * @param timeoutInSeconds maximum wait time in seconds
     * @param pollingIntervalInSeconds polling interval in seconds
     * @return visible WebElement
     */
    public WebElement waitForElementVisibleWithFluentWait(
            By elementLocator,
            int timeoutInSeconds,
            int pollingIntervalInSeconds) {

        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofSeconds(pollingIntervalInSeconds))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Element not found or not visible");

        return fluentWait.until(
                ExpectedConditions.visibilityOfElementLocated(elementLocator));
    }

    /**
     * Waits for an element to be present in the DOM using FluentWait.
     *
     * @param elementLocator By locator of the element
     * @param timeoutInSeconds maximum wait time in seconds
     * @param pollingIntervalInSeconds polling interval in seconds
     * @return WebElement once present in DOM
     */
    public WebElement waitForElementPresentWithFluentWait(
            By elementLocator,
            int timeoutInSeconds,
            int pollingIntervalInSeconds) {

        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofSeconds(pollingIntervalInSeconds))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Element not found in DOM");

        return fluentWait.until(
                ExpectedConditions.presenceOfElementLocated(elementLocator));
    }


}
