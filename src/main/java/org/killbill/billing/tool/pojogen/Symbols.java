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
import java.util.Collection;
import java.util.HashSet;

public class Symbols extends HashSet<String>{
    static final String[] JAVA = new String[]{
        /* Java keywords */
        "abstract",	"continue",	"for",	        "new",	        "switch",
        "assert",	"default",	"goto",	        "package",	    "synchronized",
        "boolean",	"do",	"   if",	        "private",	    "this",
        "break",	"double",	"implements",	"protected",	"throw",
        "byte",	    "else",	    "import",	    "public",	    "throws",
        "case",	    "enum",	    "instanceof",	"return",	    "transient",
        "catch",	"extends",	"int",	        "short",	    "try",
        "char",	    "final",	"interface",	"static",	    "void",
        "class",	"finally",	"long",	        "strictfp", 	"volatile",
        "const","   float", 	"native",	    "super",	    "while",
        /* plus */
        "true",     "false",    "null"
    };
    private static final Log log = new Log(Symbols.class);
    public Symbols(){
    }
    public Symbols(Collection<String> source){
        super(source);
    }
    public Symbols clone(){
        return new Symbols(this);
    }
    public static Symbols union(Symbols ... vargs){
        Symbols result = new Symbols();
        for( int i = 0 ; i < vargs.length ; i++){
            result.addAll( vargs[i]);
        }
        return result;
    }
    public static Symbols java(){
        return new Symbols(Arrays.asList(JAVA));
    }
}
