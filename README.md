# api-pojos

POJO generation tool for `killbill-api` and `killbill-plugin-api` interfaces. This tool useful for:

1. Generate default getter/setter implementation for POJO interfaces
2. Generate default business methods for business interfaces
3. Generate builder classes to build the default implementation
4. Generate Jackson's Module and Revolver classes

## Build

Use this command to build the tool `mvn package shade:shade`. This will produce an uberjar at `target/pojogen.jar`.
If you have a file structure like the one showing in the [Usage](#usage) section, then you can use the following one-line command:

```shell
mvn package shade:shade && cp target/pojogen.jar ../
```

## Usage

This section assumes that:
- You've been working with Maven and Kill Bill project for quite some time (Read more on [dependency problem](#dependency-problem))
- Have the following working directory:

```
    .
    ├── pojogen.jar                       # The uberjar
    ├── api-pojos                         # api-pojos project root directory (source of pojogen.jar tool)
    ├── killbill-api                      # killbill-api project root directory
    ├── killbill-plugin-api               # killbill-plugin-api project root directory
    ├── killbill-plugin-framework-java    # killbill-plugin-framework-java project root directory
```

To run the tool:

  `java -jar pojogen.jar`. 
  
This will run the tool with the default configuration options.

### Generate POJOs for killbill-api

To generate POJOs for the [killbill-api](https://github.com/killbill/killbill-api) repo, you can run `java -jar pojogen.jar`

Note that this will run the command with the default configuration 
defined in `<api-pojos>/src/main/resources/killbill-api-config.xml`.

### Generate POJOs for killbill-plugin-api

To generate POJOs for the [killbill-plugin-api](https://github.com/killbill/killbill-plugin-api) repo, you can run 
`java -jar pojogen.jar --source-project=plugin-api`. Note that this will run the command with the default configuration 
defined in `<api-pojos>/src/main/resources/killbill-plugin-api-config.xml`. 

The `--source-project` option is needed here because its default value is `api`. `--source-project` is also only useful if 
you do not specify `<sourceDirectories />` in XML configuration or `--source-dirs` via the command line.

### Override XML configuration file

To override configuration file, you can run,  `java -jar pojogen.jar overridden-api-config.xml`. You can copy
`<api-pojos>/src/main/resources/killbill-api-config.xml` or `<api-pojos>/src/main/resources/killbill-plugin-api-config.xml`, 
put them in the same directory as `pojogen.jar`, and edit configuration values as needed.

### Override configuration using `--<option>`

`pojogen.jar` allows overriding the default configuration options. For example:

1. `java -jar pojogen.jar --output-subpackage=impl`. This will: 
   - It will override `<outputSubpackage />` from `boilerplate` to `impl`.
   - Generate `killbill-api` POJOs to `killbill-plugin-framework-java`. 
   - XML configuration file is not specified, so the command above will using the default
     `<api-pojos>/src/main/resources/killbill-api-config.xml` configuration.

2. `java -jar pojogen.jar --source-dirs=./killbill-plugin-api/catalog/src/main/java,./killbill-plugin-api/control/src/main/java overriden-config.xml`.
   This will: 
   - Generate POJOs for only the `catalog` and `control` modules from `killbill-plugin-api`.
   - Will use `overriden-config.xml` as the XML configuration instead of the default configuration

## XML Configuration

To understand more about the elements in the XML configuration, you can check [setting](SETTINGS.md) doc.

## Command line options

Not all configuration that available in XML configuration can be overridden by command line options. You can always 
check all available options via `java -jar pojogen.jar --help` command.

## Dependency problem

It is possible that during generation, you will encounter maven error. This is because one or more local maven artifacts 
are corrupt. You can always identify [all of them](https://stackoverflow.com/a/52755704), and then try to re-download them. 
But if you find it confusing and too much work, you can run 
`mvn dependency:copy-dependencies -DoutputDirectory=../lib -Dhttps.protocols="TLSv1.2" -f ./killbill-api/pom.xml` and 
use ` --input-dependencies=lib` to fix the problem.

## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
