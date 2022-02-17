/*
 * Copyright 2022-2022 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.tool.pojogen;

public class Test {
  private static final String SUFFIX = "Test";
  private final Implementation implementation;
  public Test(Implementation implementation){
    this.implementation = implementation;
  }
  public String getId(){
    return this.implementation.getId() + SUFFIX;
  }
  public Implementation getImplementation(){
    return this.implementation;
  }
  public String getName(){
    return this.implementation.getName() + SUFFIX;
  }
  public String getNamespace(){
    return this.implementation.getNamespace() ;
  }
  public String getPackage(){
    return this.implementation.getNamespace() ;
  }
}
