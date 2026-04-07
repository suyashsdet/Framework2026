# Framework2026 — Selenium Test Automation Framework

A production-grade Java test automation framework built with **Selenium 4**, **TestNG**, **Maven**, and a fully automated **CI/CD pipeline** using **GitHub Actions**.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 24 | Programming language |
| Selenium | 4.41.0 | Browser automation |
| TestNG | 7.12.0 | Test execution and management |
| Maven | 3.x | Build and dependency management |
| Allure | 2.33.0 | Interactive test reporting |
| ChainTest | 1.0.12 | Dark theme HTML + email reports |
| Apache POI | 5.5.1 | Excel data driven testing |
| OpenCSV | 5.12.0 | CSV data driven testing |
| AspectJ | 1.9.25.1 | AOP weaving for Allure |

---

## Framework Architecture

```
Framework2026/
├── src/main/java/
│   ├── constants/        # AppConstants, WaitConstants, FileLocationConstants
│   ├── error/            # Custom error messages
│   ├── exceptions/       # BrowserExceptions, EnvironmentExceptions
│   ├── factory/          # DriverFactory, OptionsFactory
│   ├── helpers/          # StringHelper utilities
│   ├── listeners/        # TestAllureListener, AnnotationTransformer, Retry
│   └── pages/            # Page Object Model classes
│       ├── components/   # Reusable page components
│       ├── LoginPage
│       ├── AccountsPage
│       ├── SearchResultsPage
│       ├── ProductInformationPage
│       ├── RegistrationPage
│       └── LogoutPage
├── src/main/resources/
│   └── config/           # Environment config files
│       ├── default.config.properties
│       ├── dev.config.properties
│       ├── qa.config.properties
│       ├── stage.config.properties
│       └── uat.config.properties
├── src/test/java/
│   ├── base/             # BaseTest — setup, teardown, screenshot
│   └── tests/            # Test classes
└── src/test/resources/
    ├── testdata/         # Excel and CSV test data files
    ├── testrunners/      # TestNG XML suite files
    ├── allure.properties
    └── chaintest.properties
```

---

## Key Design Patterns

### Page Object Model (POM)
Every page of the application has its own Java class. Test classes never interact with Selenium directly — they only call page object methods. This separates test logic from UI interaction logic.

### Factory Pattern
`DriverFactory` creates the correct `WebDriver` instance based on the browser parameter. `OptionsFactory` builds the correct browser options (headless, incognito, sandbox flags) for each browser type.

### Data Driven Testing
Tests read input data from external sources:
- **Excel** via Apache POI — `registrationTestWithExcelData`
- **CSV** via OpenCSV — `registrationTestWithCSVData`
- **TestNG DataProvider** — inline data providers for map-based tests

### Multi Environment Support
`DriverFactory.initProperties()` reads `System.getProperty("env")` to load the matching config file. Passing `-Denv=qa` loads `qa.config.properties` with QA-specific URL and credentials. No code changes needed to switch environments.

### Thread Safe WebDriver
`DriverFactory.threadLocal` stores one `WebDriver` per thread using `ThreadLocal<WebDriver>`. This allows parallel test execution across multiple browsers without drivers interfering with each other.

### Retry Mechanism
`Retry` listener implements `IRetryAnalyzer`. Failed tests automatically retry up to a configured number of times before being marked as failed. `AnnotationTransformer` applies the retry analyser to all tests automatically.

---

## Running Tests Locally

### Default environment, Chrome browser
```bash
mvn clean test
```

### Specific environment
```bash
mvn clean test -Denv=qa
mvn clean test -Denv=dev
mvn clean test -Denv=stage
mvn clean test -Denv=uat
```

### Specific browser
```bash
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=edge
```

### Headless mode
```bash
mvn clean test -Dheadless=true
```

### Specific XML suite
```bash
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_all_test_default_sequential.xml
```

### Generate Allure report
```bash
mvn allure:report
# Report generated at: target/site/allure-maven-plugin/index.html
```

---

## TestNG XML Suites

| XML File | Browsers | Execution |
|---|---|---|
| `testng_all_test_default_parallel.xml` | Single (from config/flag) | Parallel |
| `testng_all_test_default_sequential.xml` | Single (from config/flag) | Sequential |
| `testng_all_test_default_parallel_multi_browser.xml` | Chrome + Firefox + Edge | Parallel |
| `testng_all_test_default_sequential_multi_browser.xml` | Chrome + Firefox + Edge | Sequential |

---

## CI/CD Pipeline — GitHub Actions

The framework has a fully automated CI/CD pipeline with **4 GitHub Actions workflows**. Each workflow is documented below.

---

### 1. Lint Check — Line Length

**File:** `.github/workflows/lint-check.yml`
**Trigger:** Automatic — every push and pull request to any branch

**What it does:**
Scans every `.java` file in the project and checks that no line exceeds **120 characters**. This enforces code readability across all editors and code review tools.

**Why 120 characters:**
- Keeps code readable in split-screen editors without horizontal scrolling
- Makes code review easier in GitHub PR diff view
- Follows widely accepted Java coding standards

**What you see when it fails:**
```
❌ FILE : ./src/main/java/pages/ProductInformationPage.java
   LINE : 91
   CHARS: 153 (exceeds limit by 33 characters)
```

**What you see when it passes:**
```
✅ LINT PASSED
   Total files scanned : 12
   Total violations    : 0
```

**How to fix a violation:**
- Split method parameters across multiple lines
- Extract a long expression into a named variable
- Break a method chain at the `.` onto the next line

---

### 2. Build Check — Maven Compile

**File:** `.github/workflows/build-check.yml`
**Trigger:** Automatic — every push and pull request to any branch

**What it does:**
Runs `mvn compile` to verify all Java source files compile successfully. Acts as the **first gate** in the CI pipeline — if code does not compile, there is no point running tests.

**What `mvn compile` does:**
1. Reads `pom.xml` to understand project structure
2. Downloads all dependencies from Maven Central if not cached
3. Compiles all `.java` files under `src/main/java` and `src/test/java`
4. Outputs `.class` files into `target/`

**Dependency caching:**
The `~/.m2/repository` folder is cached using `pom.xml` as the cache key. This reduces build time from ~3 minutes to ~20 seconds on repeat runs.

**What you see when it fails:**
```
[ERROR] LoginPage.java:[15,8] cannot find symbol
BUILD FAILURE
```

**What you see when it passes:**
```
BUILD SUCCESS
Total time: 18.3 s
```

---

### 3. Run Tests — Single Browser

**File:** `.github/workflows/run-tests-single-browser.yml`
**Trigger:** Manual — via "Run workflow" button on GitHub Actions tab

**What it does:**
Runs the full TestNG test suite on a single browser of your choice. After tests finish, generates 3 reports and publishes them to GitHub Pages as live URLs.

**Input parameters (all dropdowns/checkboxes):**

| Parameter | Type | Options | Description |
|---|---|---|---|
| `env` | Dropdown | default / dev / qa / stage / uat | Which config file to load |
| `browser` | Dropdown | chrome / firefox / edge | Which browser to use |
| `xml_file` | Dropdown | parallel / sequential | Which TestNG XML suite |
| `headless` | Checkbox | true / false | Run without visible window |
| `incognito` | Checkbox | true / false | Run with no cookies/cache |
| `use_matrix` | Checkbox | true / false | Enable parallel VM execution |

**Parallel VM mode (`use_matrix = true`):**
Splits 5 test classes across 2 GitHub VMs running simultaneously:
- VM 1 → `LoginPageTest` + `AccountsPageTest` + `SearchResultsPageTest`
- VM 2 → `ProductInformationPageTest` + `RegistrationPageTest`

Expected time saving: ~50% faster than single VM.

**Reports published to GitHub Pages:**
- 📊 Allure Report — `https://suyashsdet.github.io/Framework2026/allure/`
- 🔗 ChainTest Report — `https://suyashsdet.github.io/Framework2026/chaintest/`
- ✅ TestNG Report — `https://suyashsdet.github.io/Framework2026/testng/`

---

### 4. Run Tests — Multi Browser

**File:** `.github/workflows/run-tests-multi-browser.yml`
**Trigger:** Manual — via "Run workflow" button on GitHub Actions tab

**What it does:**
Runs the full TestNG test suite across Chrome, Firefox, and Edge. After tests finish, generates 3 reports and publishes them to GitHub Pages.

**Why no browser dropdown:**
The multi-browser XML files hardcode Chrome, Firefox, and Edge inside `<parameter name="browser">` tags. These values are read directly by `BaseTest.setup()` and override any `-Dbrowser=` flag. A browser dropdown would be misleading and ignored — intentionally excluded.

**Input parameters:**

| Parameter | Type | Options | Description |
|---|---|---|---|
| `env` | Dropdown | default / dev / qa / stage / uat | Which config file to load |
| `xml_file` | Dropdown | parallel_multi / sequential_multi | Which XML suite (single VM only) |
| `headless` | Checkbox | true / false | Run without visible windows |
| `incognito` | Checkbox | true / false | Run with no cookies/cache |
| `use_matrix` | Checkbox | true / false | Enable parallel VM per browser |

**Parallel VM mode (`use_matrix = true`):**
Each browser gets its own dedicated GitHub VM running simultaneously:
- VM 1 → Chrome — full test suite
- VM 2 → Firefox — full test suite
- VM 3 → Edge — full test suite

All 3 VMs start at the same time. Total time = slowest browser, not sum of all 3.
Expected time saving: ~66% faster than single VM.

Results from all 3 VMs are merged into one unified Allure report showing all browsers side by side.

**Reports published to GitHub Pages:**
- 📊 Allure Report — `https://suyashsdet.github.io/Framework2026/allure/`
- 🔗 ChainTest Report — `https://suyashsdet.github.io/Framework2026/chaintest/`
- ✅ TestNG Report — `https://suyashsdet.github.io/Framework2026/testng/`

---

## CI/CD Pipeline Overview

```
Every Push / PR
│
├── lint-check.yml ──────────────────── checks 120 char line limit
└── build-check.yml ─────────────────── verifies mvn compile passes

Manual Trigger
│
├── run-tests-single-browser.yml
│   ├── use_matrix=false → 1 VM → all tests → reports → GitHub Pages
│   └── use_matrix=true  → 2 VMs in parallel → merge → reports → GitHub Pages
│
└── run-tests-multi-browser.yml
    ├── use_matrix=false → 1 VM → Chrome+Firefox+Edge → reports → GitHub Pages
    └── use_matrix=true  → 3 VMs in parallel (1 per browser) → merge → reports → GitHub Pages
```

---

## Reports

All reports are published to GitHub Pages after every test run and accessible via live URL — no download needed.

| Report | URL | What it shows |
|---|---|---|
| Landing page | `https://suyashsdet.github.io/Framework2026/` | Links to all 3 reports |
| Allure | `https://suyashsdet.github.io/Framework2026/allure/` | Dashboard, timeline, per-test details, screenshots |
| ChainTest | `https://suyashsdet.github.io/Framework2026/chaintest/` | Dark theme interactive report + email summary |
| TestNG | `https://suyashsdet.github.io/Framework2026/testng/` | Pass/fail per test method with stack traces |

---

## Author

**Suyash Mudholkar**
SDET — Selenium Test Automation Engineer
