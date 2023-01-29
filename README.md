# api-pojos

POJO generation for killbill-api

## Usage

POJO generation for killbill-api is done with the tool `pojogen`.

To run the tool:

```
java -jar pojogen.jar

```

- It expects that there's [killbill-api](https://github.com/killbill/killbill-api/) project in `./killbill-api` directory. 
  This can be overridden by ` --input` option. 

- It expects that you've been working with `killbill-api` for sometimes, thus your local maven repository contains all 
  JAR required by `killbill-api` project, and `pojogen` will scan all library in your maven repository. Furthermore, you 
  may find there's an error that caused by some not-related JARs that does not exist. Just delete those JARs, as maven 
  always re-downloaded those JARs when needed. If this give too much trouble, you can: 
  `mvn dependency:copy-dependencies -DoutputDirectory=../lib -Dhttps.protocols="TLSv1.2" -f ./killbill-api/pom.xml` and 
  set ` --input-dependencies=lib`.
  As explained above, this is can be overridden by ` --input-dependencies` option.

- It will try to generate all packages in `killbill-api`. 
  This can be overridden by ` --input-packages` options, separated by comma.

- It expects that there's [killbill-plugin-framework-java](https://github.com/killbill/killbill-plugin-framework-java/) 
  project in `./killbill-plugin-framework-java` directory. 
  This can be overridden by ` --output` option.

- It expects that your ` --output` is maven based project, thus it will generate resources files in `src/main/resources` 
  directory.
  This can be overridden by ` --output-resources` option.

- All generated classes will be placed in `boilerplate` sub-package in project defined in ` --output` option. 
  This can be overridden by ` --output-subpackage` option.

- It expects that your ` --output` is maven based project, thus it will generate test files in `src/test/java` directory.
  This can be overridden by ` --output-test` option.


For all available options, you can use:
```
java -jar pojogen.jar --help
```

Please look at the [example](doc/EXAMPLE.md) for edge cases on how to generate POJOs for killbill-api. For more about 
`settings.xml` file, refers to [settings](doc/SETTINGS.md) 


## Build

To build the tool:

```
mvn package shade:shade
```

This will produce an uberjar at `target/pojogen.jar`


## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
