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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

public class Text{
    public static void append(StringBuilder s, int tabs, Object o ){
        String indent  =  new String(new char[tabs]).replace("\0", " ");
        s.append(String.format("%s%s\n", indent, o.toString())) ;
    }
    public static void append(StringBuilder s, Object o ){
        append(s, 0,  o);
    }
    public static void append(StringBuilder s){
        append(s, "");
    }
    public static boolean zero(String s){
        return (s == null) || (s.length() == 0);
    }
    public static String capitalize(String s) {
        if((s == null) || (s.length() == 0)){
            return "";
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    public static String quote(String s) {
        return ("\"" + ((s == null) ? "" : s) + "\"");
    }
    public static List<String> toStrings(List<?> list){
        ArrayList<String> strings = new ArrayList<String>();
        for(Object o : list){
            strings.add(Objects.toString(o));
        }
        return strings;
    }
    public static String indent(String s, int spaces){
        String prefix = "";
        for(int i = 0 ; i< spaces ; i++){
            prefix =  prefix + " ";
        }
        String[] array = s.split("\n");
        for(int i= 0 ; i < array.length ; i++){
            if(array[i].length() > 0){
                array[i] =  prefix +  array[i];
            }
        }
        return String.join("\n", array);
    }
}
