<#---------------------------------------------------------------------------->
<#import "string.ftl" as string>
<#---------------------------------------------------------------------------->
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
<#macro println value>
  ${value}${"\n"}<#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro print value>
  ${value}<#t>
</#macro>
<#---------------------------------------------------------------------------->
<#macro block right=0 bottom=0 top=0 >
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
            <#local result = _indent(line, right) >
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
