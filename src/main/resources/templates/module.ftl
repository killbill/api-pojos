<#import "common.ftl" as common>
<#import "license.ftl" as license>
<#import "unit.ftl" as unit>
<#---------------------------------------------------------------------------->
<#macro renderModule>
    <@common.block -8>
        public class ${moniker} extends ${type(base)} {
            public ${moniker}(){
                this.setAbstractTypes(new ${type(resolver.name)}());
            }
            @${type("java.lang.Override")}
            public String getModuleName(){
                return "${namespace}";
            }
        }
    </@common.block>
</#macro> 
<#---------------------------------------------------------------------------->
<#macro render>
    <@common.block>
        <@license.render/>

        <@unit.package namespace/>

        <@unit.import imports/>

        <@renderModule/>
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
<@render/>
