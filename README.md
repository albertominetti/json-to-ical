# Json to iCalendar

## Overview

This small project converts a json file with the following format:

```json
[
    {
        "title": "Hai guardato \"Aeron: Open-source high-performance messaging\" by Martin Thompson",
        "titleUrl": "https://www.youtube.com/watch?v\u003dtM4YskS94b0",
        "time": "2020-10-11T20:17:25.926Z"
    },
    {
        "title": "Hai guardato Efficient Fault Tolerant Java with Aeron Clustering",
        "titleUrl": "https://www.youtube.com/watch?v\u003doPcknKn7ebY",
        "time": "2020-10-10T20:17:19.883Z"
    }
]
```

into an [iCalendar](https://tools.ietf.org/html/rfc5545) file with the following format:

```icalendar
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Michael Angstadt//biweekly 0.6.4//EN
BEGIN:VEVENT
DTSTAMP:20201014T211113Z
SUMMARY;LANGUAGE=it_IT:Hai guardato "Aeron: Open-source high-performance me
 ssaging" by Martin Thompson
DESCRIPTION:https://www.youtube.com/watch?v=tM4YskS94b0
DTSTART:20201011T201725Z
DURATION:PT5M
UID:2020-10-11T20:17:25.926Z_https://www.youtube.com/watch?v=tM4YskS94b0
END:VEVENT
BEGIN:VEVENT
DTSTAMP:20201014T211114Z
SUMMARY;LANGUAGE=it_IT:Hai guardato Efficient Fault Tolerant Java with Aero
 n Clustering
DESCRIPTION:https://www.youtube.com/watch?v=oPcknKn7ebY
DTSTART:20201010T201719Z
DURATION:PT5M
UID:2020-10-10T20:17:19.883Z_https://www.youtube.com/watch?v=oPcknKn7ebY
END:VEVENT
BEGIN:VEVENT
END:VCALENDAR
```

I used to import my YouTube history in my personal calendar to keep track of the spent time watching videos.

### How it works?

While the application is running, as soon as a new `*.json` file is available in the input folder, it will be processed for generating several iCalendar description files that contain the corresponding events. An "aggregation" step groups the data by year and month for creating smaller files that are more suitable for the bulk loading.

## Architecture and Framework

I choose to use spring-boot to quickly setup an application that can run on any JVM with a minimal configuration and Spring Framework to process, split and aggregate the bulk amount of data using pipes and filters architecture.

### Spring Integration versus Apache Camel

To meet these requirements, several libraries and frameworks are extremely useful, especially the ones that implement the [enterprise integration patterns](https://www.enterpriseintegrationpatterns.com/) described by Bobby Woolf and Gregor Hohpe in the homonyms [book](https://www.amazon.com/o/asin/0321200683/ref=nosim/enterpriseint-20). Two main frameworks implement these patterns: Apache Camel and Spring Integration.

I have extensive experience with [Apache Camel](https://camel.apache.org/) in the field of banking messaging with many standards (SEPA, SWIFT, FIX, Temenos, Caceis, Kondor+, Morning*, etc.) and many protocols (HTTP, REST, jdbc, TibcoMQ, ActiveMQ, IBM-MQ, SFTP,etc ), it has a quite nice java dsl, despite several people still use the xml description, and it fits well when involving many not homogeneous systems because of the [comprehensive components and adapters](https://camel.apache.org/components/latest/index.html).

Despite the quoted positive aspects, Apache Camel has some pitfalls that prevent me from suggesting it for a new project:
* it is not well documented, and the examples on the web are mainly with the old xml definition
* the Camel context is based on the Spring context for some important aspects, like the [transactional one](https://camel.apache.org/components/latest/eips/transactional-client.html)
* there are too many unexpected behaviors that are "standard" and obscure in Camel, like _By default, Camel will move consumed files to the .camel sub-folder relative to the directory where the file was consumed_ for the [File component](https://camel.apache.org/components/latest/file-component.html) or when it uses the inbound headers for the outbound integration
* sometime it is [extremely difficult](https://github.com/albertominetti/camel-retriable-route/blob/master/src/main/java/org/example/MainRouteBuilder.java) to achieve something that with plain java is easy, and it will lead to transform part of the business logic into a big Processor that handle the whole message
* the learning curve is extremely steep and testing is mandatory also for simplest interactions.

Till 2010, Camel had so many components to make the developer life happier; nowadays there are libraries that completely fill the initial lack of components for the other framework. So I decided to try [Spring Integration](https://spring.io/projects/spring-integration), especially now that its [java dsl](https://spring.io/blog/2014/12/01/spring-integration-java-dsl-pre-java-8-line-by-line-tutorial) is more widespread.

Final consideration about Spring Integration:
* the learning curve is smoother than the Camel one, even more for people with EIP proficiency
* the minimal configuration generates the simplest behavior, in perfect spring-boot style
* it exposes "internals" of EIP that are hidden in Camel, like channels, gateway, poller, etc.
* it is possible to ignore the internals and focus on the intention, like with Camel
* the Spring community is bigger and seems that there is more support for the newest components than with Camel, like for RSocket



### Useful resources:
 * https://github.com/mangstadt/biweekly