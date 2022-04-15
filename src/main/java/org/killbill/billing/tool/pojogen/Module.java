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

public class Module extends Unit {

    private final Entity base;
    private final Resolver resolver;

    public Module(Entity entity, List<String> imports, Mapping mapping, Symbols symbols, 
            Entity base, Resolver resolver)
    {
        super(entity, imports, mapping, symbols);
        this.resolver = resolver;
        this.base = base;
    }
    public Entity getBase(){
        return this.base;
    }
    public Resolver getResolver(){
        return this.resolver;
    }
    public static Module create(Configuration configuration, Symbols symbols, Resolver resolver){
        String namespace = resolver.getNamespace();
        String name = Namespaces.join(namespace, configuration.getModule());
        Entity entity = new Entity(namespace, name);
        Entity base = new Entity("com.fasterxml.jackson.databind.module.SimpleModule");
        Importer importer = new Importer(entity, symbols);
        importer.add(base);
        importer.add(resolver);
        importer.addJavaDefaults();
        List<String> imports  = importer.getImports();
        Mapping mapping  = importer.getMapping();
        symbols =  importer.getSymbols();
        return new Module(entity, imports, mapping, symbols, base, resolver);
    }
}
