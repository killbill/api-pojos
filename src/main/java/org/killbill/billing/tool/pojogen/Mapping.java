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

import  java.util.HashMap;
import  java.util.Map;

public class Mapping extends HashMap<String, String> { 

    private static final Log log = new Log(Mapping.class);

    public Mapping(){
    }
    public Mapping(Map<String,String> source){
        super(source);
    }
    public String resolve(Entity entity){
        return resolve(entity.getName());
    }
    public String resolve(String key){
        if(!containsKey(key)){
            return key;
        }
        return get(key);
    }
    public Mapping clone(){
        return new Mapping(this);
    }
    public Symbols getSymbols(){
        return new Symbols(this.values());
    }
    public static Mapping merge(Mapping... vargs){
        Mapping result = new Mapping();
        for(int i = 0 ; i < vargs.length ; i++){
            for (Map.Entry<String, String> entry : vargs[i].entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
