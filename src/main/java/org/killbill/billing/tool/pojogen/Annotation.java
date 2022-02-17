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

import java.util.HashMap;
import java.util.Map; 

public class Annotation {

  private static final Log log = new Log(Annotation.class);

  private final String name;
  private final Mapping mapping;
  private final String content;

  public Annotation(String name, Mapping mapping){
    this(name, null, mapping);
  }
  public Annotation(String name, String content, Mapping mapping){
    this.name = name;
    this.mapping = new Mapping(mapping);
    this.content = content;
  }
  @Override
  public String toString(){
    return String.format("@%s%s",
         this.mapping.resolve(this.name),
         (this.content ==  null) ? "" : String.format("(%s)", this.content));
  }
}
