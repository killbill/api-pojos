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

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.*;

public class Importer {

    private static final Log log = new Log(Importer.class);

    private final Symbols symbols;
    private final Entity entity;
    private final Map<String, Entity> entities;
    private final Set<String> namespaces;

    public Importer(Entity entity, Symbols symbols) {
        this.entity = entity;
        this.symbols = new Symbols(symbols);
        this.entities = new HashMap<String, Entity>();
        this.namespaces = new HashSet<String>();
        this.namespaces.add("java.lang");
        this.namespaces.add(this.entity.getNamespace());
        this.add(this.entity);
    }

    public void add(Entity entity) {
        this.add(entity.getNamespace(), entity.getName());
    }

    public void add(String namespace, String name) {
        if (!this.entities.containsKey(name)) {
            this.entities.put(name, new Entity(namespace, name));
        }
    }

    public void add(String name) {
        this.add(name.substring(0, name.lastIndexOf('.')), name);
    }

    public void add(ResolvedType type) {
        if (type.isReferenceType()) {
            ResolvedReferenceType reference = type.asReferenceType();
            Optional<ResolvedReferenceTypeDeclaration> optional = reference.getTypeDeclaration();
            if (optional.isPresent()) {
                if (optional.get().hasName()) {
                    this.add(optional.get().getPackageName(), optional.get().getQualifiedName());
                }
            }
        }
    }

    public void addAll(List<ResolvedType> types) {
        for (ResolvedType type : types) {
            this.add(type);
        }
    }

    public List<String> getImports() {
        HashSet<String> imports = new HashSet<String>();
        HashMap<String, String> mapping = new HashMap<String, String>();
        resolve(this.entities, this.namespaces, this.symbols, imports, mapping);
        List<String> list = new ArrayList<String>(imports);
        Collections.sort(list);
        return list;
    }

    public Symbols getSymbols() {
        HashSet<String> imports = new HashSet<String>();
        HashMap<String, String> mapping = new HashMap<String, String>();
        resolve(this.entities, this.namespaces, this.symbols, imports, mapping);
        return Symbols.union(this.symbols, new Symbols(mapping.values()));
    }

    public Mapping getMapping() {
        HashSet<String> imports = new HashSet<String>();
        HashMap<String, String> mapping = new HashMap<String, String>();
        resolve(this.entities, this.namespaces, this.symbols, imports, mapping);
        return new Mapping(mapping);
    }

    public void addJavaDefaults() {
        addJavaDefaults(this);
    }

    @Override
    public String toString() {
        HashSet<String> imports = new HashSet<String>();
        HashMap<String, String> mapping = new HashMap<String, String>();
        resolve(this.entities, this.namespaces, this.symbols, imports, mapping);
        return toString(imports, mapping, Symbols.union(this.symbols, new Symbols(mapping.values())));
    }

    private static Map<String, Map<String, List<Entity>>> partition(Map<String, Entity> entities) {
        Map<String, Map<String, List<Entity>>> output = new HashMap<String, Map<String, List<Entity>>>();
        for (Entity entity : entities.values()) {
            if (!output.containsKey(entity.getReference())) {
                output.put(entity.getReference(), new HashMap<String, List<Entity>>());
            }
            if (!output.get(entity.getReference()).containsKey(entity.getExport())) {
                output.get(entity.getReference()).put(entity.getExport(), new ArrayList<Entity>());
            }
            output.get(entity.getReference()).get(entity.getExport()).add(entity);
        }
        return output;
    }

    private static void resolve(
            Map<String, Entity> entities,
            Set<String> namespaces, Set<String> symbols,
            Set<String> imports, Map<String, String> map) {
        Map<String, Map<String, List<Entity>>> partitions = partition(entities);

        for (String index : partitions.keySet()) {
            for (String reference : partitions.get(index).keySet()) {
                for (Entity entity : partitions.get(index).get(reference)) {
                    if ((partitions.get(index).size() > 1) || symbols.contains(index)) {
                        map.put(entity.getName(), entity.getName());
                    } else {
                        if (!namespaces.contains(entity.getNamespace())) {
                            imports.add(reference);
                        }
                        map.put(entity.getName(), entity.getId());
                    }
                }
            }
        }
    }

    public static String toString(Set<String> imports, Map<String, String> mapping, Set<String> symbols) {
        StringBuilder s = new StringBuilder();
        for (String namespace : imports) {
            s.append("import " + namespace + ";\n");
        }
        for (String name : mapping.keySet()) {
            s.append(name + " -> " + mapping.get(name) + "\n");
        }
        for (String symbol : symbols) {
            s.append(symbol + "\n");
        }
        return s.toString();
    }

    private static void addJavaDefaults(Importer importer) {

        importer.add("java.lang", "java.lang.Appendable");
        importer.add("java.lang", "java.lang.AutoCloseable");
        importer.add("java.lang", "java.lang.CharSequence");
        importer.add("java.lang", "java.lang.Cloneable");
        importer.add("java.lang", "java.lang.Comparable");
        importer.add("java.lang", "java.lang.Iterable");
        importer.add("java.lang", "java.lang.Readable");
        importer.add("java.lang", "java.lang.Runnable");
        importer.add("java.lang", "java.lang.Thread.UncaughtExceptionHandler");

        importer.add("java.lang", "java.lang.Boolean");
        importer.add("java.lang", "java.lang.Byte");
        importer.add("java.lang", "java.lang.Character");
        importer.add("java.lang", "java.lang.Character.Subset");
        importer.add("java.lang", "java.lang.Character.UnicodeBlock");
        importer.add("java.lang", "java.lang.Class");
        importer.add("java.lang", "java.lang.ClassLoader");
        importer.add("java.lang", "java.lang.ClassValue");
        importer.add("java.lang", "java.lang.Compiler");
        importer.add("java.lang", "java.lang.Double");
        importer.add("java.lang", "java.lang.Enum");
        importer.add("java.lang", "java.lang.Float");
        importer.add("java.lang", "java.lang.InheritableThreadLocal");
        importer.add("java.lang", "java.lang.Integer");
        importer.add("java.lang", "java.lang.Long");
        importer.add("java.lang", "java.lang.Math");
        importer.add("java.lang", "java.lang.Number");
        importer.add("java.lang", "java.lang.Object");
        importer.add("java.lang", "java.lang.Package");
        importer.add("java.lang", "java.lang.Process");
        importer.add("java.lang", "java.lang.ProcessBuilder");
        importer.add("java.lang", "java.lang.ProcessBuilder.Redirect");
        importer.add("java.lang", "java.lang.Runtime");
        importer.add("java.lang", "java.lang.RuntimePermission");
        importer.add("java.lang", "java.lang.SecurityManager");
        importer.add("java.lang", "java.lang.Short");
        importer.add("java.lang", "java.lang.StackTraceElement");
        importer.add("java.lang", "java.lang.StrictMath");
        importer.add("java.lang", "java.lang.String");
        importer.add("java.lang", "java.lang.StringBuffer");
        importer.add("java.lang", "java.lang.StringBuilder");
        importer.add("java.lang", "java.lang.System");
        importer.add("java.lang", "java.lang.Thread");
        importer.add("java.lang", "java.lang.ThreadGroup");
        importer.add("java.lang", "java.lang.ThreadLocal");
        importer.add("java.lang", "java.lang.Throwable");
        importer.add("java.lang", "java.lang.Void");

        importer.add("java.lang", "java.lang.Character.UnicodeScript");
        importer.add("java.lang", "java.lang.ProcessBuilder.Redirect.Type");
        importer.add("java.lang", "java.lang.Thread.State");

        importer.add("java.lang", "java.lang.ArithmeticException");
        importer.add("java.lang", "java.lang.ArrayIndexOutOfBoundsException");
        importer.add("java.lang", "java.lang.ArrayStoreException");
        importer.add("java.lang", "java.lang.ClassCastException");
        importer.add("java.lang", "java.lang.ClassNotFoundException");
        importer.add("java.lang", "java.lang.CloneNotSupportedException");
        importer.add("java.lang", "java.lang.EnumConstantNotPresentException");
        importer.add("java.lang", "java.lang.Exception");
        importer.add("java.lang", "java.lang.IllegalAccessException");
        importer.add("java.lang", "java.lang.IllegalArgumentException");
        importer.add("java.lang", "java.lang.IllegalMonitorStateException");
        importer.add("java.lang", "java.lang.IllegalStateException");
        importer.add("java.lang", "java.lang.IllegalThreadStateException");
        importer.add("java.lang", "java.lang.IndexOutOfBoundsException");
        importer.add("java.lang", "java.lang.InstantiationException");
        importer.add("java.lang", "java.lang.InterruptedException");
        importer.add("java.lang", "java.lang.NegativeArraySizeException");
        importer.add("java.lang", "java.lang.NoSuchFieldException");
        importer.add("java.lang", "java.lang.NoSuchMethodException");
        importer.add("java.lang", "java.lang.NullPointerException");
        importer.add("java.lang", "java.lang.NumberFormatException");
        importer.add("java.lang", "java.lang.ReflectiveOperationException");
        importer.add("java.lang", "java.lang.RuntimeException");
        importer.add("java.lang", "java.lang.SecurityException");
        importer.add("java.lang", "java.lang.StringIndexOutOfBoundsException");
        importer.add("java.lang", "java.lang.TypeNotPresentException");
        importer.add("java.lang", "java.lang.UnsupportedOperationException");

        importer.add("java.lang", "java.lang.AbstractMethodError");
        importer.add("java.lang", "java.lang.AssertionError");
        importer.add("java.lang", "java.lang.BootstrapMethodError");
        importer.add("java.lang", "java.lang.ClassCircularityError");
        importer.add("java.lang", "java.lang.ClassFormatError");
        importer.add("java.lang", "java.lang.Error");
        importer.add("java.lang", "java.lang.ExceptionInInitializerError");
        importer.add("java.lang", "java.lang.IllegalAccessError");
        importer.add("java.lang", "java.lang.IncompatibleClassChangeError");
        importer.add("java.lang", "java.lang.InstantiationError");
        importer.add("java.lang", "java.lang.InternalError");
        importer.add("java.lang", "java.lang.LinkageError");
        importer.add("java.lang", "java.lang.NoClassDefFoundError");
        importer.add("java.lang", "java.lang.NoSuchFieldError");
        importer.add("java.lang", "java.lang.NoSuchMethodError");
        importer.add("java.lang", "java.lang.OutOfMemoryError");
        importer.add("java.lang", "java.lang.StackOverflowError");
        importer.add("java.lang", "java.lang.ThreadDeath");
        importer.add("java.lang", "java.lang.UnknownError");
        importer.add("java.lang", "java.lang.UnsatisfiedLinkError");
        importer.add("java.lang", "java.lang.UnsupportedClassVersionError");
        importer.add("java.lang", "java.lang.VerifyError");
        importer.add("java.lang", "java.lang.VirtualMachineError");

        importer.add("java.lang", "java.lang.Deprecated");
        importer.add("java.lang", "java.lang.Override");
        importer.add("java.lang", "java.lang.SafeVarargs");
        importer.add("java.lang", "java.lang.SuppressWarnings");
    }
}



