package listeners;

// ============================================================
// NEW FILE — does NOT exist in FrameworkD11
// ============================================================
//
// WHY THIS CLASS EXISTS:
//   FrameworkD11 had no retry logic — a flaky test that failed
//   once was immediately marked FAILED in the report.
//
//   Framework2026 introduces this Retry class so that a test
//   which fails due to a transient issue (network blip, slow
//   page load, timing issue) gets another chance before being
//   reported as a real failure.
//
// HOW IT WORKS:
//   TestNG calls retry() after every test failure.
//   - If count < MAX_TRY  → return true  → TestNG re-runs the test
//   - If count >= MAX_TRY → return false → TestNG marks it FAILED
//
//   With MAX_TRY = 3, each test can be attempted up to 3 times
//   total (1 original run + up to 2 retries... actually 3 retries
//   because count starts at 0 and increments before comparing).
//   Effectively: original attempt + 3 retries = 4 total attempts.
//
// HOW IT IS WIRED IN:
//   AnnotationTransformer.java (also new in Framework2026) calls
//   annotation.setRetryAnalyzer(Retry.class) for every @Test
//   method, so you never need to write retryAnalyzer = Retry.class
//   on any individual test.
//
// IMPORTANT — count is per-instance:
//   TestNG creates a fresh Retry instance per test method, so the
//   count resets to 0 for each test. Tests do not share state.
// ============================================================

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class Retry implements IRetryAnalyzer {

    // Tracks how many times the current test has already been retried.
    // Starts at 0. Incremented each time retry() returns true.
    private int count = 0;

    // Maximum number of retry attempts allowed per test.
    // NEW vs D11: this constant (and the entire retry mechanism) did not exist.
    // Set to 3 → a failing test will be retried up to 3 times.
    private static final int MAX_TRY = 3;

    /**
     * Called by TestNG automatically after every test failure.
     *
     * @param iTestResult  the result object for the failed test
     * @return true  → TestNG will re-run this test (retry)
     *         false → TestNG marks this test as FAILED and moves on
     *
     * NEW vs D11: This entire method is new.
     */
    @Override
    public boolean retry(ITestResult iTestResult) {
        if (count < MAX_TRY) {
            // Still within the allowed retry budget — increment and retry.
            count++;
            return true;
        }
        // Retry budget exhausted — let TestNG mark the test as FAILED.
        return false;
    }
}
