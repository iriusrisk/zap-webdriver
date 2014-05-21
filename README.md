zap-webdriver
=============
Example security tests using JUnit, Selenium WebDriver and OWASP ZAP to test the Bodgeit store (https://code.google.com/p/bodgeit/)
The tests use selenium to navigate and login to the app, then spider the content with ZAP and perform a security scan using ZAP's scanner.  Tests pass or fail based on vulnerabilities found.

Getting started
===============
1. Download and start the [bodgeit store](https://code.google.com/p/bodgeit/) on port 8080
2. Download and start [OWASP ZAP](https://code.google.com/p/zaproxy/wiki/Downloads?tm=2) at least version 2.3.0.1
3. In the ZAP Options change the local proxy port to 8888
4. Download this repository
5. Run: mvn test

Details
=======
The Selenium steps to navigate the application and submit forms is contained in the MyAppNavigation class.  The JUnit testing steps are defined in ZapScanTest.
Keeping these two aspects separate makes test maintenance easier.  If your testing team already has Selenium code to perform navigation (e.g. Page Objects), you can then drop those in to the MyAppNavigation class.

The ZapScanTest class should be regarded as a starting point for your own test cases and it makes some wild assumptions about which alerts to ignore.  If you're going to use these tests as
part of a Continuous Integration/Continuous Delivery process then please make sure that the build will fail for important security vulnerabilities.

For a more comprehensive security testing framework with security requirements specified in plain English and many more pre-written tests, consider the [BDD-Security framework](http://www.continuumsecurity.net/bdd-intro.html) instead.




