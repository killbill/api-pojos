<#import "common.ftl" as common>
<#import "header.ftl" as header>
<#import "unit.ftl" as unit>
<#---------------------------------------------------------------------------->
<#macro renderTest >
    <#local var = declare("a", "b", "mapper", "s") />
    <@common.block -8>
            
        @${type("org.testng.annotations.Test")}(groups = { "fast" })
        public class ${moniker} {
            @${type("org.testng.annotations.Test")}
            public void anInstanceShouldBeEqualToItself()
            {
              ${type(target)} ${var["a"]} = new ${type(target)}.Builder<>().build();
              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.equals(${var["a"]}));
            }
            @${type("org.testng.annotations.Test")}
            public void anInstanceShouldBeEqualToItsCopy()
            {
              ${type(target)} ${var["a"]} = new ${type(target)}.Builder<>().build();
              ${type(target)} ${var["b"]} = new ${type(target)}(${var["a"]});

              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.equals(${var["b"]}));
            }
            @${type("org.testng.annotations.Test")}
            public void twoNewInstancesShouldBeEqual()
            {
              ${type(target)} ${var["a"]} = new ${type(target)}.Builder<>().build();
              ${type(target)} ${var["b"]} = new ${type(target)}.Builder<>().build();

              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.equals( ${var["b"]}));
            }
            @${type("org.testng.annotations.Test")}
            public void twoNewInstancesHashcodeShouldBeEqual()
            {
              ${type(target)} ${var["a"]} =  new ${type(target)}.Builder<>().build();
              ${type(target)} ${var["b"]} =  new ${type(target)}.Builder<>().build();

              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.hashCode() == ${var["b"]}.hashCode());
            }
            @${type("org.testng.annotations.Test")}
            public void twoNewInstancesStringShouldBeEqual()
            {
              ${type(target)} ${var["a"]} =  new ${type(target)}.Builder<>().build();
              ${type(target)} ${var["b"]} =  new ${type(target)}.Builder<>().build();

              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.toString().equals(${var["b"]}.toString()));
            }
            @${type("org.testng.annotations.Test")}
            public void callIssers()
            {
              ${type(target)} ${var["a"]} =  new ${type(target)}.Builder<>().build();

              <#list target.properties as property>
                  <#if (property.isser)?? >
                      <@common.block -12>
                          ${var["a"]}.${property.isser.name}();
                      </@common.block>
                  </#if>
              </#list>
            }
            @${type("org.testng.annotations.Test")}
            public void callGetters()
            {
              ${type(target)} ${var["a"]} =  new ${type(target)}.Builder<>().build();

              <#list target.properties as property>
                  <#if (property.getter)?? >
                      <@common.block -12>
                          ${var["a"]}.${property.getter.name}();
                      </@common.block>
                  </#if>
              </#list>
            }
            @${type("org.testng.annotations.Test")}
            public void jsonRoundtrip()
              throws ${type("java.io.IOException")}, 
                ${type("com.fasterxml.jackson.core.JsonParseException")}, 
                ${type("com.fasterxml.jackson.core.JsonProcessingException")}, 
                ${type("com.fasterxml.jackson.databind.JsonMappingException")} 
            {
              ${type("com.fasterxml.jackson.databind.ObjectMapper")} ${var["mapper"]} = new ${type("com.fasterxml.jackson.databind.ObjectMapper")}();
              ${var["mapper"]}.disable(${type("com.fasterxml.jackson.databind.SerializationFeature")}.WRITE_DATES_AS_TIMESTAMPS);
              ${var["mapper"]}.registerModule(new ${type("com.fasterxml.jackson.datatype.joda.JodaModule")}());
              ${var["mapper"]}.setDateFormat(new ${type("com.fasterxml.jackson.databind.util.StdDateFormat")}().withColonInTimeZone(true));
              ${var["mapper"]}.findAndRegisterModules();

              ${type(target)} ${var["a"]} =  new ${type(target)}.Builder<>().build();
              ${type("java.lang.String")} ${var["s"]} =  mapper.writeValueAsString(${var["a"]});;
              ${type(target.base)} ${var["b"]} = mapper.readValue(${var["s"]}, ${type(target.base)}.class);

              ${type("org.testng.Assert")}.assertTrue(${var["a"]}.equals( ${var["b"]}));
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

        <@renderTest/>
    </@common.block>
</#macro>
<#---------------------------------------------------------------------------->
<#---------------------------------------------------------------------------->
<@render/>
