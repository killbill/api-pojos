
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
<#import "string.ftl" as string>
<#---------------------------------------------------------------------------->
<#function _indent input count=0 >
    <#if count == 0>
        <#return input >
    </#if>
    <#local input  = string.right_trim(input)>
    <#if input?length == 0>
        <#return input>
    </#if>
    <#if count lt 0 > 
        <#local s = string.left_trim(input) >
        <#if (input?length - s?length) gt (-count) >
            <#return input[(-count)..]>
        <#else>
            <#return s>
        </#if>
    <#else>
        <#return input?left_pad( input?length + count ) >
    </#if>
</#function>
<#---------------------------------------------------------------------------->
<#macro println value="" >
  ${value}${"\n"}<#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro print value="" >
  ${value}<#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro block left=0 bottom=0 top=0 >
    <#local capture>
        <#nested>
    </#local>
    <#if top gt 0 >
        <#list 0..<top as i>
            <#local  capture = "\n" + capture >
        </#list>
    </#if>
    <#if bottom gt 0 >
        <#list 0..<bottom as i>
            <#local capture = capture + "\n" >
        </#list>
    </#if>
    <#local lines = capture?split("\n") >
    <#local start = 0 >
    <#local end = lines?size >
    <#if top lt 0 >
        <#local start = -top >
    </#if>
    <#if bottom lt 0 >
        <#local end= lines?size + bottom >
    </#if>
    <#local output = [] >
    <#list lines as line >
        <#if ( ( line?index gte start ) &&  ( line?index lt end ) ) >
            <#local result = _indent(line, left) >
            <#if ( result?length gt 0 ) >
                <#local output += [ result ] >
            <#else>
                <#local output += [ "" ] >
            </#if>
        </#if>
    </#list>
    <@print output?join("\n") />
</#macro>
<#---------------------------------------------------------------------------->
<#macro span >
    <#local capture>
        <#nested>
    </#local>
    <@print capture.remove("\n") />
</#macro>
<#---------------------------------------------------------------------------->
<#macro join items=[] separator=" " prefix="" suffix="">
    <#if (items?size gt 0) >
        <@print ( prefix + items?join(separator) + suffix ) />
    </#if>
</#macro>
<#---------------------------------------------------------------------------->
<#macro border left="" right="" >
    <#local capture>
        <#nested>
    </#local>
    <#local lines = capture?split("\n") >
    <#local output = [] >
    <#list lines as line >
       <#local input = string.right_trim(line)>
       <#local input = string.right_trim( left + input + right )>
       <#local output += [ input ] >
    </#list>
    <@print output?join("\n") />
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
