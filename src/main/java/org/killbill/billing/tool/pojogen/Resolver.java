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

public class Resolver extends Unit {

    private final Entity base;
    private final List<Implementation> implementations;

    public Resolver(Entity entity, List<String> imports, Mapping mapping, Symbols symbols,
                    Entity base, List<Implementation> implementations) {
        super(entity, imports, mapping, symbols);
        this.implementations = implementations;
        this.base = base;
    }

    public List<Implementation> getImplementations() {
        return this.implementations;
    }

    public Entity getBase() {
        return this.base;
    }

    public static Resolver create(Configuration configuration, Symbols symbols, String namespace, List<Implementation> implementations) {

        String name = Namespaces.join(namespace, configuration.getResolver());
        Entity entity = new Entity(namespace, name);
        Entity base = new Entity("com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver");
        Importer importer = new Importer(entity, symbols);
        importer.add(base);
        importer.addJavaDefaults();
        for (Implementation implementation : implementations) {
            importer.add(implementation);
            importer.add(implementation.getBase());
        }
        List<String> imports = importer.getImports();
        Mapping mapping = importer.getMapping();
        symbols = importer.getSymbols();
        return new Resolver(entity, imports, mapping, symbols, base, implementations);
    }
}
