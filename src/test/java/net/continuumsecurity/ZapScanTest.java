/*
 * Copyright (c) 2014 ContinuumSecurity www.continuumsecurity.net
 *
 * The contents of this file are subject to the GNU Affero General Public
 * License version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Initial Developer of the Original Code is ContinuumSecurity.
 * Portions created by ContinuumSecurity are Copyright (C)
 * ContinuumSecurity SLNE. All Rights Reserved.
 *
 * Contributor(s): Stephen de Vries
 */

package net.continuumsecurity;

import net.continuumsecurity.proxy.ScanningProxy;
import net.continuumsecurity.proxy.Spider;
import net.continuumsecurity.proxy.ZAProxyScanner;
import net.continuumsecurity.web.drivers.DriverFactory;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.zaproxy.clientapi.core.Alert;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class ZapScanTest {
    static Logger log = Logger.getLogger(ZapScanTest.class.getName());
    private final static String ZAP_PROXYHOST = "localhost";
    private final static int ZAP_PROXYPORT = 8888;
    private final static String ZAP_APIKEY = null;

    // Change this to the appropriate driver for the OS, alternatives in the drivers directory
    private final static String CHROME_DRIVER_PATH = "drivers/chromedriver.exe";
    private final static String MEDIUM = "MEDIUM";
    private final static String HIGH = "HIGH";
    private ScanningProxy zapScanner;
    private Spider zapSpider;
    private WebDriver driver;
    private MyAppNavigation myApp;
    private final static String[] policyNames = {"directory-browsing","cross-site-scripting","sql-injection","path-traversal","remote-file-inclusion","server-side-include",
            "script-active-scan-rules","server-side-code-injection","external-redirect","crlf-injection"};
    int currentScanID;


    @Before
    public void setup() {
        zapScanner = new ZAProxyScanner(ZAP_PROXYHOST,ZAP_PROXYPORT,ZAP_APIKEY);
        zapScanner.clear(); //Start a new session
        zapSpider = (Spider)zapScanner;
        log.info("Created client to ZAP API");
        driver = DriverFactory.createProxyDriver("chrome",createZapProxyConfigurationForWebDriver(), CHROME_DRIVER_PATH);
        myApp = new MyAppNavigation(driver);
        myApp.registerUser(); //Doesn't matter if user already exists, bodgeit just throws an error
    }

    @After
    public void after() {
        driver.quit();
    }

    @Test
    public void testSecurityVulnerabilitiesBeforeLogin() {
        myApp.navigateBeforeLogin();
        log.info("Spidering...");
        spiderWithZap();
        log.info("Spider done.");

        setAlertAndAttackStrength();
        zapScanner.setEnablePassiveScan(true);
        scanWithZap();

        List<Alert> alerts = filterAlerts(zapScanner.getAlerts());
        logAlerts(alerts);
        assertThat(alerts.size(), equalTo(0));
    }

    @Test
    public void testSecurityVulnerabilitiesAfterLogin() {
        myApp.login();
        myApp.navigateAfterLogin();

        log.info("Spidering...");
        spiderWithZap();
        log.info("Spider done.");

        setAlertAndAttackStrength();
        zapScanner.setEnablePassiveScan(true);
        scanWithZap();

        List<Alert> alerts = filterAlerts(zapScanner.getAlerts());
        logAlerts(alerts);
        assertThat(alerts.size(), equalTo(0));
    }

    private void logAlerts(List<Alert> alerts) {
        for (Alert alert : alerts) {
            log.info("Alert: "+alert.getAlert()+" at URL: "+alert.getUrl()+" Parameter: "+alert.getParam()+" CWE ID: "+alert.getCweId());
        }
    }
    /*
        Remove false positives, filter based on risk and reliability
     */
    private List<Alert> filterAlerts(List<Alert> alerts) {
       List<Alert> filtered = new ArrayList<Alert>();
       for (Alert alert : alerts) {
           if (alert.getRisk().equals(Alert.Risk.High) && alert.getConfidence() != Alert.Confidence.Low) filtered.add(alert);
       }
       return filtered;
    }

    public void setAlertAndAttackStrength() {
        for (String policyName : policyNames) {
            String ids = enableZapPolicy(policyName);
            for (String id : ids.split(",")) {
                zapScanner.setScannerAlertThreshold(id,MEDIUM);
                zapScanner.setScannerAttackStrength(id,HIGH);
            }
        }
    }

    private void scanWithZap() {
        log.info("Scanning...");
        zapScanner.scan(myApp.BASE_URL);
        currentScanID = zapScanner.getLastScannerScanId();
        int complete = 0;
        while (complete < 100) {
            complete = zapScanner.getScanProgress(currentScanID);
            log.info("Scan is " + complete + "% complete.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Scanning done.");
    }

    private String enableZapPolicy(String policyName) {
        String scannerIds = null;
        switch (policyName.toLowerCase()) {
            case "directory-browsing":
                scannerIds = "0";
                break;
            case "cross-site-scripting":
                scannerIds = "40012,40014,40016,40017";
                break;
            case "sql-injection":
                scannerIds = "40018";
                break;
            case "path-traversal":
                scannerIds = "6";
                break;
            case "remote-file-inclusion":
                scannerIds = "7";
                break;
            case "server-side-include":
                scannerIds = "40009";
                break;
            case "script-active-scan-rules":
                scannerIds = "50000";
                break;
            case "server-side-code-injection":
                scannerIds = "90019";
                break;
            case "remote-os-command-injection":
                scannerIds = "90020";
                break;
            case "external-redirect":
                scannerIds = "20019";
                break;
            case "crlf-injection":
                scannerIds = "40003";
                break;
            case "source-code-disclosure":
                scannerIds = "42,10045,20017";
                break;
            case "shell-shock":
                scannerIds = "10048";
                break;
            case "remote-code-execution":
                scannerIds = "20018";
                break;
            case "ldap-injection":
                scannerIds = "40015";
                break;
            case "xpath-injection":
                scannerIds = "90021";
                break;
            case "xml-external-entity":
                scannerIds = "90023";
                break;
            case "padding-oracle":
                scannerIds = "90024";
                break;
            case "el-injection":
                scannerIds = "90025";
                break;
            case "insecure-http-methods":
                scannerIds = "90028";
                break;
            case "parameter-pollution":
                scannerIds = "20014";
                break;
            default : throw new RuntimeException("No policy found for: "+policyName);
        }
        if (scannerIds == null) throw new RuntimeException("No matching policy found for: " + policyName);
        zapScanner.setEnableScanners(scannerIds, true);
        return scannerIds;
    }

    private static Proxy createZapProxyConfigurationForWebDriver() {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(ZAP_PROXYHOST + ":" + ZAP_PROXYPORT);
        proxy.setSslProxy(ZAP_PROXYHOST + ":" + ZAP_PROXYPORT);
        return proxy;
    }

    private void spiderWithZap() {
        zapSpider.excludeFromSpider(myApp.LOGOUT_URL);
        zapSpider.setThreadCount(5);
        zapSpider.setMaxDepth(5);
        zapSpider.setPostForms(false);
        zapSpider.spider(myApp.BASE_URL);
        int spiderID = zapSpider.getLastSpiderScanId();
        int complete  = 0;
        while (complete < 100) {
            complete = zapSpider.getSpiderProgress(spiderID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (String url : zapSpider.getSpiderResults(spiderID)) {
            log.info("Found URL: "+url);
        }
    }
}
