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

import java.nio.charset.Charset;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class Templates {
    private static final Log log = new Log(Templates.class);
    private final Configuration configuration;
    public Templates(Charset encoding, Class cl, String path){
        this.configuration= new Configuration(Configuration.VERSION_2_3_31);
        this.configuration.setClassForTemplateLoading(cl, path);
        this.configuration.setDefaultEncoding(encoding.name());
        this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.configuration.setLogTemplateExceptions(false);
        this.configuration.setWrapUncheckedExceptions(true);
        this.configuration.setFallbackOnNullLoopVariable(false);
    }
    private Template get(String id) throws Exception {
        return this.configuration.getTemplate(id);
    }
    protected String render(String id, Object model) throws Exception{
        StringWriter writer = new StringWriter();
        this.get(id).process(model, writer);
        return writer.toString();
    }
    public String render(Implementation implementation) throws Exception {
        return this.render("implementation.ftl", implementation);
    }
    public String render(Resolver resolver) throws Exception {
        return this.render("resolver.ftl", resolver);
    }
    public String render(Module module) throws Exception {
        return this.render("module.ftl", module);
    }
    public String render(Service service) throws Exception {
        return  this.render("service.ftl", service);
    }
    public String render(Test test) throws Exception {
        return this.render("test.ftl", test);
    }
}
