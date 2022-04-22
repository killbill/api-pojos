
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
