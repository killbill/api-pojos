# api-pojos

POJO generation for killbill-api and killbill-plugin-api interfaces.

## Build

Use this command to build the tool `mvn package shade:shade`. This will produce an uberjar at `target/pojogen.jar`.
If you have file structure like the one showing in [Usage](#usage), then this is one-liner command:

```shell
mvn package shade:shade && cp target/pojogen.jar ../
```

## Usage

All usage section will assume that:
- You've been working with maven and Kill Bill project for a quite some time (Read more on [dependency problem](#dependencies-problem))
- Have following working directory:

```
    .
    ├── pojogen.jar                       # The uberjar
    ├── api-pojos                         # api-pojos project root directory (source of pojogen.jar tool)
    ├── killbill-api                      # killbill-api project root directory
    ├── killbill-plugin-api               # killbill-plugin-api project root directory
    ├── killbill-plugin-framework-java    # killbill-plugin-framework-java project root directory
```

`pojogen.jar` tool have an options to override default configuration values that you can check via `java -jar pojogen.jar --help`.

### Generate killbill-api interfaces to killbill-plugin-framework-java

To doing this, simply run `java -jar pojogen.jar`. The `pojogen.jar` tool will run command with default configuration 
defined in `<api-pojos>/src/main/resources/killbill-api-config.xml`.

### Generate killbill-plugin-api interfaces to killbill-plugin-framework-java

To doing this, simply run `java -jar pojogen.jar --source-project=plugin-api`. The `pojogen.jar` tool will run command 
with default configuration defined in `<api-pojos>/src/main/resources/killbill-plugin-api-config.xml`. 

The `--source-project` option needed here because its default value is `api`. `--source-project` also only useful if 
you not specify `<sourceDirectories />` in XML configuration or `--source-dirs` in command line option. 

### Override XML configuration file

To override configuration file, you can run, for example `java -jar pojogen.jar overridden-api-config.xml`. You can copy
`<api-pojos>/src/main/resources/killbill-api-config.xml` or `<api-pojos>/src/main/resources/killbill-plugin-api-config.xml`, 
put them in the same directory as `pojogen.jar`, and edit configuration values as needed.

### Override configuration using `--<option>`

`pojogen.jar` support some options to override one ore more default configuration. For example:

1. `java -jar pojogen.jar --output-subpackage=impl`. This will: 
   - Generate `killbill-api` interfaces to `killbill-plugin-framework-java`. 
   - XML configuration file not specified, so command above will using default
     `<api-pojos>/src/main/resources/killbill-api-config.xml` configuration. 
   - It will override `<outputSubpackage />` from `boilerplate` to `impl`.

2. `java -jar pojogen.jar --source-dirs=./killbill-plugin-api/catalog/src/main/java,./killbill-plugin-api/control/src/main/java overriden-config.xml`.
   This will: 
   - Generate only `catalog` and `control` module in `killbill-plugin-api` to `killbill-plugin-framework-java`.
   - Will use `overriden-config.xml` as XML configuration instead of its default.

## XML Configuration

To understand more about elements of XML configuration, you can check [setting](SETTINGS.md) doc.

## Command line options

Not all configuration that available in XML configuration can be overridden by command line options. You can always 
check all available options via `java -jar pojogen.jar --help` command.

## Dependency problem

It is possible that during generation, you will encounter maven error. This is because one or more local maven artifacts 
are corrupt. You can always identify [all of them](https://stackoverflow.com/a/52755704), and then try to re-download them. 
but if you found it is confusing and too much work, you can 
`mvn dependency:copy-dependencies -DoutputDirectory=../lib -Dhttps.protocols="TLSv1.2" -f ./killbill-api/pom.xml` and 
use ` --input-dependencies=lib` to fix the problem.

## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
