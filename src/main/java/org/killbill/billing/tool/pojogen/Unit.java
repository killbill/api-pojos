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
import java.util.List;
import java.util.Map;

public abstract class Unit extends Entity {

    private static final Log log = new Log(Unit.class);

    protected final Mapping mapping;
    protected final Symbols symbols;
    protected final List<String> imports;

    public Unit(Entity entity, List<String> imports, Mapping mapping, Symbols symbols) {
        super(entity);
        this.imports = imports;
        this.mapping = mapping;
        this.symbols = symbols;
    }

    public List<String> getImports() {
        return this.imports;
    }

    public Symbols getSymbols() {
        return this.symbols;
    }

    public Map<String, String> declare(String... vargs) {
        Map<String, String> map = new HashMap<String, String>();
        int attempts = 0;
        while (true) {
            map.clear();
            boolean valid = true;
            for (int i = 0; i < vargs.length; i++) {
                String name = newVarName(vargs[i], attempts);
                map.put(vargs[i], name);
                if (this.symbols.contains(name)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                break;
            }
            attempts++;
        }
        return map;
    }

    protected String newVarName(String name, int attempts) {
        if (attempts > 0) {
            name = name + attempts;
        }
        return name;
    }

    public String type(String name) {
        return this.mapping.resolve(name);
    }

    public String type(Entity entity) {
        return this.mapping.resolve(entity);
    }
}
