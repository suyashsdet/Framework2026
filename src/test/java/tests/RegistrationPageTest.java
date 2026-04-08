package tests;

import base.BaseTest;
import constants.FileLocationConstants;
import helpers.StringHelper;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.CSVUtil;
import utils.ExcelUtil;

public class RegistrationPageTest extends BaseTest {

    @BeforeClass
    public void registrationPageSetup() {
        registrationPage = loginPage.goToRegisterPage();

    }

    @AfterMethod(alwaysRun = true)
    public void resetRegistrationPage() {
        // -------------------------------------------------------
        // NEW vs D11: Added a null guard at the top of this method.
        //
        // D11 code jumped straight into:
        //   userComponent = registrationPage.navigateToUserDropDown();
        //
        // The problem: if @BeforeClass registrationPageSetup() failed
        // (e.g. the browser timed out loading the page, or loginPage
        // itself was null because BaseTest.setup() threw), then
        // registrationPage is null. Calling .navigateToUserDropDown()
        // on null throws NullPointerException inside @AfterMethod,
        // which creates a second fake failure in the report and hides
        // the real root cause.
        //
        // The fix: if registrationPage is null, return immediately.
        // The test is already marked as failed by TestNG — there is
        // nothing to reset, so we exit cleanly.
        // -------------------------------------------------------
        if (registrationPage == null) return;
        userComponent = registrationPage.navigateToUserDropDown();
        logoutPage = userComponent.navigateToLogoutPage();
        homePage = logoutPage.clickOnContinue();
        userComponent = homePage.navigateToUserDropDown();
        registrationPage = userComponent.navigateToRegistrationPage();
    }

    @DataProvider(name = "registrationData")
    public Object[][] registrationData() {
        return new Object[][] {{"John", "Doe", "9999999999", "Pass@123", "Pass@123", "Yes", "Yes"},
                {"Mike", "Smith", "8888888888", "Test@123", "Test@123", "Yes", "Yes"},
                {"Sara", "Williams", "7777777777", "Abc@1234", "Abc@1234", "Yes", "yes"}};
    }

    @Test(dataProvider = "registrationData")
    public void registrationTest(
            String firstName,
            String lastName,
            String telephone,
            String password,
            String confirmPassword,
            String subscribe,
            String agree) {

        accountsPage = registrationPage.doRegister(firstName, lastName, StringHelper.getRandomEmailAddress(),
                telephone, password, confirmPassword, subscribe, agree);

        Assert.assertTrue(accountsPage.validateAccountCreation(),"Registration Failed");
    }

    @DataProvider(name = "registrationDataFromExcel")
    public Object[][] getDataFromExcelForUserRegistration() {
        return ExcelUtil.getData(FileLocationConstants.REGISTRATION_SHEET_NAME);

    }

    @DataProvider(name = "registrationDataFromCSV")
    public Object[][] getDataFromCSVForUserRegistration() {
        return CSVUtil.csvData("Registration Data");

    }



    @Test(dataProvider = "registrationDataFromExcel")
    public void registrationTestWithExcelData(
            String firstName,
            String lastName,
            String telephone,
            String password,
            String subscribe) {

        accountsPage = registrationPage.doRegister(firstName, lastName, StringHelper.getRandomEmailAddress(),
                telephone, password, password, subscribe, "yes");

        Assert.assertTrue(accountsPage.validateAccountCreation(),"Registration Failed");
    }



    @Test(dataProvider = "registrationDataFromCSV")
    public void registrationTestWithCSVData(
            String firstName,
            String lastName,
            String telephone,
            String password,
            String subscribe) {

         accountsPage= registrationPage.doRegister(firstName, lastName, StringHelper.getRandomEmailAddress(),
                telephone, password, password, subscribe, "yes");
        Assert.assertTrue(accountsPage.validateAccountCreation(),"Registration Failed");

    }

}
