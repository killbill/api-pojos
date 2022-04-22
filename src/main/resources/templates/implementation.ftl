
<#----------------------------------------------------------------------------|

  Copyright 2022-2022 The Billing Project, LLC

  The Billing Project licenses this file to you under the Apache License, 
  version 2.0 (the "License"); you may not use this file except in compliance 
  with the License.  You may obtain a copy of the License at:

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  License for the specific language governing permissions and limitations
  under the License.

|----------------------------------------------------------------------------->



<#---------------------------------------------------------------------------->
<#import "common.ftl" as common>
<#import "header.ftl" as header>
<#import "unit.ftl" as unit>
<#---------------------------------------------------------------------------->
<#macro renderParameter parameter name>
    <@common.join items=parameter.annotations separator=" " prefix="" suffix=" " />
    <@common.join items=parameter.modifiers separator=" " prefix="" suffix=" " />
      ${parameter.type}<#if parameter.variadic > ...</#if> ${name}<#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderParameters parameters>
    <#list parameters as parameter >
        <@renderParameter parameter parameter.name />
        <#if parameter?index lt (parameters?size - 1) >
            ${", "}<#t>
        </#if>
    </#list>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderAnnotations annotations>
    <@common.join items=annotations separator="\n" prefix="" suffix="\n" />
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderModifiers modifiers>
    <@common.join items=modifiers separator=" " prefix="" suffix=" "/>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderTypeParameters typeParameters>
    <@common.join items=typeParameters separator=", " prefix="<" suffix="> " />
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderTypeParameters typeParameters>
    <@common.join items=typeParameters separator=", " prefix="<" suffix="> " />
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderExceptions exceptions>
    <@common.join items=exceptions separator=", " prefix="throws " suffix=" "/>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderMethodHeader method>
    <@renderAnnotations method.annotations />
    <@renderModifiers method.modifiers />
    <@renderTypeParameters method.typeParameters  />
    ${method.result} <#t>
    ${method.name}(<@renderParameters method.parameters/>) <#t>
    <@renderExceptions method.exceptions/>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderMethod method >
    <@renderMethodHeader method/>
    <@common.block -8> 
        {
            throw new UnsupportedOperationException("${method.signature} must be implemented.");
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderSetterHeader method field>
    <@renderAnnotations method.annotations />
    <@renderModifiers method.modifiers />
    <@renderTypeParameters method.typeParameters  />
    ${method.result} <#t>
    ${method.name}(<@renderParameter method.parameters[0] field/>) <#t>
    <@renderExceptions method.exceptions/>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderSetter setter field>
    <@renderSetterHeader setter field/>
    <@common.block -8>
        {
            this.${field} = ${field};
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderGetter getter field>
    <@renderMethodHeader getter/>
    <@common.block -8>
        {
            return this.${field};
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderConstructor >
    <#local var = declare("that", "builder") />
    <@common.block -8>
        public ${moniker}(final ${moniker} ${var["that"]}) { 
            <#list properties as property>
            this.${property.field} = ${var["that"]}.${property.field};
            </#list>
        }
        protected ${moniker}(final ${type(builder)}<?> ${var["builder"]}) {
            <#list properties as property>
            this.${property.field} = ${var["builder"]}.${property.field};
            </#list>
        }
        protected ${moniker}() { }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderToString>
    <#local var = declare("sb") />
    <@common.block -8>
        @${type("java.lang.Override")}
        public String toString() {
            final StringBuffer ${var["sb"]} = new StringBuffer(this.getClass().getSimpleName());
            ${var["sb"]}.append("{");
            <#list properties as property>
                <#if property?index gt 0>
                    <@common.block -12 >
                        ${var["sb"]}.append(", ");
                    </@common.block>
                </#if>
                <@common.block -12>
                    <#if property.type.string>
                        ${var["sb"]}.append("${property.name}=");
                        if( this.${property.field} == null ) {
                            ${var["sb"]}.append(this.${property.field});
                        }else{
                            ${var["sb"]}.append("'").append(this.${property.field}).append("'");
                        }
                    <#elseif property.type.array>
                        ${var["sb"]}.append("${property.name}=").append(Arrays.toString(this.${property.field}));
                    <#else>
                        ${var["sb"]}.append("${property.name}=").append(this.${property.field});
                    </#if>
                </@common.block>
            </#list>
            ${var["sb"]}.append("}");
            return ${var["sb"]}.toString();
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderEquals >
    <#local var = declare("o", "that") />
    <@common.block -8>
        @${type("java.lang.Override")}
        public boolean equals(final ${type("java.lang.Object")} ${var["o"]}) {
            if ( this == ${var["o"]} ) {
                return true;
            }
            if ( ( ${var["o"]} == null ) || ( this.getClass() != ${var["o"]}.getClass() ) ) {
                return false;
            }
            <#if properties?size gt 0 >
                <@common.block -8>
                    final ${type(name)} ${var["that"]} = (${type(name)}) ${var["o"]};
                    <#list properties as property>
                        <#if property.type.primitive>
                            <@common.block -12>
                                if( this.${property.field} != ${var["that"]}.${property.field} ) {
                                    return false;       
                                }
                            </@common.block>
                        <#elseif property.type.array>
                            <@common.block -12>
                                if( !${type("java.util.Arrays")}.deepEquals(this.${property.field}, ${var["that"]}.${property.field}) ) {
                                    return false;
                                }
                            </@common.block>
                        <#elseif property.comparable>
                            <@common.block -12>
                                if( ( this.${property.field} != null ) ? ( 0 != this.${property.field}.compareTo(${var["that"]}.${property.field}) ) : ( ${var["that"]}.${property.field} != null ) ) {
                                    return false;
                                }
                            </@common.block>
                        <#else>
                            <@common.block -12>
                                if( !${type("java.util.Objects")}.equals(this.${property.field}, ${var["that"]}.${property.field}) ) {
                                    return false;
                                }
                            </@common.block>
                        </#if>
                    </#list>
                </@common.block>
            </#if>
            return true;
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderHashCode >
    <#local var = declare("result")>
    <@common.block -8>
        @${type("java.lang.Override")}
        public int hashCode() {
            int ${var["result"]} = 1;
            <@common.block -12>
                <#list properties as property>
                    <#if property.type.array>
                        ${var["result"]} = ( 31 * ${var["result"]} ) + ${type("java.util.Arrays")}.deepHashCode(this.${property.field});
                    <#else>
                        ${var["result"]} = ( 31 * ${var["result"]} ) + ${type("java.util.Objects")}.hashCode(this.${property.field});
                    </#if>
                </#list>
            </@common.block>
            return ${var["result"]};
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderBuilder >
    <@common.block -8>
        <#local var = declare("T","that") />
        @${type("java.lang.SuppressWarnings")}("unchecked")
        public static class ${builder.moniker}<${var["T"]} extends ${type(builder)}<${var["T"]}>> {

        <#list properties as property>
            protected ${property.type} ${property.field};
        </#list>

            public ${builder.moniker}() { }
            public ${builder.moniker}(final ${builder.moniker} ${var["that"]}) {
                <#list properties as property>
                this.${property.field} = ${var["that"]}.${property.field};
                </#list>
            }
        <#list properties as property>
            public ${var["T"]} with${property.id}(final ${property.type} ${property.field}) {
                this.${property.field} = ${property.field};
                return (${var["T"]}) this;
            }
        </#list>
            public ${var["T"]} source(final ${type(base)} ${var["that"]}) <@renderExceptions exceptions />{
                <#list properties as property>
                    <#if (property.isser)?? >
                        <@common.block -12>
                            this.${property.field} = ${var["that"]}.${property.isser.name}();
                        </@common.block>
                    <#elseif (property.getter)?? >
                        <@common.block -12>
                            this.${property.field} = ${var["that"]}.${property.getter.name}();
                        </@common.block>
                    </#if>
                </#list>
                return (${var["T"]}) this;
            }
            protected ${builder.moniker} validate() {
              return this;
            }
            public ${type(name)} build() {
                return new ${type(name)}(this.validate());
            }
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro renderImplementation>
    <@common.block -8>
        @${type("com.fasterxml.jackson.databind.annotation.JsonDeserialize")}( builder = ${type(builder)}.class )
        public class ${moniker} implements ${type(base)} {

            <#list properties as property>
            protected <#if property.setters?size lt 0>final </#if>${property.type} ${property.field};
            </#list>
            
            <@common.block 12>
                <@renderConstructor/>
                <#list properties as property>
                    <#if (property.isser)?? >
                        <@renderGetter property.isser property.field />
                    </#if>
                    <#if (property.getter)?? >
                        <@renderGetter property.getter property.field />
                    </#if>
                    <#list property.setters as setter>
                        <@renderSetter setter property.field />
                    </#list>
                </#list>
                <#list methods as method>
                    <@renderMethod method />
                </#list>
                <@renderEquals />
                <@renderHashCode />
                <@renderToString />

                <@renderBuilder />
            </@common.block>
        }
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#macro render>
    <@header.render/>

    <@unit.package namespace/>

    <@unit.import imports/>

    <@renderImplementation/>
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
<@render/>
