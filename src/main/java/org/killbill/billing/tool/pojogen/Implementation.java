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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedLambdaConstraintType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.resolution.types.ResolvedUnionType;
import com.github.javaparser.resolution.types.ResolvedWildcard;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametersMap;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametrized;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;

public class  Implementation {

  private static final Log log = new Log(Implementation.class);

  private final Mapping mapping;
  private final ResolvedInterfaceDeclaration declaration;
  private final String base;
  private final String name;
  private final String namespace;
  private final Symbols symbols;
  private final List<Method> methods;
  private final List<Property> properties;
  private final List<String> imports;

  private Implementation( ResolvedInterfaceDeclaration declaration, String namespace, String name, String base,
      List<String> imports, List<Property> properties, List<Method> methods, Mapping mapping, Symbols symbols){
    this.declaration = declaration;
    this.namespace = namespace;
    this.name = name;
    this.base = base;
    this.imports = imports;
    this.properties = properties;
    this.methods= methods;
    this.mapping = mapping;
    this.symbols = symbols;
  }
  public String getBase(){
    return this.base;
  }
  public String getBuilder(){
    return Namespaces.join(this.name, "Builder");
  }
  public List<String> getImports(){
    return this.imports;
  }
  public List<Method> getMethods(){
    return this.methods;
  }
  public String getPackage(){
    return this.getNamespace();
  }
  public List<Property> getProperties(){
    return this.properties;
  }
  public String getId(){
    return Namespaces.id(this.namespace, this.name);
  }
  public String getName(){
    return this.name;
  }
  public String getNamespace(){
    return this.namespace;
  }
  public String getUid(){
    long uid = 0;
    try{
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] bytes = md.digest( this.name.getBytes());

      for(int i = 0 ; i < 8 ; i++){
        uid = uid << 8;
        uid = uid | (long)(0x0FF & (bytes[2*i] ^  bytes[2*i + 1]));
      } 
    }catch(Exception e){}

    return "0x" + Long.toHexString(uid).toUpperCase() + "L";
  }
  public String type(String name ){
    return this.mapping.resolve(name);
  }
  public Map<String,String> declare(String... vargs){
    Map<String,String> map = new HashMap<String,String>();
    int attempts = 0;
    while(true){
      map.clear();
      boolean valid = true;
      for(int i = 0 ; i < vargs.length ; i++){
        String name = newVarName(vargs[i], attempts);
        map.put(vargs[i], name);
        if(this.symbols.contains(name)){
          valid = false;
          break;
        }
      }
      if(valid){
        break;
      }
      attempts++;
    }
    /*
       for(int i = 0 ; i < vargs.length ; i++){
       map.put(vargs[i], "varg" + i);
       }
    */
    return map;
  }
  public String newVarName(String name, int attempts){
    if(attempts > 0){
      name = name + attempts;
    }
    return name;
  }
  @Override
  public String toString(){
    StringBuilder s = new StringBuilder();
    int indent = 0;
    Text.append(s, indent,String.format("package %s;", this.namespace));
    Text.append(s);
    for(String reference : this.imports){
      Text.append(s, indent,String.format("import %s;", reference));
    }
    Text.append(s);
    Text.append(s, indent, String.format("class %s implements %s {", 
          Namespaces.id(this.namespace, this.name), this.mapping.resolve(this.base)));
    Text.append(s);
    if(!this.properties.isEmpty()){
      for(Property property : this.properties){
        Text.append(s, indent + 2, String.format("%s;", property));
      }
      Text.append(s);
    }
    if(!this.methods.isEmpty()){
      for(Method method : this.methods){
        Text.append(s, indent + 2, String.format("%s;", method));
      }
      Text.append(s);
    }
    Text.append(s,indent,"}");
    return s.toString();
  }
  public static Implementation create(Configuration configuration, 
    Symbols symbols, String namespace, String name, 
    ResolvedInterfaceDeclaration declaration){
    symbols = new Symbols(symbols);
    List<MethodUsage> usages = Method.unique(traverse(declaration));
    for(MethodUsage usage: usages){
      symbols.add(usage.getName());
    }
    Importer importer  = new Importer(symbols, namespace, name);
    importer.add(namespace, Namespaces.join(name, "Builder"));
    importer.add("java.lang", "java.lang.Boolean");
    importer.add("java.lang", "java.lang.Byte");
    importer.add("java.lang", "java.lang.Character");
    importer.add("java.lang", "java.lang.Float");
    importer.add("java.lang", "java.lang.Integer");
    importer.add("java.lang", "java.lang.Long");
    importer.add("java.lang", "java.lang.Short");
    importer.add("java.lang", "java.lang.Double");
    importer.add("java.lang", "java.lang.Object");
    importer.add("java.lang", "java.lang.String");
    importer.add("java.lang", "java.lang.StringBuffer");
    importer.add("java.lang", "java.lang.Override");
    importer.add("java.lang", "java.lang.SuppressWarnings");
    importer.add("java.io"  , "java.io.Serializable");
    importer.add("java.util", "java.util.Objects");
    importer.add("java.util", "java.util.Arrays");
    importer.add(declaration.getPackageName() , declaration.getQualifiedName());
    ArrayList<ResolvedType> types = new ArrayList<ResolvedType>();

    for(MethodUsage usage : usages){
      gather(usage, types);
    }
    importer.addAll(types);
    List<String> imports  = importer.getImports();
    Mapping mapping  = importer.getMapping();
    symbols =  importer.getSymbols();
    HashMap<String,String> fields = new  HashMap<String,String>();
    for(String id: Property.possible(usages)){
      if(!fields.containsKey(id)){
        int attempts = 0;
        while(true){
          String field = newField(id, attempts++);
          if(!symbols.contains(field)){
            // symbols.add(field);
            fields.put(id, field);
            break;
          }
        }
      }
    }
    ArrayList<MethodUsage> rest = new ArrayList<MethodUsage>();
    List<Property> properties = Property.create(configuration, usages, fields, mapping, symbols, rest);
    ArrayList<Method> methods = new ArrayList<Method>();
    for(MethodUsage usage :rest){
      methods.add(Method.create(configuration, usage, mapping, symbols));
    }
    for(Property property : properties){
      if(property.getIsser()!= null){
        override(property.getIsser(), true);
      }
      if(property.getGetter()!= null){
        override(property.getGetter(),true);
      }
      for(Method setter : property.getSetters()){
        override(setter,true);
      }
    }
    for(Method method: methods){
      override(method, false);
    }
    return new Implementation(declaration, namespace, name, declaration.getQualifiedName(), 
        imports, properties, methods, mapping, symbols);
  }
  private static void override(Method method, boolean finalize){
    method.addAnnotation("java.lang.Override");
    method.addModifier(Modifier.PUBLIC);
    method.getExceptions().clear();
    for(Method.Param parameter: method.getParameters()){
      if(finalize){
        parameter.addModifier(Modifier.FINAL);
      }
    }
  }
  private static List<MethodUsage> traverse(ResolvedInterfaceDeclaration declaration){
    ArrayList<MethodUsage> usages = new ArrayList<MethodUsage>();
    Stack<ResolvedReferenceType> generations = new Stack<ResolvedReferenceType>();
    for(ResolvedMethodDeclaration method: declaration.getDeclaredMethods()){
      if(!method.isDefaultMethod() && !method.isStatic()){
        usages.add(new MethodUsage(method));
      }
    }
    for( ResolvedReferenceType ancestor: declaration.getAncestors()){
      traverse(usages, push(generations, ancestor));
    }
    return usages;
  }
  private static void traverse(List<MethodUsage> usages, Stack<ResolvedReferenceType>  generations){
    ResolvedReferenceType  type = generations.peek();
    Optional<ResolvedReferenceTypeDeclaration> declaration = type.getTypeDeclaration();
    if(declaration.isPresent()){
      if(declaration.get().isInterface()){
        for(MethodUsage method: type.getDeclaredMethods()){
          if(!method.getDeclaration().isDefaultMethod() && !method.getDeclaration().isStatic()){
            usages.add(Type.specialize(method, type));
          }
        }
        for( ResolvedReferenceType ancestor: declaration.get().getAncestors()){
          traverse(usages, push(generations, Type.specialize(ancestor, type)));
        }
      }
    }
  }
  private static void gather(MethodUsage method, List<ResolvedType> types){
    ResolvedMethodDeclaration declaration = method.getDeclaration();
    if(declaration.isGeneric()){
      List<ResolvedTypeParameterDeclaration> declarations = declaration.getTypeParameters();
      for(int i = 0 ; i < declarations.size() ; i++){
        gather(declarations.get(i),types);
      }
    }
    gather( method.returnType(), types);
    List<ResolvedType> parameters =	method.getParamTypes();
    for(int i = 0 ; i< parameters.size(); i++){
      gather(parameters.get(i), types);
    }
    List<ResolvedType> exceptions = method.exceptionTypes();
    if(!exceptions.isEmpty()){
      for(int i = 0; i < exceptions.size() ; i++){
        gather(exceptions.get(i), types);
      }
    }
  }
  private static void gather(ResolvedTypeParameterDeclaration parameter, List<ResolvedType> types){
    List<ResolvedTypeParameterDeclaration.Bound> bounds = parameter.getBounds();
    if(!bounds.isEmpty()){
      for(int i = 0 ; i < bounds.size() ; i++){
        gather(bounds.get(i).getType(), types); 
      }
    }
  }
  private static void gather(ResolvedType type, List<ResolvedType> types){
    types.add(type);
    if(type.isReferenceType()){
      ResolvedReferenceType reference = type.asReferenceType();
      List<Pair<ResolvedTypeParameterDeclaration,ResolvedType>>	parameters = reference.getTypeParametersMap();
      if(!parameters.isEmpty()){
        for( int i = 0; i < parameters.size() ; i++ ){
          gather(parameters.get(i).b, types);
        }
      }
    }else if( type.isTypeVariable() ){
      ResolvedTypeVariable variable = type.asTypeVariable();
      ResolvedTypeParameterDeclaration parameter = variable.asTypeParameter();
      // gather(parameter, types);
    }
    else if(type.isWildcard()){
      ResolvedWildcard wildcard = type.asWildcard();
      if(wildcard.isBounded()){
        gather(wildcard.getBoundedType(), types); 
      }
    }else if(type.isArray()){
      ResolvedArrayType array = type.asArrayType();
      gather(array.getComponentType(), types); 
    } else if(type.isConstraint()){
      ResolvedLambdaConstraintType constraint = type.asConstraintType();
      gather(constraint.getBound(), types);
    }
  }
  private static Stack<ResolvedReferenceType> push(Stack<ResolvedReferenceType> source,  ResolvedReferenceType type){
    Stack<ResolvedReferenceType>  stack = new Stack<ResolvedReferenceType>();
    for( ResolvedReferenceType item : source){
      stack.add(item);
    }
    stack.add(type);
    return stack;
  }
  public static String newField(String name, int attempts ){
    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
    if(attempts == 0){
      return name;
    }
    return name + attempts;
  }
  private static void walk(StringBuilder s ,int generations, ResolvedReferenceType type, Mapping mapping, boolean debug){
    Optional<ResolvedReferenceTypeDeclaration> declaration = type.getTypeDeclaration();
    if(declaration.isPresent()){
      if(declaration.get().isInterface()){
        Text.append(s, generations, " >> interface " + Type.unparse(type, mapping, debug)) ;
        for(MethodUsage method: type.getDeclaredMethods()){
          Text.append(s, generations,  "   > " + Method.unparse(Type.specialize(method, type), mapping, debug));
        }
        for( ResolvedReferenceType ancestor: type.getDirectAncestors()){
          walk(s, generations + 1, Type.specialize(ancestor, type), mapping, debug);
        }
      }
    }
  }
  public static String walk(ResolvedInterfaceDeclaration declaration, boolean debug){
    StringBuilder s = new StringBuilder();
    int generations = 0;
    Mapping mapping = new Mapping();
    Text.append(s, generations , " >> interface " + mapping.resolve(declaration.getQualifiedName()) ) ;
    for(ResolvedMethodDeclaration method: declaration.getDeclaredMethods()){
      Text.append(s, generations , "   > " + Method.unparse(new MethodUsage(method), mapping, debug));
    }
    for(ResolvedReferenceType ancestor: declaration.getAncestors()){
      walk(s, generations + 1, ancestor, mapping, debug);
    }
    return s.toString();
  }
}
