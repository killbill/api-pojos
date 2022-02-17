# Settings

## Example of a settings file

```xml

<?xml version="1.0" encoding="UTF-8"?>
<Settings>
  <sourceDirectories>
    <sourceDirectory>input/project/src/main/java</sourceDirectory>
  </sourceDirectories>
  <dependencyDirectories>
    <dependencyDirectory>input/lib</dependencyDirectory>
  </dependencyDirectories>
  <outputSubpackage>boilerplate</outputSubpackage>
  <outputClassPrefix></outputClassPrefix>
  <outputClassSuffix>Imp</outputClassSuffix>
  <outputDirectory>output/project/src/main/java</outputDirectory>
  <testDirectory>output/project/src/test/java</testDirectory>
  <acceptedPackages>
    <package>org.killbill.billing.catalog.api</package>
  </acceptedPackages>
  <acceptedInterfaces>
  </acceptedInterfaces>
  <comparableTypes>
    <type>org.joda.time.DateTime</type>
  </comparableTypes>
</Settings>

```

## Structure of a settings file

### The root element

The `<Settings>` element is the root element of the xml file.

### The child elements of `<Settings>`

  * `<sourceDirectories>`       - Contains 1 or more `<sourceDirectory>`. Each `<sourceDirectory>` is the root directory of a Java Package Structure containing Java source code.

  * `<dependencyDirectories>`   - Contains 0 or more `<dependencyDirectory>`. Each `<dependencyDirectory>` is a directory that contains the JAR libaries the Java source code needs to compile.

  * `<outputSubpackage>`        - The subpackage to place the generated POJOs. If the package of the source interface is `org.killbill.billing.catalog.api` and the `<OutputSubpackage>` is `boilerplate`, the corresponding POJOs would be placed in the package `org.killbill.billing.catalog.api.boilerplate`.

  * `<outputClassPrefix>`       - The prefix of the names of the generated POJOs. If the name of the source interface is `SomeInterface` and `<outputClassPrefix>` is `MyPrefix`, the name of its generated POJO would be `MyPrefixSomeInterface`.[^1]

  * `<outputClassSuffix>`       - The suffix of the names of the generated POJOs. If the name of the source interface is `SomeInterface` and `<outputClassSuffix>` is `MySuffix`, the name of its generated POJO would be `SomeInterfaceMySuffix`.[^1]

  * `<outputDirectory>`         - The directory to place the generated POJOs.

  * `<testDirectory>`           - The directory to place the generated unit test for each generated POJO.

  * `<acceptedPackages>`        - Contains 0 or more `<package>`. Each  `<package>` is the name of a Java package. All top-level non-generic interfaces within the Java package will have a POJO generated for it.[^2]

  * `<acceptedInterfaces>`      - Contains 0 or more `<interface>`. Each  `<interface>` is the canonical name of a top-level non-generic Java interface. Each `<interface>` will have a POJO generated for it.[^2]

  * `<comparableTypes>`         - Contains 0 or more `<type>`. Each `<type>` is the canonical name of a type that implements `java.lang.Comparable`. The generated POJO will use `java.lang.Comparable.compareTo()` instead of `java.lang.Object.equals()` for their equality testing.



[^1]: `<outputClassPrefix>` and `<outputClassSuffix>` can be specified simultaneously.
[^2]: If `<acceptedPackages>` and `<acceptedInterface>` are both empty, POJOs will be generated for all top-level non-generic interfaces found in the Java source code

