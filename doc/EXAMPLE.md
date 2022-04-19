# Example

The following example details how to generate POJOs for the all the top-level non-generic interfaces in the package `org.killbill.billing.catalog.api` of `killbill-api`.

## Building the POJO generator tool 

We need to build the POJO generator tool `pojogen`.

```

# Clone the tool's repo as 'tool'
git clone https://github.com/killbill/api-pojos.git tool

# Enter the 'tool' directory
cd tool

# Build the tool as an uberjar
mvn package shade:shade

# Exit the tool directory
cd ..

# Copy the uberjar to the base directory
cp tool/target/pojogen.jar .

```

We can now test the tool by running it.

```
java  -jar pojogen.jar --help 

```

## Setting up the input 

We need to download the source code and dependencies of `killbill-api` as the tool needs those to generate POJOs.

```

# Make the 'input' directory 
mkdir input

# Enter the 'input' directory
cd input 

# Clone killbill-api as 'project'
git clone https://github.com/killbill/killbill-api.git project

# Fetch killbill-api's dependencies and place them in the 'lib' directory
mvn dependency:copy-dependencies -DoutputDirectory=../lib -Dhttps.protocols="TLSv1.2" -f project/pom.xml

# Exit the 'input' directory
cd ..

```

## Output

We need to create a maven project to compile and test the generate POJOs.

```

# Make the 'output' directory
mkdir output 

# Enter the 'output' directory
cd output

# Generate a new maven project in the 'project' directory
mvn archetype:generate -DgroupId=output  -DartifactId=project -DarchetypeVersion=1.4 -DinteractiveMode=false

# Exit the 'output' directory
cd ..

```

Add the following dependencies to the Maven project file `output/project/pom.xml`. They are required to build the POJOs and their tests.

```xml
<dependency>
    <groupId>org.kill-bill.billing</groupId>
    <artifactId>killbill-api</artifactId>
    <version>0.53.17</version>
</dependency>
<dependency>
  <groupId>joda-time</groupId>
  <artifactId>joda-time</artifactId>
  <version>2.10.14</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.13.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-joda</artifactId>
    <version>2.13.2</version>
</dependency>
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.5</version>
    <scope>test</scope>
</dependency>
```

## Create the settings file

Create a file containing the folowing xml and save it as killbill-api-catalog.xml

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
  <resourceDirectory>output/project/src/main/resources</resourceDirectory>
  <testDirectory>output/project/src/test/java</testDirectory>
  <acceptedPackages>
    <package>org.killbill.billing.catalog.api</package>
  </acceptedPackages>
  <acceptedInterfaces>
  </acceptedInterfaces>
  <comparableTypes>
    <type>org.joda.time.DateTime</type>
  </comparableTypes>
  <builderClass>Builder</builderClass>
  <resolverClass>Resolver</resolverClass>
  <moduleClass>Module</moduleClass>
</Settings>

```

## Running the tool

Run the following command to generate the POJOs.

```
java  -jar pojogen.jar  killbill-api-catalog.xml
```

## Testing the POJOS

```

# Enter the output maven project directory
cd output/project

# Running the unit tests
mvn test

# Exit the directory
cd ../..

```

## Taking apart killbill-api-catalog.xml


```xml
  <sourceDirectories>
    <sourceDirectory>input/project/src/main/java</sourceDirectory>
  </sourceDirectories>
```
Tells the tool that the root directory of the Java Package Structure of `killbill-api` is `input/project/src/main/java`.
In a Java Package Structure, each subdirectory corresponds to a Java package.

```xml
  <dependencyDirectories>
    <dependencyDirectory>input/lib</dependencyDirectory>
  </dependencyDirectories>
```
Tells the tool that the JAR dependencies of `killbill-api` is in `input/lib`.


```xml
  <outputSubpackage>boilerplate</outputSubpackage>
  <outputClassPrefix></outputClassPrefix>
  <outputClassSuffix>Imp</outputClassSuffix>
```
Tells the tool how to name the generated POJO and the package to place it in.
For example, the interface `org.killbill.billing.catalog.api.Unit` would have a corresponding POJO `org.killbill.billing.catalog.api.boilerplate.UnitImp`.


```xml
  <outputDirectory>output/project/src/main/java</outputDirectory>
```
Tells the tool to output the Java Package Structure of the generated POJOs in `output/project/src/main/java`.

```xml
  <resourceDirectory>output/project/src/main/resources</resourceDirectory>
```
Tells the tool to output the generated resources in `output/project/src/main/resources`.


```xml
  <testDirectory>output/project/src/test/java</testDirectory>
```
Tells the tool to output the Java Package Structure of the the test units in `output/project/src/test/java`.


```xml
  <acceptedPackages>
      <package>org.killbill.billing.catalog.api</package>
  </acceptedPackages>
```
Tells the tool to generate POJOs for all the top-level non-generic interfaces in `org.killbill.billing.catalog.api`.


```xml
  <acceptedInterfaces>
  </acceptedInterfaces>
```
No particular interface specified.


```xml
  <comparableTypes>
      <type>org.joda.time.DateTime</type>
  </comparableTypes>
```
Tells the tool to generate POJOs that uses `java.lang.Comparable.compareTo()` instead of `java.lang.Object.equals()` when comparing instances of `org.joda.time.DateTime`.

```xml
  <builderClass>Builder</builderClass>
```
Tells the tool to name the static inner class implementing the Builder pattern for each generated POJO `Builder`.


```xml
  <resolverClass>Resolver</resolverClass>
```
Tells the tool to name the class implementing `com.fasterxml.jackson.databind.AbstractTypeResolver' for each package `Resolver`.


```xml
  <moduleClass>Module</moduleClass>
```
Tells the tool to name the class implementing `com.fasterxml.jackson.databind.Module` for each package `Module`.

