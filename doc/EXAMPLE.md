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

Add the following dependency to the Maven project file `output/project/pom.xml`. This is required to build the POJOs and their tests.

```xml
<dependency>
    <groupId>org.kill-bill.billing</groupId>
    <artifactId>killbill-api</artifactId>
    <version>0.53.17</version>
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
Tells the tool to look for the Java source code of killbill-api in `input/project/src/main/java`.


```xml
  <dependencyDirectories>
    <dependencyDirectory>input/lib</dependencyDirectory>
  </dependencyDirectories>
```
Tells the tool where to look for the JAR dependencies of killbill-api in `input/lib`.


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
Tells the tool to save the Java source code for the POJOs in `output/project/src/main/java`.


```xml
  <testDirectory>output/project/src/test/java</testDirectory>
```
Tells the tool to save the Java source code for the tests in `output/project/src/test/java`.


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
Tells the tool to generate POJOs that uses `java.lang.Comparable.compareTo()` instead of `java.lang.Object.equals()` when comparing `org.joda.time.DateTime`.


