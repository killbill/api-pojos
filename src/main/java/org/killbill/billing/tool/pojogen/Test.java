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

import java.util.List;

public class Test extends Unit {
    private final static String SUFFIX = "Test";
    private final Implementation target;

    public Test(Entity entity, List<String> imports, Mapping mapping, Symbols symbols,
            Implementation target)
    {
        super(entity, imports, mapping, symbols);
        this.target = target;
    }
    public Implementation getTarget(){
        return this.target;
    }
    public static Test create(Configuration configuration, Symbols symbols, Implementation implementation){

        String namespace = implementation.getNamespace();
        String name = implementation.getName() + SUFFIX;
        Entity entity = new Entity(namespace, name);

        Importer importer = new Importer(entity, symbols);
        importer.addJavaDefaults();
        importer.add(implementation);
        importer.add(implementation.getBase());

        importer.add("org.testng.Assert");
        importer.add("org.testng.annotations.Test");
        importer.add("com.fasterxml.jackson.core.JsonParseException");
        importer.add("com.fasterxml.jackson.core.JsonProcessingException");
        importer.add("com.fasterxml.jackson.databind.JsonMappingException");
        importer.add("com.fasterxml.jackson.databind.ObjectMapper");
        importer.add("com.fasterxml.jackson.databind.SerializationFeature");
        importer.add("com.fasterxml.jackson.datatype.joda.JodaModule");
        importer.add("com.fasterxml.jackson.databind.util.StdDateFormat");
        importer.add("java.io.IOException");

        List<String> imports  = importer.getImports();
        Mapping mapping  = importer.getMapping();
        symbols =  importer.getSymbols();

        return new Test(entity, imports, mapping, symbols, implementation);
    }
}
