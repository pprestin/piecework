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

import static org.testng.AssertJUnit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class E2eTestHelper {
    public static void fillForm(WebDriver driver, String[][] data) {
        for ( String[] e : data ) { 
            String k = e[0];
            String v = e[1];
            WebElement element = driver.findElement(By.name(k));
            String t = element.getAttribute("type");
            //System.out.println(k + "'s type =" + a);
            if ( t.startsWith("text") ) {
                element.sendKeys(v);
            } else if ( t.equals("hidden") ) {
                WebElement e1 = element.findElement(By.xpath("../input[@data-ng-change]"));
                e1.sendKeys(v);
            } else if ( t.equals("checkbox") ) {
                element.click();
            }
        }
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

        // Wait for the page to load, timeout after 2 seconds
        (new WebDriverWait(driver, 2)).until(new ExpectedCondition<Boolean>() {
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
}
