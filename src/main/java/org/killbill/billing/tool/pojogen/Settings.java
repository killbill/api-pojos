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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JacksonXmlRootElement(localName = "Settings")
public class Settings{

    private static final Log log = new Log(Settings.class);

    private File output; 
    private File test; 
    private File resource; 
    private String builder;
    private String module;
    private String prefix;
    private String suffix;
    private String resolver;
    private String subpackage;
    private String service;
    private List<File> dependencies;
    private List<File> sources; 
    private List<String> comparables ;
    private List<String> interfaces;
    private List<String> packages; 

    public Settings(){
        this.sources = new ArrayList<File>();
        this.dependencies = new ArrayList<File>();
        this.comparables = new ArrayList<String>();
        this.packages= new ArrayList<String>();
        this.interfaces= new ArrayList<String>();
        this.output= new File(System.getProperty("user.dir"));
        this.subpackage = "";
        this.prefix = "";
        this.suffix = "";
        this.test = null;
        this.resource = null;
        this.builder = "Builder";
        this.module = null;
        this.resolver = null;
    }
    @JacksonXmlElementWrapper(localName = "acceptedInterfaces")
    @JacksonXmlProperty(localName = "interface")
    public List<String> getInterfaces(){
        return this.interfaces;
    }
    @JacksonXmlElementWrapper(localName = "acceptedInterfaces")
    @JacksonXmlProperty(localName = "interface")
    public void  setInterfaces(List<String> interfaces){
        this.interfaces = interfaces;
    }
    @JacksonXmlElementWrapper(localName = "acceptedPackages")
    @JacksonXmlProperty(localName = "package")
    public List<String> getPackages(){
        return this.packages;
    }
    @JacksonXmlElementWrapper(localName = "acceptedPackages")
    @JacksonXmlProperty(localName = "package")
    public void  setPackages(List<String> packages){
        this.packages = packages;
    }
    @JacksonXmlElementWrapper(localName = "comparableTypes")
    @JacksonXmlProperty(localName = "type")
    public List<String> getComparables(){
        return this.comparables;
    }
    @JacksonXmlElementWrapper(localName = "comparableTypes")
    @JacksonXmlProperty(localName = "type")
    public void  setComparables(List<String> comparables){
        this.comparables = comparables;
    }
    @JacksonXmlElementWrapper(localName = "dependencyDirectories")
    @JacksonXmlProperty(localName = "dependencyDirectory")
    public List<File> getDependencies() {
        return this.dependencies;
    }
    @JacksonXmlElementWrapper(localName = "dependencyDirectories")
    @JacksonXmlProperty(localName = "dependencyDirectory")
    public void setDependencyDirectories(List<File> dependencies) {
        this.dependencies = dependencies;
    }
    @JacksonXmlElementWrapper(localName = "sourceDirectories")
    @JacksonXmlProperty(localName = "sourceDirectory")
    public List<File> getSources() {
        return this.sources;
    }
    @JacksonXmlElementWrapper(localName = "sourceDirectories")
    @JacksonXmlProperty(localName = "sourceDirectory")
    public void setSources(List<File> sources) {
        this.sources = sources;
    }
    @JacksonXmlProperty(localName = "testDirectory")
    public File getTest(){
        return this.test;
    }
    @JacksonXmlProperty(localName = "testDirectory")
    public void setTest(File test){
        this.test =  test;
    }
    @JacksonXmlProperty(localName = "resourceDirectory")
    public File getResource(){
        return this.resource;
    }
    @JacksonXmlProperty(localName = "resourceDirectory")
    public void setResource(File resource){
        this.resource = resource;
    }
    @JacksonXmlProperty(localName = "outputClassPrefix")
    public String getPrefix(){
        return this.prefix;
    }
    @JacksonXmlProperty(localName = "outputClassPrefix")
    public void setPrefix(String prefix){
        this.prefix = prefix;
    }
    @JacksonXmlProperty(localName = "outputClassSuffix")
    public String getSuffix(){
        return this.suffix;
    }
    @JacksonXmlProperty(localName = "outputClassSuffix")
    public void setSuffix(String suffix){
        this.suffix = suffix;
    }
    @JacksonXmlProperty(localName = "outputDirectory")
    public File getOutput(){
        return this.output;
    }
    @JacksonXmlProperty(localName = "outputDirectory")
    public void setOutput(File output){
        this.output = output;
    }
    @JacksonXmlProperty(localName = "outputSubpackage")
    public String getSubpackage(){
        return this.subpackage;
    }
    @JacksonXmlProperty(localName = "outputSubpackage")
    public void setSubpackage(String subpackage){
        this.subpackage = subpackage;
    }
    @JacksonXmlProperty(localName = "builderClass")
    public void setBuilder(String builder){
        this.builder = builder;
    }
    @JacksonXmlProperty(localName = "builderClass")
    public String getBuilder(){
        return this.builder;
    }
    @JacksonXmlProperty(localName = "resolverClass")
    public void setResolver(String resolver){
        this.resolver = resolver;
    }
    @JacksonXmlProperty(localName = "resolverClass")
    public String getResolver(){
        return this.resolver;
    }
    @JacksonXmlProperty(localName = "moduleClass")
    public void setModule(String module){
        this.module = module;
    }
    @JacksonXmlProperty(localName = "moduleClass")
    public String getModule(){
        return this.module;
    }
    public void write(File file) throws Exception
    {
        write(file, this);
    }
    public static void write(File file, Settings settings) throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        String xml = mapper.writeValueAsString(settings);
        xml = format(xml);
        FileUtils.writeStringToFile(file, xml, StandardCharsets.UTF_8);
    }
    public static Settings read(File file) throws Exception
    {
        XmlMapper mapper = new XmlMapper();
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return mapper.readValue( s,Settings.class);
    }
    public static  String format(String xml ) throws Exception
    {
        StringWriter writer= new StringWriter();
        StreamSource source = new StreamSource(new StringReader(xml));
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
        transformer.transform(source, result);
        return writer.toString().trim();
    }
    public String toString(){
        String result = "<Settings/>";
        try{
            XmlMapper mapper = new XmlMapper();
            String xml = mapper.writeValueAsString(this);
            result = format(xml);
        }catch(Exception e){
        }
        return result;
    }
}
