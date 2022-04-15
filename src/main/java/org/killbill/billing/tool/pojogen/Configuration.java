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
    private final File resource;
    private final File test;
    private final Settings settings;
    private final String builder;
    private final String module;
    private final String prefix;
    private final String resolver; 
    private final String subpackage;
    private final String suffix;
    private final Templates templates;
    private final List<File> dependencies;
    private final List<File> sources;
    private final Set<String> comparables;
    private final Set<String> interfaces;
    private final Set<String> namespaces;

    public Configuration(Charset encoding, Templates templates, Settings settings){
        this.encoding = encoding;
        this.settings = settings;
        this.templates = templates;
        this.sources = new ArrayList<File>(this.settings.getSources());
        this.dependencies = new ArrayList<File>(this.settings.getDependencies());
        this.comparables = new HashSet<String>(this.settings.getComparables());
        this.interfaces = new HashSet<String>(this.settings.getInterfaces());
        this.namespaces = new HashSet<String>(this.settings.getPackages());
        this.test = this.settings.getTest();
        this.resource = this.settings.getResource();
        this.subpackage  = this.settings.getSubpackage();
        this.suffix = this.settings.getSuffix();
        this.prefix= this.settings.getPrefix();
        this.resolver = this.settings.getResolver();
        this.module = this.settings.getModule();
        this.output = this.settings.getOutput() ;
        this.builder = this.settings.getBuilder();
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
    public String getBuilder(){
        return this.builder;
    }
    public List<File> getDependencies() {
        return this.dependencies;
    }
    public Charset getEncoding() {
        return this.encoding;
    }
    public String getModule(){
        return this.module;
    }
    public File getOutput() {
        return this.output;
    }
    public String getResolver(){
        return this.resolver;
    }
    public File getResource() {
        return this.resource;
    }
    public List<File> getSources() {
        return this.sources;
    }
    public  Templates getTemplates() {
        return this.templates;
    }
    public File getTest() {
        return this.test;
    }
}
