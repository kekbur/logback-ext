Logback Extensions
==================

This is a fork of https://github.com/trautonen/logback-ext

[![Build Status](https://circleci.com/gh/WriskHQ/logback-ext.svg?style=shield&circle-token=035569d22c22736f2f7f9cd1ed994231c14481be)](https://circleci.com/gh/WriskHQ/logback-ext)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/co.wrisk.logback/logback-ext-cloudwatch-appender/badge.svg)](https://maven-badges.herokuapp.com/maven-central/co.wrisk.logback/logback-ext-cloudwatch-appender/)
![License](https://img.shields.io/github/license/WriskHQ/logback-ext.svg?style=flat-square)

Extensions for Logback logging library mainly for appenders aimed for Amazon Web Services,
including CloudWatch Logs, DynamoDB, Kinesis, SNS and SQS appenders. Contains also high
performance asynchronous appender based on LMAX disrupotr and some utilities like Jackson JSON
encoder.


## Using Logback Extensions

Logback Extensions requires Java 8 or newer. Include desired modules in your project's
dependency management and configure the appenders or encoders using Logback's XML configutation
or Java API.


### Modules

All modules belong to group `org.eluder.logback`. See each module for specific documentation.

* Extensions core module: [logback-ext-core](logback-ext-core/)
* AWS core module: [logback-ext-aws-core](logback-ext-aws-core/)
* Jackson JSON encoder: [logback-ext-jackson](logback-ext-jackson/)
* LMAX Disruptor appender: [logback-ext-lmax-appender](logback-ext-lmax-appender/)
* CloudWatch appender: [logback-ext-cloudwatch-appender](logback-ext-cloudwatch-appender/)
* DynamoDB appender: [logback-ext-dynamodb-appender](logback-ext-dynamodb-appender/)
* Kinesis appender: [logback-ext-kinesis-appender](logback-ext-kinesis-appender/)
* SNS appender: [logback-ext-sns-appender](logback-ext-sns-appender/)
* SQS appender: [logback-ext-sqs-appender](logback-ext-sqs-appender/)


### AWS Authentication

All AWS based appenders require IAM authentication. The default credentials provider from
`org.eluder.logback.ext.aws.core.AwsSupport` creates a credential chain in the following order.

1. Environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_KEY`
2. System properties `aws.accessKeyId` and `aws.secretKey`
3. Appender configuration properties `accessKey` and `secretKey`
4. AWS profile configuration file `~/.aws/credentials`
5. EC2 instance role

Best practice for EC2 instances is to use instance role only. With instance role no access keys or
secret keys are exposed if the server is compromised.


### Continuous Integration

CircleCI builds the project with Oracle JDK 8. Builds are deployed
to Sonatype OSSRH.
