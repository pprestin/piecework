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

import java.util.concurrent.TimeUnit;

import org.testng.annotations.*;
import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import com.google.inject.Binder;
import com.google.inject.Module;

import org.openqa.selenium.firefox.FirefoxDriver;

public class E2eModuleFactory implements IModuleFactory {

   public static class FirefoxDriverModule implements Module {
       @Override
       public void configure(Binder binder) {
           WebDriver driver = new FirefoxDriver();
           driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
           binder.bind(WebDriver.class).toInstance(driver);
       }
   }

   public static class ChromeDriverModule implements Module {
       @Override
       public void configure(Binder binder) {
           WebDriver driver = new ChromeDriver();
           driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
           binder.bind(WebDriver.class).toInstance(driver);
       }
   }

   public static class InternetExplorerDriverModule implements Module {
       @Override
       public void configure(Binder binder) {
           WebDriver driver = new InternetExplorerDriver();
           driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
           binder.bind(WebDriver.class).toInstance(driver);
       }
   }

 /**
   * @param context The current test context
   * @param testClass The test class
   *
   * @return The Guice module that should be used to get an instance of this
   * test class.
   */
  @Override
  public Module createModule(ITestContext context, Class<?> testClass) {
      if ( System.getProperty("fx") != null || System.getProperty("firefox") != null ) {
          return new FirefoxDriverModule();
      } else if ( System.getProperty("ie") != null )  {
          return new InternetExplorerDriverModule();
      } else if ( System.getProperty("ch") != null || System.getProperty("chrome") != null )  {
          return new ChromeDriverModule();
      } else {
          return new FirefoxDriverModule();
      }
  }
}
