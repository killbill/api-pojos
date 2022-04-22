<#import "common.ftl" as common>
<#import "header.ftl" as header>
<#import "unit.ftl" as unit>
<#---------------------------------------------------------------------------->
<#macro renderResolver>
    <@common.block -8>
        public class ${moniker} extends ${type(base)} {
            public ${moniker}(){
                <#list implementations as implementation>
                    <@common.block -8>
                        this.addMapping(${type(implementation.base)}.class, ${type(implementation.name)}.class);
                    </@common.block>
                </#list>
            }
        }
    </@common.block>
</#macro> 
<#---------------------------------------------------------------------------->
<#macro render>
    <@common.block>
        <@header.render/>

        <@unit.package namespace/>

        <@unit.import imports/>

        <@renderResolver/>
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
<@render/>
