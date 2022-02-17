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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

public class Importer{
  private static final Log log = new Log(Importer.class);

  private final Symbols symbols;
  private final String name;
  private final String namespace;
  private final Map<String, Map<String, Set<String>>> partitions; 
  private final Set<String> namespaces; 
  public Importer(Symbols symbols, String namespace, String name){
    this.partitions= new HashMap< String, Map<String, Set<String>>>();
    this.namespaces = new HashSet<String>();
    this.namespace = namespace;
    this.name = name;
    this.symbols = symbols;
    this.namespaces.add("java.lang");
    this.namespaces.add(this.namespace);
    this.add(this.namespace, this.name);
  }
  public void add(String namespace, String name){
    if(Namespaces.belongs(namespace, name)){
      put(this.partitions, namespace, name) ;
    }
  }
  public void add(ResolvedType type){
    if(type.isReferenceType()){
      ResolvedReferenceType reference =  type.asReferenceType();
      Optional<ResolvedReferenceTypeDeclaration> optional = reference.getTypeDeclaration();
      if(optional.isPresent()){
        if(optional.get().hasName()){
          this.add(optional.get().getPackageName(), optional.get().getQualifiedName());
        }
      }
    }
  }
  public void addAll(List<ResolvedType> types){
    for(ResolvedType type : types){
      this.add(type);
    }
  }
  public List<String> getImports(){
    HashSet<String> imports = new HashSet<String>();
    HashMap<String,String> mapping = new HashMap<String,String>();
    resolve( this.partitions, this.namespaces, this.symbols,imports, mapping);
    List<String> list = new ArrayList<String>(imports);
    Collections.sort(list);
    return list;
  }
  public Symbols getSymbols(){
    HashSet<String> imports = new HashSet<String>();
    HashMap<String,String> mapping = new HashMap<String,String>();
    resolve( this.partitions, this.namespaces, this.symbols,imports, mapping);
    return Symbols.union(this.symbols, new Symbols(mapping.values()));
  }
  public Mapping getMapping(){
    HashSet<String> imports = new HashSet<String>();
    HashMap<String,String> mapping = new HashMap<String,String>();
    resolve( this.partitions, this.namespaces, this.symbols,imports, mapping);
    return new Mapping(mapping);
  }
  @Override
  public String toString(){
    HashSet<String> imports = new HashSet<String>();
    HashMap<String,String> mapping = new HashMap<String,String>();
    resolve( this.partitions, this.namespaces, this.symbols,imports, mapping);
    return toString(imports, mapping, Symbols.union(this.symbols, new Symbols(mapping.values())));
  }
  private static void resolve(
      Map<String, Map<String, Set<String>>> partitions, 
      Set<String> namespaces, Set<String> symbols,  
      Set<String> imports, Map<String, String> map){
    for(String key: partitions.keySet()){
      for(String path: partitions.get(key).keySet()){
        String namespace = Namespaces.namespace(path, key);
        for(String name : partitions.get(key).get(path)){
          if((partitions.get(key).size() > 1) || symbols.contains(key) ){
            map.put(name, name);
          }else{
            if(!namespaces.contains(namespace)){
              imports.add(path);
            }
            map.put(name, Namespaces.id(namespace, name)); 
          }
        }
      }
    }
  }
  private static void put(Map<String, Map<String, Set<String>>> partitions,  String namespace , String name){
    String key  =Namespaces.key(namespace, name);
    String path = Namespaces.name(namespace, key);
    if(!partitions.containsKey(key)){
      partitions.put(key, new HashMap<String,Set<String>>());
    }
    if(!partitions.get(key).containsKey(path)){
      partitions.get(key).put(path,  new HashSet<String>());
    }
    partitions.get(key).get(path).add(name);
  }
  public static  String toString(HashMap<String, HashMap<String, HashSet<String>>> partitions){
    StringBuilder s = new StringBuilder();
    for( String key : partitions.keySet()){
      s.append(" > " + key) ;
      s.append("\n");
      for( String  path : partitions.get(key).keySet()){
        s.append("  > " + path ) ;
        s.append("\n");
        for(String  name: partitions.get(key).get(path)){
          s.append("   > " + name ) ;
          s.append("\n");
        }
      }
    }
    return s.toString();
  }
  public static  String toString(Set<String> imports, Map<String, String> mapping, Set<String> symbols){
    StringBuilder s = new StringBuilder();
    for(String namespace : imports){
      s.append("import " + namespace + ";\n");
    }
    for(String name: mapping.keySet()){
      s.append(name + " -> " + mapping.get(name)+ "\n");
    }
    for(String symbol : symbols){
      s.append(symbol + "\n");
    }
    return s.toString();
  }
}
