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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;

public class Method {
    private static final Log log = new Log(Method.class);

    public static class Param { 
        private final boolean variadic;
        private final Mapping mapping;
        private final Type type;
        private final List<Annotation> annotations;
        private final List<Modifier> modifiers;
        private final String name;

        public Param(List<Modifier> modifiers, List<Annotation> annotations, 
                Type type, String name , boolean variadic, Mapping mapping){
            this.annotations = annotations;
            this.modifiers = modifiers;
            this.type = type;
            this.name = name;
            this.variadic = variadic;
            this.mapping = new Mapping(mapping);
        }
        public boolean isVariadic(){
            return this.variadic;
        }
        public List<Annotation> getAnnotations(){
            return this.annotations;
        }
        public List<Modifier> getModifiers(){
            return this.modifiers;
        }
        public String getName(){
            return this.name;
        }
        public Type getType(){
            return this.type;
        }
        public void addAnnotation(String name){
            this.addAnnotation(name, null);
        }
        public void addAnnotation(String name, String content) {
            this.annotations.add(new Annotation(name, content, this.mapping));
        }
        public void addModifier(Modifier modifier){
            this.modifiers.add(modifier);
        }
        @Override
        public String toString(){
            ArrayList<String>  strings = new ArrayList<String>();
            strings.addAll(Text.toStrings(this.annotations));
            strings.addAll(Text.toStrings(this.modifiers));
            strings.add(this.type + (this.variadic ?  " ..." : ""));
            strings.add(this.name);
            return String.join(" ", strings);
        }
    }
    private static final String ERASURE = "java.lang.Object";

    private final Mapping mapping;
    private final MethodUsage usage;
    private final Type result;
    private final String name;
    private final List<Annotation> annotations; 
    private final List<Modifier> modifiers; 
    private final List<Method.Param> parameters;
    private final List<Type> exceptions; 
    private final List<Type.Param> typeParameters;
    private Method(MethodUsage usage, List<Annotation> annotations, List<Modifier> modifiers, List<Type.Param> typeParameters,
            Type result, String name, List<Method.Param> parameters, List<Type> exceptions, Mapping mapping){
        this.usage = usage;
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.result= result;
        this.name = name;
        this.parameters = parameters;
        this.exceptions = exceptions;
        this.mapping = new Mapping(mapping);
    }
    public List<Annotation> getAnnotations(){
        return this.annotations;
    }
    public List<Type> getExceptions(){
        return this.exceptions;
    }
    public List<Modifier> getModifiers(){
        return this.modifiers;
    }
    public String getName(){
        return this.name;
    }
    public Type getResult(){
        return this.result;
    }
    public String getSignature(){
        return getSignature(this.usage);
    }
    public List<Method.Param> getParameters(){
        return this.parameters;
    }
    public List<Type.Param> getTypeParameters(){
        return this.typeParameters;
    }
    public void addAnnotation(String name){
        this.addAnnotation(name, null);
    }
    public void addAnnotation(String name, String content){
        this.annotations.add(new Annotation(name, content ,this.mapping));
    }
    public void addModifier(Modifier modifier){
        this.modifiers.add(modifier);
    }
    @Override
    public String toString(){
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Text.toStrings(this.annotations));
        list.addAll(Text.toStrings(this.modifiers));
        if(!this.typeParameters.isEmpty()){
            list.add(String.format( "<%s>", String.join(", ",Text.toStrings(this.typeParameters))));
        }
        list.add(this.result.toString());
        list.add(String.format("%s(%s)", this.name,
                    String.join(", ", Text.toStrings(this.parameters))
                    ));
        if(!this.exceptions.isEmpty()){
            list.add("throws");
            list.add(String.join(", ", Text.toStrings(this.exceptions)));
        }
        return String.join(" ", list);
    }
    public static Method create(Configuration configuration, MethodUsage usage, Mapping mapping, Symbols symbols){
        if(usage != null)
        {
            mapping = new Mapping(mapping);
            symbols = new Symbols(symbols);
            ResolvedMethodDeclaration declaration = usage.getDeclaration();
            if(declaration.isGeneric()){
                if(!declaration.getTypeParameters().isEmpty()){
                    gather(declaration.getTypeParameters(), mapping, symbols);
                }
            }
            ArrayList<Annotation> annotations = new ArrayList<Annotation>();
            ArrayList<Modifier> modifiers  = new ArrayList<Modifier>();
            List<Type.Param> typeParameters = map(declaration.getTypeParameters(), mapping, symbols);
            Type result =  new Type(usage.returnType(), mapping);
            String name = declaration.getName();
            List<Method.Param> parameters = map(usage, mapping, symbols);
            List<Type> exceptions =  map(usage.exceptionTypes(), mapping);
            return new Method(usage, annotations, modifiers, typeParameters, result, name, parameters, exceptions, mapping);
        }
        return null;
    }
    public static List<Method> create(Configuration configuration, 
            List<MethodUsage> usages, Mapping mapping, Symbols symbols){
        ArrayList<Method> methods = new ArrayList<Method>();
        for(MethodUsage usage : usages){
            Method method = create(configuration, usage, mapping, symbols);
            if(method != null){
                methods.add(method);
            }
        }
        return methods;
    }
    private static List<Type> map(List<ResolvedType> types, Mapping mapping){
        ArrayList<Type> list = new ArrayList<Type>();
        for(ResolvedType type: types){
            list.add(new Type(type, mapping));
        }
        return list;
    }
    private static List<Type.Param> map(List<ResolvedTypeParameterDeclaration>  declarations, 
            Mapping mapping,  Symbols globals){
        ArrayList<Type.Param> list = new ArrayList<Type.Param>();
        for(int i = 0 ; i < declarations.size() ; i++){
            list.add(new Type.Param(declarations.get(i), mapping));
        }
        return list;
    }
    private static List<Method.Param> map(MethodUsage usage, Mapping mapping, Symbols symbols){
        ResolvedMethodDeclaration declaration = usage.getDeclaration();
        Optional<MethodDeclaration> ast = declaration.toAst();
        List<ResolvedType> types = usage.getParamTypes();

        boolean rename = true;
        if(ast.isPresent() ){
            NodeList<Parameter> inputs = ast.get().getParameters();
            if(inputs.size() == types.size()){
                for(Parameter input: inputs){
                    if(symbols.contains(input.getName().asString() )){
                        break;
                    }
                    rename = false;
                }
            }
        }
        List<String> names = newParamNames(symbols, types.size());
        ArrayList<Method.Param> outputs = new ArrayList<Method.Param>();
        boolean variadic =  declaration.hasVariadicParameter();

        for(int i = 0; i < types.size() ; i++){
            boolean last = (i == (types.size() -1));
            ArrayList<Modifier> modifiers = new ArrayList<Modifier>();
            ArrayList<Annotation> annotations = new ArrayList<Annotation>();
            String name = "";
            Type type;
            if(rename){
                name = names.get(i); 
            }else{
                name = ast.get().getParameters().get(i).getName().asString();
            }
            if( variadic && last &&  types.get(i).isArray()){
                ResolvedArrayType array = types.get(i).asArrayType();
                type = new Type(array.getComponentType(), mapping);
            }else{
                type = new Type( types.get(i), mapping);
            }
            outputs.add(new Method.Param(modifiers, annotations, type, name , variadic && last, mapping));
        }
        return outputs;
    }
    private static void gather(List<ResolvedTypeParameterDeclaration> types, Mapping mapping, Symbols symbols){
        boolean rename  = false;
        for(ResolvedTypeParameterDeclaration  type: types){
            String name =type.getName();
            if (symbols.contains(name) ){
                rename = true;
                break;
            }
        }
        if(rename){
            List<String> names = newTypeNames(symbols, types.size());
            for(int i = 0 ; i < types.size() ; i++){
                mapping.put(types.get(i).getQualifiedName(), names.get(i));
                symbols.add(names.get(i));
            }

        }else {
            for(int i = 0 ; i < types.size() ; i++){
                mapping.put(types.get(i).getQualifiedName(),types.get(i).getName());
                symbols.add(types.get(i).getName());
            }
        }
    }
    public static List<MethodUsage> unique(List<MethodUsage> usages){
        HashMap<String, ArrayList<MethodUsage>> signatures =  new HashMap<String, ArrayList<MethodUsage>>(); 

        for(MethodUsage usage: usages){
            String key  = getSignature(usage);
            if(!signatures.containsKey(key)){
                signatures.put(key, new ArrayList<MethodUsage>());
            }
            signatures.get(key).add(usage);
        }
        ArrayList<MethodUsage> methods =  new ArrayList<MethodUsage>();
        for(String key : signatures.keySet()){
            MethodUsage usage = covariant(signatures.get(key));
            if( usage!=null){
                methods.add(usage);
            }
        }
        return methods;
    }
    private static MethodUsage covariant(List<MethodUsage> usages){
        if(usages.size() > 0){
            ArrayList<ResolvedType> types = new ArrayList<ResolvedType>();
            for(int i = 0 ; i < usages.size() ; i++){
                types.add(usages.get(i).returnType());
            }
            for(int i = 0 ; i < types.size() ; i++){
                boolean valid = true;
                for(int j = 0 ; j < types.size() ; j++){
                    if(!types.get(j).isAssignableBy(types.get(i))){
                        valid =  false;
                        break;
                    }
                }
                if(valid) return usages.get(i);
            }
            return usages.get(0);
        }
        return null;
    }
    public static String toString(MethodUsage method){
        return toString(method, new Mapping());
    }
    public static String toString(MethodUsage method, Mapping mapping){
        return unparse(method, mapping, false);
    }
    public static String unparse(MethodUsage method, Mapping mapping, boolean debug){
        StringBuilder s = new StringBuilder();
        ResolvedMethodDeclaration declaration = method.getDeclaration();
        Optional<MethodDeclaration> ast = declaration.toAst();

        if(declaration.isGeneric()){
            List<ResolvedTypeParameterDeclaration>  declarations = declaration.getTypeParameters();
            if(!declarations.isEmpty() ){
                s.append("<");
                for(int i = 0 ; i < declarations.size() ; i++){
                    if(i > 0) s.append(", ");
                    s.append(Type.Param.unparse(declarations.get(i), mapping, debug));
                }
                s.append(">");
                s.append(" ");
            }
        }
        s.append(Type.unparse( method.returnType(), mapping, debug));
        s.append(" ");
        s.append(method.getName());
        s.append("(");

        List<ResolvedType> parameters =	method.getParamTypes();
        for(int i = 0 ; i< parameters.size(); i++){
            if(i > 0) s.append(", ");

            if(method.getDeclaration().hasVariadicParameter() 
                    && parameters.get(i).isArray() && (i == (parameters.size() - 1))){
                ResolvedArrayType array = parameters.get(i).asArrayType();
                s.append(Type.unparse(array.getComponentType(), mapping, debug));
                s.append("...");
            }else{
                s.append(Type.unparse(parameters.get(i), mapping, debug));
            }

            if(debug){
                s.append(" ");
                if(ast.isPresent()){
                    NodeList<Parameter> nodes = ast.get().getParameters();
                    s.append(nodes.get(i).getName().asString());
                }else{
                    s.append(newParamName(i, parameters.size(), 0));
                }
            }
        }
        s.append(")");
        List<ResolvedType> exceptions = method.exceptionTypes();
        if(!exceptions.isEmpty()){
            for(int i = 0; i < exceptions.size() ; i++){
                if(i > 0) s.append(", ");
                else  s.append(" throws ");
                s.append(Type.unparse(exceptions.get(i), mapping, debug));
            }
        }
        return s.toString();
    }

    public static String id(String first , String rest, int position, int parameters, int attempts ){
        StringBuilder name = new StringBuilder();
        int j = position;
        while ( j >=first.length()){
            int k = j % rest.length();
            j = j / rest.length();
            name.append(rest.charAt(k)); 
        }
        name = name.append( first.charAt(j) );
        name = name.reverse();
        if(attempts > 0){
            name.append(attempts);
        }
        return name.toString();
    }
    public static List<String> newTypeNames(Set<String> symbols, int count){
        ArrayList<String> names = new ArrayList<String>();
        int attempts = 0;
        while(true){
            names.clear();
            boolean valid = true;
            for(int i = 0 ; i < count ; i++){
                String name = newTypeName(i, count, attempts);
                if (symbols.contains(name) ){
                    valid = false;
                    break;
                }
                names.add(name);
            }
            if(valid){
                break;
            }
            attempts++;
        }
        return names;
    }
    public static List<String> newParamNames(Set<String> symbols, int count){
        ArrayList<String> names = new ArrayList<String>();
        int attempts = 0;
        while(true){
            names.clear();
            boolean valid = true;
            for(int i = 0 ; i < count ; i++){
                String name = newParamName(i, count, attempts);
                if (symbols.contains(name) ){
                    valid = false;
                    break;
                }
                names.add(name);
            }
            if(valid){
                break;
            }
            attempts++;
        }
        return names;
    }
    public static String newTypeName(int position, int parameters, int attempts ){
        String first = "TUVWXYZ";
        String rest= "ABCDEFGHIJKLMNOPQRS";
        return id(first, rest, position, parameters, attempts);
    }
    public static String newParamName(int position, int parameters, int attempts ){
        String first = "abcdefghijklmnopqrstuvwxyz";
        String rest = "abcdefghijklmnopqrstuvwxyz";
        return id(first, rest , position, parameters, attempts);
    }

    public static String getSignature(MethodUsage method){
        StringBuilder s = new StringBuilder();
        Mapping mapping = new Mapping();
        ResolvedMethodDeclaration declaration = method.getDeclaration();

        if(declaration.isGeneric()){
            List<ResolvedTypeParameterDeclaration>  declarations = declaration.getTypeParameters();
            for(int i = 0 ; i < declarations.size() ; i++){
                mapping.put(declarations.get(i).getQualifiedName(), ERASURE);
            }
        }
        s.append(method.getName());
        s.append("(");
        List<ResolvedType> parameters =	method.getParamTypes();
        for(int i = 0 ; i< parameters.size(); i++){
            if(i > 0) s.append(", ");
            s.append(Type.unparse(parameters.get(i), mapping, false));
        }
        s.append(")");
        return s.toString();
    }
}
