package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class Retry implements IRetryAnalyzer {
    private int count = 0;
    private static final int MAX_TRY = 3;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (count < MAX_TRY) {
            count++;
            return true;
        }
        return false;
    }
}