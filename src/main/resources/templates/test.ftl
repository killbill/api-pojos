package ${package};

import org.junit.Assert;
import org.junit.Test;

public class ${id} {
  @Test
    public void anInstanceShouldBeEqualToItself()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();
      Assert.assertTrue(a.equals(a));
    }
  @Test
    public void anInstanceShouldBeEqualToItsCopy()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();
      ${implementation.id} b =  new ${implementation.id}(a);

        Assert.assertTrue(a.equals(b));
    }
  @Test
    public void twoNewInstancesShouldBeEqual()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();
      ${implementation.id} b =  new ${implementation.id}.Builder().build();

      Assert.assertTrue(a.equals(b));
    }
  @Test
    public void twoNewInstancesHashcodeShouldBeEqual()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();
      ${implementation.id} b =  new ${implementation.id}.Builder().build();

      Assert.assertTrue(a.hashCode() == b.hashCode());
    }
  @Test
    public void twoNewInstancesStringShouldBeEqual()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();
      ${implementation.id} b =  new ${implementation.id}.Builder().build();

      Assert.assertTrue(a.toString().equals(b.toString()));
    }
  @Test
    public void callIssers()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();

      <#list implementation.properties as property>
          <#if (property.isser)?? >
      a.${property.isser.name}();
          </#if>
      </#list>
    }
  @Test
    public void callGetters()
    {
      ${implementation.id} a =  new ${implementation.id}.Builder().build();

      <#list implementation.properties as property>
          <#if (property.getter)?? >
      a.${property.getter.name}();
          </#if>
      </#list>
    }
}
