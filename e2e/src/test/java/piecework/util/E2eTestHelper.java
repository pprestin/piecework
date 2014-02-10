/*
 * Copyright 2014 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package piecework.util;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;
import org.testng.annotations.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

public class E2eTestHelper {
    public static final long MILLISECONDS_IN_ONE_DAY = 24*60*60*1000;

    public static void fillForm(WebDriver driver, String[][] data) {
        //JavascriptExecutor jse = (JavascriptExecutor) driver;
        for ( String[] e : data ) { 
            for (int i=0; i<3; ++i) {
                try {
                    String k = e[0];
                    String v = e[1];
                    WebElement element = driver.findElement(By.name(k));
                    String tagName = element.getTagName();
                    String t = element.getAttribute("type");
                    //System.out.println(k + "'s type =" + a);
                    if ( tagName.equals("input") ) {
                        if ( t.equals("hidden") ) {
                            WebElement e1 = element.findElement(By.xpath("../input[@data-ng-change]"));
                            e1.sendKeys(v);
                        } else if ( t.equals("checkbox") ) {
                            element.click();
                        } else if ( t.equals("radio") ) {
                            if ( v != null && !v.isEmpty() ) {
                                WebElement el = element.findElement(By.xpath("//input[@name='" + k + "' and @type='radio' and @value='"+v+"']"));
                                el.click();
                            } else {
                                element.click();
                            }
                        } else {  // "text", "date", "datetime" etc.
                            element.sendKeys(org.openqa.selenium.Keys.HOME); // need this for field with inputmask
                            element.sendKeys(v);
                        }
                    } else if ( tagName.startsWith("text") ) {
                        element.sendKeys(v);
                    } else if ( tagName.equals("select") ) {
                        if ( v != null && !v.isEmpty() ) {
                            WebElement el = element.findElement(By.xpath(".//option[@value='"+v+"']"));
                            el.click();
                        } else {
                            WebElement el = element.findElement(By.xpath(".//option[1]"));
                            el.click();
                        }
                    }
                    break;
                } catch ( Exception ex ) {
                    try {
                        Thread.sleep(1000);   // wait a bit, then try again
                    } catch (InterruptedException ex1) {
                    }
                }
            }  // inner loop
        } // outer loop
    }

    // verify form fields
    public static void verifyForm(WebDriver driver, String[][] data) {
        for ( String[] e : data ) { 
            String k = e[0];
            String v = e[1];
            boolean verified = false;
            for (int i=0; i<3; ++i) {
                try {
                    WebElement element = driver.findElement(By.xpath(k));
                    String tagName = element.getTagName();
                    String actual =  element.getText();
                    if ( (actual == null || !actual.equals(v) ) && ( null != element.getAttribute("value") ) ) {
                        actual = element.getAttribute("value");
                    }
                    //System.out.println("k="+k+", v="+v+", tag="+tagName+", actual="+actual);
                    assertEquals( actual, v);
                    verified = true;
                    break;
                } catch ( Exception ex) {
                    try {
                        Thread.sleep(1000);   // wait a bit, then try again
                    } catch (InterruptedException ex1) {
                    }
                }
            }  // inner loop
            assertTrue(verified, "could not find element <"+k + ">, expected value: " + v);
        }  // outer loop
    }

    public static void clickButton(WebDriver driver, String buttonValue) {
        WebElement button = driver.findElement(By.xpath("//button[@value='" + buttonValue + "']"));
        if ( button != null ) {
            button.click();
            //System.out.println("clicked button with value " + buttonValue);
        }
    }

    public static String getRequestId(WebDriver driver) {
        String requestId = null;

        // Wait for the page to load, timeout after 5 seconds
        (new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getCurrentUrl().indexOf("requestId=") > 0;
            }
        });

        String url = driver.getCurrentUrl();
        int idx = url.indexOf("requestId=");
        if ( idx > 0 ) {
            requestId = url.substring(idx + "requestId=".length());
//            System.out.println("CurrentUrl is : " + url+", requestId="+requestId);
        }

        return requestId;
    }

    public static void clickTaskLink(WebDriver driver, String baseUrl, String linkText) {
        driver.get(baseUrl);
        WebElement anchor = driver.findElement(By.partialLinkText(linkText));
        if ( anchor != null ) {
            String taskUrl = anchor.getAttribute("href");
            //System.out.println("taskUrl is : " + taskUrl);
            anchor.click();
        }
    }

    public static void assignTask(WebDriver driver) {
       try {
        WebElement assignToMeBtn  = driver.findElement(By.xpath("//button[@data-ng-click='claim()']"));
        if ( assignToMeBtn != null ) {
            assignToMeBtn.click();
            Thread.sleep(1000);   // need to wait here to avoid StaleElementReferenceException
        }
        } catch ( org.openqa.selenium.ElementNotVisibleException e) {
            // task is already assigned.
        } catch (InterruptedException e) {
            // task is already assigned.
        }
    }

    // misc helper methods
    // chrome with date picker requires "MM/dd/yyyy\tHH:mma"
    // but firefox and ie, without date picker, requires "yyyy/MM/dd HH:mm a".
    public static String getDate(int days, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date dt = new Date();
        dt.setTime(dt.getTime() + days*MILLISECONDS_IN_ONE_DAY);
        return dateFormat.format(dt);
    }

    // get date usig default date  format
    public static String getDate(int days) {
        return getDate(days, "yyyy/MM/dd");  // default date format
    }

    public static String getCurrentDate() {
        return getDate(0);
    }

    public static String getCurrentDate(String format) {
        return getDate(0, format);
    }

    // get current datetime usig default format
    public static String getCurrentDateTime() {
        return getCurrentDateTime("yyyy/MM/dd HH:mm a");  // default datetime format
    }

    public static String getCurrentDateTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    public static void waitForUserLogin(WebDriver driver, String patialLoginStr, int timeoutInSeconds) {
        if ( driver == null || patialLoginStr == null || patialLoginStr.length() < 3 ) {
            return;
        }

        if ( timeoutInSeconds < 1 ) {
            timeoutInSeconds = 1;
        }

        String url = driver.getCurrentUrl();
        System.out.println(url);
        if ( url.indexOf(patialLoginStr) > 0 ) { 
            try {
                for (int i=0; i<timeoutInSeconds && url.indexOf(patialLoginStr) > 0; ++i) {
                    url = driver.getCurrentUrl();
                    //System.out.println(url);
                    Thread.sleep(1000);
                }   
                Thread.sleep(1000);   // additional wait for redrection
            } catch (InterruptedException e) {
            }   
        } 
    }

    // convience function
    public static void sleep(Integer seconds) {
        // sanity check
        if ( seconds == null || seconds <= 0 || seconds > 60) {
           return;
        }

        try {
            Thread.sleep(1000*seconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep() {
        Integer wait = Integer.getInteger("w");
        if ( wait == null ) {
             wait = Integer.getInteger("wait");
        }

        sleep(wait);
    }
}
