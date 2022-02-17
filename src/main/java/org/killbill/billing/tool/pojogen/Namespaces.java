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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

class Namespaces {
  public static String id(String namespace, String name){
    if(namespace == null) namespace = "";
    if(name == null)  name = "";
    if(namespace.length() > 0){
      if(name.startsWith( namespace + ".")){
        return name.substring(namespace.length() + 1);
      }
    }
    return name; 
  }
  public static String namespace(String name, String id){
    if(name == null) name = "";
    if(id== null) id = "";
    if(id.length() > 0){
      if(name.endsWith( "." + id)){
        return name.substring(0, name.length() - id.length() - 1);
      }
    }
    return name;
  }
  public static String name(String namespace, String id){
    return join(namespace, id);
  }
  public static String join(String lhs , String rhs){
    if(lhs == null) lhs = "";
    if(rhs == null) rhs = "";
    lhs = lhs.trim();
    rhs = rhs.trim();
    if( lhs.length() == 0 ) return rhs;
    if( rhs.length() == 0 ) return lhs;
    return lhs + "." + rhs;
  }
  public static String root(String s){
    return  split(s).get(0);
  }
  public static boolean belongs(String  namespace , String name){
    if(namespace == null) namespace="";
    if(name == null) name="";
    if(namespace.length() > 0 ) namespace = namespace + ".";
    return name.startsWith( namespace);
  }
  public static List<String> split(String s){
   return new ArrayList<String>(Arrays.asList(s.split("\\.")));
  }
  public static List<String> path(String  namespace , String name){
    List<String> path = split(namespace);
    String file =  id(namespace, name);
    path.add(file + ".java");
    return path;
  }
  public static String key(String namespace, String name){
    return  root(id(namespace,name));
  }
}
