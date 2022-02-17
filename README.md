# api-pojos

POJO generation for killbill-api

## Usage

POJO generation for killbill-api is done with the tool `pojogen`. It takes in a XML file that contains the POJO generation [settings](doc/SETTINGS.md).

To run the tool:

```
java -jar pojogen.jar settings.xml

```
Please look at the [example](doc/EXAMPLE.md) for details on how to generate POJOs for killbill-api.


## Build

To build the tool:

```
mvn package shade:shade
```

This will produce an uberjar at `target/pojogen.jar`


## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
