
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
        <@header.render/>

        <@unit.package namespace/>

        <@unit.import imports/>

        <@renderModule/>
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
<@render/>
