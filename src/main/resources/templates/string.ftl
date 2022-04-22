<#---------------------------------------------------------------------------->
<#function whitespace input>
    <#return ( input == " " ) || ( input == "\n" ) || ( input == "\t" ) >
</#function>
<#---------------------------------------------------------------------------->
<#function reverse input>
    <#return input?split("")?reverse?join("") >
</#function>
<#---------------------------------------------------------------------------->
<#function left_trim input>
    <#list 0..<input?length as i >
        <#if !whitespace(input[i]) >
            <#if i == 0 >
                <#return input >
            <#else>
                <#return input[i..]>
            </#if>
        </#if>
    </#list>
    <#return ("") >
</#function>
<#---------------------------------------------------------------------------->
<#function right_trim input>
    <#if input?length gt 0>
      <#list input?length-1..0 as i >
          <#if !whitespace(input[i]) >
              <#if i == 0 >
                  <#return input >
              <#else>
                  <#return input[0..i] >
              </#if>
          </#if>
      </#list>
    </#if>
    <#return ("") >
</#function>
<#---------------------------------------------------------------------------->
<#function indent input count >
    <#if count lt 0 >
        <#local s = left_trim(input) >
        <#if ( ( input?length - s?length ) lt (-count) ) >
            <#return s >
        <#else>
            <#return input[(-count)..] >
        </#if>
    <#else>
        <#return input?left_pad( input?length + count ) >
    </#if>
</#function>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
