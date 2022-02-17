
<#---------------------------------------------------------------------------->
<#macro join items=[] separator=" " prefix="" suffix="" >
    <#if (items?size gt 0) >
        ${ prefix + items?join(separator) + suffix }<#t>
    </#if>
</#macro>
<#---------------------------------------------------------------------------->
<#macro block shift=0 >
    <#local capture>
        <#nested>
    </#local>
    <#--
    <#local lines = capture?remove_beginning("\n")?remove_ending("\n")?split("\n") >
    -->
    <#local lines = capture?split("\n") >
    <#list lines as line >
        <#if ( line?index lt (lines?size - 1) ) >
            <#if shift lt 0 >
                <#if (line?length gt -shift) >
                    ${ line[-shift..] }<#t>
                <#else>
                    ${""}<#t>
                </#if>
            <#else>
                <#if line?trim?length gt 0 >
                  ${ line?left_pad( line?length + shift ) }<#t>
                <#else>
                    ${""}<#t>
                </#if>
            </#if>
            ${"\n"}<#t>
        </#if>
    </#list>
</#macro>
<#---------------------------------------------------------------------------->
<#macro span shift=0 >
    <#local capture>
        <#nested>
    </#local>
    <#local lines = capture?split("\n") >
    <#list lines as line >
        <#if ( line?index lt (lines?size - 1) ) >
            ${line}<#t>
        </#if>
    </#list>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderParam param name>
    <@join items=param.annotations separator=" " prefix="" suffix=" " />
   <@join items=param.modifiers separator=" " prefix="" suffix=" " />
      ${param.type}<#if param.variadic > ...</#if> ${name}<#t>
  </#macro>
<#---------------------------------------------------------------------------->
<#macro renderParams params>
    <#list params as param >
        <@renderParam param param.name />
        <#if param?index lt (params?size - 1) >
            ${", "}<#t>
        </#if>
    </#list>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderMethodHeader method>
    <@join items=method.annotations separator="\n" prefix="" suffix="\n" />
    <@join items=method.modifiers separator=" " prefix="" suffix=" "/>
    <@join items=method.typeParameters separator=", " prefix="<" suffix="> " />
    ${method.result} <#t>
    ${method.name}(<@renderParams method.parameters/>) <#t>
    <@join items=method.exceptions separator=", " prefix="throws " suffix=" " /><#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderMethod method>
    <@block 4>
        <@renderMethodHeader method />
        <@block -12>
            {
                throw new UnsupportedOperationException("${method.signature} must be implemented.");
            }
        </@block>
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderSetterHeader method field>
    <@join items=method.annotations separator="\n" prefix="" suffix="\n" />
    <@join items=method.modifiers separator=" " prefix="" suffix=" "/>
    <@join items=method.typeParameters separator=", " prefix="<" suffix="> " />
    ${method.result} <#t>
    ${method.name}(<@renderParam method.parameters[0] field/>) <#t>
    <@join items=method.exceptions separator=", " prefix="throws " suffix=" " /><#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderSetter setter field>
    <@block 4>
        <@renderSetterHeader setter field/>
        <@block -12>
            {
                this.${field} = ${field};
            }
        </@block>
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderGetter getter field >
    <@block 4>
        <@renderMethodHeader getter />
        <@block -12>
            {
              return this.${field};
            }
        </@block>
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderConstructor >
    <@block -4>
    <#local var = declare("T", "builder", "other") />
        public ${id}(final ${id} ${var["other"]}) { 
            <#list properties as property>
            this.${property.field} = ${var["other"]}.${property.field};
            </#list>
        }
        public <${var["T"]} extends ${type(builder)}<${var["T"]}>> ${id}(${type(builder)}<?> ${var["builder"]}) {
            <#list properties as property>
            this.${property.field} = ${var["builder"]}.${property.field};
            </#list>
        }
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderToString n=4 >
    <#local var = declare("sb") />
    <@block ( n-8 )>
        @Override
        public String toString() {
            final StringBuffer ${var["sb"]} = new StringBuffer("${id}{");
            <#list properties as property>
                <#if property?index gt 0>
                    <@block -12 >
                        ${var["sb"]}.append(", ");
                    </@block>
                </#if>
                <@block -8 >
                    ${var["sb"]}.append("${property.name}=").append(this.${property.field});
                </@block>
            </#list>
            ${var["sb"]}.append("}");
            return ${var["sb"]}.toString();
        }
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderEquals n=4 >
    <#local var = declare("o", "that") />
    <@block (n-8)>
        @Override
        public boolean equals(final ${type("java.lang.Object")} ${var["o"]}) {
            if ( this == ${var["o"]} ) {
                return true;
            }
            if ( ( ${var["o"]} == null ) || ( this.getClass() != ${var["o"]}.getClass() ) ) {
                return false;
            }
            final ${type(name)} ${var["that"]} = (${type(name)}) ${var["o"]};

            <#list properties as property>
                  <#if property.type.primitive>
                      <@block -12>
                          if( this.${property.field} != ${var["that"]}.${property.field} ) {
                              return false;       
                          }
                      </@block>
                  <#elseif property.type.array>
                      <@block -12>
                          if( !${type("java.util.Arrays")}.deepEquals( this.${property.field}, ${var["that"]}.${property.field} ) ) {
                              return false;
                          }
                      </@block>
                  <#elseif property.comparable>
                      <@block -12>
                          if( this.${property.field} != ${var["that"]}.${property.field} )
                              if( ( this.${property.field} == null ) || ( ${var["that"]}.${property.field} == null ) ||
                                  ( 0 != this.${property.field}.compareTo( ${var["that"]}.${property.field} ) ) ) 
                                  return false;
                      </@block>
                  <#else>
                      <@block -12>
                          if( !${type("java.util.Objects")}.equals( this.${property.field}, ${var["that"]}.${property.field} ) ) {
                              return false;
                          }
                      </@block>
                  </#if>
            </#list>
            return true;
        }
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderHashCode >
    <#local var = declare("result")>
    <@block -4>
        @Override
        public int hashCode() {
            int ${var["result"]} = 1;
            <@block -12>
                <#list properties as property>
                    <#if property.type.array>
                        ${var["result"]} = ( 31 * ${var["result"]} ) + ${type("java.util.Arrays")}.deepHashCode(this.${property.field});
                    <#else>
                        ${var["result"]} = ( 31 * ${var["result"]} ) + ${type("java.util.Objects")}.hashCode(this.${property.field});
                    </#if>
                </#list>
            </@block>
            return ${var["result"]};
        }
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderBuilder n=4 >
    <@block (n-8)>
        <#local var = declare("T") />
        @${type("java.lang.SuppressWarnings")}("unchecked")
        public static class Builder<${var["T"]} extends ${type(builder)}<${var["T"]}>> {

        <#list properties as property>
            private ${property.type} ${property.field};
        </#list>

        <#list properties as property>
            public ${var["T"]} with${property.id}(final ${property.type} ${property.field}) {
                this.${property.field} = ${property.field};
                return (${var["T"]}) this;
            }
        </#list>
            public ${type(name)} build() {
                return new ${type(name)}(this);
            }
        }
    </@block>
</#macro>
<#---------------------------------------------------------------------------->
/* This is generated code, edit with caution! */
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

package ${package};

<#list imports as type>
import ${type};
</#list>

public class ${id} implements ${type(base)}, ${type("java.io.Serializable")} {

    private static final long serialVersionUID = ${uid};

    <#list properties as property>
    private <#if property.setters?size == 0>final </#if>${property.type} ${property.field};
    </#list>

    <@renderConstructor/>
    <#list properties as property>
        <#if (property.isser)?? >
            <@renderGetter property.isser property.field />
        </#if>
        <#if (property.getter)?? >
            <@renderGetter property.getter property.field/>
        </#if>
        <#list property.setters as setter>
            <@renderSetter setter property.field />
        </#list>
    </#list>
    <#list methods as method>
        <@renderMethod method/>
    </#list>
    <@renderEquals/>
    <@renderHashCode/>
    <@renderToString/>
    <@renderBuilder/>
}

