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

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {

    private static final String ISSER = "^is(?<property>[A-Z].*)$";
    private static final String GETTER = "^get(?<property>[A-Z].*)$";
    private static final String SETTER = "^set(?<property>[A-Z].*)$";

    private static final Log log = new Log(Property.class);

    private final boolean comparable;
    private final Type type;
    private final Method isser;
    private final Method getter;
    private final String field;
    private final String id;
    private final List<Method> setters;

    public Property(String id, Type type, boolean comparable, String field,
                    Method isser, Method getter, List<Method> setters) {
        this.id = id;
        this.type = type;
        this.comparable = comparable;
        this.field = field;
        this.isser = isser;
        this.getter = getter;
        this.setters = setters;
    }

    public boolean isComparable() {
        return this.comparable;
    }

    public String getField() {
        return this.field;
    }

    public Method getGetter() {
        return this.getter;
    }

    public String getId() {
        return this.id;
    }

    public Method getIsser() {
        return this.isser;
    }

    public String getName() {
        return getName(this.id);
    }

    public List<Method> getSetters() {
        return this.setters;
    }

    public Type getType() {
        return this.type;
    }

    public static String getName(String id) {
        return Character.toLowerCase(id.charAt(0)) + id.substring(1);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.type);
        s.append(" ");
        s.append(this.id);
        s.append("(");
        s.append(this.field);
        s.append(")");
        s.append(" ");
        s.append("{");
        if (this.isser != null) {
            s.append(" ");
            s.append(this.isser.getResult());
            s.append(" ");
            s.append("is;");
        }
        if (this.getter != null) {
            s.append(" ");
            s.append(this.getter.getResult());
            s.append(" ");
            s.append("get;");
        }
        if (!this.setters.isEmpty()) {
            for (Method setter : this.setters) {
                s.append(" ");
                s.append("set(");
                s.append(setter.getParameters().get(0).getType());
                s.append(");");
            }
        }
        s.append(" ");
        s.append("}");
        return s.toString();
    }

    public static List<Property> create(Configuration configuration, List<MethodUsage> usages,
                                        Map<String, String> fields, Mapping mapping, Symbols symbols, List<MethodUsage> rest) {

        ArrayList<Property> properties = new ArrayList<Property>();
        HashMap<String, List<MethodUsage>> map = new HashMap<String, List<MethodUsage>>();
        for (MethodUsage usage : usages) {
            String id = identify(usage);
            if (!Text.zero(id)) {
                if (!map.containsKey(id)) {
                    map.put(id, new ArrayList<MethodUsage>());
                }
                map.get(id).add(usage);
            } else {
                rest.add(usage);
            }
        }
        for (String id : map.keySet()) {
            Property property = create(configuration, id, fields.get(id)
                    , map.get(id), mapping, symbols, rest);

            if (property != null) {
                properties.add(property);
            }
        }
        return sort(properties);
    }

    public static Property create(Configuration configuration, String id, String field,
                                  List<MethodUsage> usages, Mapping mapping, Symbols symbols, List<MethodUsage> rest) {
        MethodUsage isser = null;
        MethodUsage getter = null;
        List<MethodUsage> setters = new ArrayList<MethodUsage>();
        for (MethodUsage usage : usages) {
            if (isIsser(usage)) {
                isser = usage;
            } else if (isGetter(usage)) {
                getter = usage;
            } else if (isSetter(usage)) {
                setters.add(usage);
            }
        }
        if (isser != null && getter != null) {
            if (!getter.returnType().isAssignableBy(isser.returnType())) {
                rest.add(getter);
                getter = null;
            }
        }
        ResolvedType type = null;
        if (isser != null) {
            type = isser.returnType();
        } else if (getter != null) {
            type = getter.returnType();
        }
        if (!setters.isEmpty()) {
            if (type != null) {
                for (int i = setters.size() - 1; i >= 0; i--) {
                    if (!type.isAssignableBy(setters.get(i).getParamTypes().get(0))) {
                        rest.add(setters.get(i));
                        setters.remove(i);
                    }
                }
            } else {
                type = contravariant(setters);
                if (type == null) {
                    rest.addAll(setters);
                    setters.clear();
                }
            }
        }
        return (type == null) ? null : create(configuration, id, type, field,
                isser, getter, setters, mapping, symbols);
    }

    public static Property create(Configuration configuration, String id,
                                  ResolvedType type, String field, MethodUsage isser,
                                  MethodUsage getter, List<MethodUsage> setters,
                                  Mapping mapping, Symbols symbols) {
        String box = "";
        String unbox = "";

        if (type.isPrimitive()) {
            ResolvedPrimitiveType primitive = type.asPrimitive();
            box = String.format("(%s)", mapping.resolve(primitive.getBoxTypeQName()));
            unbox = String.format("(%s)", Type.toString(primitive));
        }
        boolean comparable = configuration.isComparable(Type.toString(type));
        return new Property(id, new Type(type, mapping), comparable, field,
                Method.create(configuration, isser, mapping, symbols),
                Method.create(configuration, getter, mapping, symbols),
                Method.create(configuration, setters, mapping, symbols));
    }

    private static ResolvedType contravariant(List<MethodUsage> setters) {
        ArrayList<ResolvedType> types = new ArrayList<ResolvedType>();
        for (MethodUsage setter : setters) {
            types.add(setter.getParamTypes().get(0));
        }
        for (ResolvedType lhs : types) {
            boolean valid = true;
            for (ResolvedType rhs : types) {
                if (!lhs.isAssignableBy(rhs)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return lhs;
            }
        }
        return null;
    }

    private static ResolvedType resolve(ResolvedType getter, List<MethodUsage> setters, List<MethodUsage> rest) {
        if (getter != null) {
            List<ResolvedType> types = new ArrayList<ResolvedType>();
            for (MethodUsage setter : setters) {
                types.add(setter.returnType());
            }
        } else {
            for (int i = setters.size() - 1; i >= 0; i--) {
                if (getter.isAssignableBy(setters.get(i).returnType())) {
                } else {
                    rest.add(setters.get(i));
                    setters.remove(i);
                }
            }
            return getter;
        }
        return null;
    }

    public static Set<String> possible(List<MethodUsage> usages) {
        HashSet<String> properties = new HashSet<String>();
        for (MethodUsage usage : usages) {
            String property = identify(usage);
            if (!Text.zero(property)) {
                properties.add(property);
            }
        }
        return properties;
    }

    private static String identify(MethodUsage usage) {
        String property = "";
        String accessor = usage.getDeclaration().getName();
        if (isIsser(usage)) {
            property = Text.capitalize(accessor);
        } else if (isGetter(usage)) {
            property = accessor.substring("get".length());
        } else if (isSetter(usage)) {
            property = accessor.substring("set".length());
        }
        return property;
    }

    private static ResolvedType type(MethodUsage usage) {
        ResolvedType type = null;
        if (isIsser(usage) || isGetter(usage)) {
            type = usage.returnType();
        } else if (isSetter(usage)) {
            type = usage.getParamTypes().get(0);
        }
        return type;
    }

    private static boolean isAccessor(MethodUsage usage) {
        return isIsser(usage) || isGetter(usage) || isSetter(usage);
    }

    private static boolean isIsser(MethodUsage usage) {
        return isGetter(usage, true);
    }

    private static boolean isGetter(MethodUsage usage) {
        return isGetter(usage, false);
    }

    private static boolean isGetter(MethodUsage usage, boolean isser) {
        ResolvedMethodDeclaration declaration = usage.getDeclaration();
        String name = declaration.getName();
        Pattern pattern = Pattern.compile(isser ? ISSER : GETTER);
        Matcher matcher = pattern.matcher(name);

        if (matcher.matches()) {
            if (declaration.getTypeParameters().isEmpty() &&
                    usage.getParamTypes().isEmpty()) {
                ResolvedType type = usage.returnType();
                if (!type.isVoid()) {
                    if (isser) {
                        // JavaBeans specification, 8.3.2 - not applicable for java.lang.Boolean
                        if (type.isPrimitive()) {
                            ResolvedPrimitiveType primitive = type.asPrimitive();
                            return primitive.isBoolean();
                        }
                        if (type.isReferenceType()) {
                            ResolvedReferenceType reference = type.asReferenceType();
                            return reference.getQualifiedName().equals(Boolean.class.getCanonicalName());
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSetter(MethodUsage usage) {
        ResolvedMethodDeclaration declaration = usage.getDeclaration();
        String name = declaration.getName();
        Pattern pattern = Pattern.compile(SETTER);
        Matcher matcher = pattern.matcher(name);

        if (matcher.matches()) {
            return (declaration.getTypeParameters().isEmpty() &&
                    usage.returnType().isVoid() &&
                    (usage.getParamTypes().size() == 1));
        }
        return false;
    }

    public static List<Property> sort(List<Property> properties) {
        HashMap<String, Property> map = new HashMap<String, Property>();
        for (Property property : properties) {
            map.put(property.getId(), property);
        }
        ArrayList<String> index = new ArrayList<String>(map.keySet());
        Collections.sort(index);
        ArrayList<Property> result = new ArrayList<Property>();
        for (String key : index) {
            result.add(map.get(key));
        }
        return result;
    }
}
