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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Configuration {

  private static final Log log = new Log(Configuration.class);

  private final Charset encoding;
  private final File output;
  private final File test;
  private final Settings settings;
  private final String prefix;
  private final String suffix;
  private final String subpackage;
  private final Templates templates;
  private final List<File> dependencies;
  private final List<File> sources;
  private final Set<String> comparables;
  private final Set<String> interfaces;
  private final Set<String> namespaces;

  public Configuration(Charset encoding, Templates templates, Settings settings){
    this.encoding = encoding;
    this.templates = templates;
    this.settings = settings;
    this.dependencies = new ArrayList<File>(this.settings.getDependencies());
    this.sources = new ArrayList<File>(this.settings.getSources());
    this.output = this.settings.getOutput() ;
    this.namespaces = new HashSet<String>(this.settings.getPackages());
    this.interfaces = new HashSet<String>(this.settings.getInterfaces());
    this.comparables = new HashSet<String>(this.settings.getComparables());
    this.subpackage  = this.settings.getSubpackage();
    this.prefix= this.settings.getPrefix();
    this.suffix = this.settings.getSuffix();
    this.test = this.settings.getTest();
  }
  public String rename(String namespace, String name){
    String id = Namespaces.id(namespace,name);
    return  Namespaces.join( rename(namespace) , this.prefix + id + this.suffix );
  }
  public String rename(String namespace){
    return  Namespaces.join(namespace, this.subpackage);
  }
  public boolean accepts(String namespace, String name){
    if(this.namespaces.isEmpty() && this.interfaces.isEmpty()){
      return true;
    }else{
      return this.namespaces.contains(namespace)|| this.interfaces.contains(name);
    }
  }
  public boolean isComparable(String name){
    return this.comparables.contains(name);
  }
  public List<File> getDependencies() {
    return this.dependencies;
  }
  public Charset getEncoding() {
    return this.encoding;
  }
  public List<File> getSources() {
    return this.sources;
  }
  public  Templates getTemplates() {
    return this.templates;
  }
  public File getOutput() {
    return this.output;
  }
  public File getTest() {
    return this.test;
  }
}
