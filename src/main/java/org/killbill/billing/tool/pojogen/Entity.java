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

import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;

public class Entity {

    protected final String export;
    protected final String id;
    protected final String moniker;
    protected final String name;
    protected final String namespace;
    protected final String reference;

    public Entity(ResolvedTypeDeclaration declaration) {
        this(declaration.getPackageName(), declaration.getQualifiedName());
    }

    public Entity(String name) {
        this(name.substring(0, name.lastIndexOf('.')), name);
    }

    public Entity(Entity that) {
        this(that.namespace, that.name);
    }

    public Entity(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
        this.id = name.substring(this.namespace.length() + 1);
        String[] path = this.id.split("\\.");
        this.reference = path[0];
        this.moniker = path[path.length - 1];
        this.export = this.namespace + "." + this.reference;
    }

    public String getExport() {
        return this.export;
    }

    public String getId() {
        return this.id;
    }

    public String getMoniker() {
        return this.moniker;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getReference() {
        return this.reference;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
