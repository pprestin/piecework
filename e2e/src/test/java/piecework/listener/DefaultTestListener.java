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

package piecework.listener;

import org.testng.ITestResult;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

public class DefaultTestListener extends TestListenerAdapter {
  @Override
  public void onTestFailure(ITestResult tr) {
    System.out.print("\033[31m");	// set red font color
    System.out.println("F");
    System.out.println(tr.getTestClass().getName() + "." + tr.getName() + " FAILED");
    System.out.print("\033[0m");	// reset to normal 
  }
 
  @Override
  public void onTestSkipped(ITestResult tr) {
    System.out.print("S");
  }
 
  @Override
  public void onTestSuccess(ITestResult tr) {
    System.out.print("*");
  }

  public void onFinish(ITestContext testContext) {
    System.out.println();
  }
}
