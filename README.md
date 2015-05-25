
[![Build Status](https://travis-ci.org/kmtong/logback-redis-appender.png?branch=master)](https://travis-ci.org/kmtong/logback-redis-appender)

# Usage

Now in Maven Central Repository:

```xml
<dependency>
  <groupId>com.cwbase</groupId>
  <artifactId>logback-redis-appender</artifactId>
  <version>1.1.2</version>
</dependency>
```

# Configurable Options

## Redis Related

* **key**: (required) Redis Key to append the logs to
* **host**: (optional, default: localhost) Redis Server Host 
* **port**: (optional, default: 6379) Redis Server Port 
* **timeout**: (optional, default: 2000) Redis connection timeout 
* **password**: (optional, default: no password) Redis connection password 
* **database**: (optional, default: 0) Redis database number 

## Event Related

* **source**: (optional) Logstash Event [source] value
* **sourceHost**: (optional, default: current hostname) Logstash Event [host] value 
* **sourcePath**: (optional) Logstash Event [path] value
* **tags**: (optional) Comma-separated strings of Logstash [tags]
* **type**: (optional) Logstash Event [type] value

Since 1.1.1 these fields support MDC property resolution by @{varname}.

## Logback/Java Specific

* **mdc**: (optional, default: false) Set to true if you want to log MDC properties 
* **location**: (optional, default: false) Set to true if you want to log the source file 
* **callerStackIndex**: (optional, default: 0) As location is determined by call stack, if you use some
  log wrapper, the location will always be the wrapper instead. 
  Set it to 1 or higher to specify the particular call stack level 


# Example

## Logback XML Configuration:

    <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
        <source>mySource</source>
        <sourcePath>mySourcePath</sourcePath>
        <type>myApplication</type>
        <tags>production</tags>
        <host>192.168.56.10</host>
        <port>6379</port>
        <key>logstash</key>
    </appender>

## Logstash Configuration:

    input {
     redis {
      codec => json
      host => "192.168.56.10"
      port => 6379
      key => "logstash"
      data_type => "list"
     }
    }


# ChangeLogs

## Version 1.1.1 -> 1.1.2

* Test the Redis Connection before borrow, see #9.

## Version 1.1.0 -> 1.1.1

* Implemented MDC property resolution by @{fieldname} in appender configuration.  See #8.

## Version 1.0.0 -> 1.1.0

* https://github.com/elasticsearch/logstash/blob/master/lib/logstash/codecs/oldlogstashjson.rb
* https://github.com/elasticsearch/logstash/blob/master/lib/logstash/event.rb

Logstash has re-defined its JSON message format as
[An event is simply a tuple of (timestamp, data).] 

     {
       "@timestamp": "2013-02-09T20:39:26.234Z",
       "@version": "1",
       message: "hello world"
     }

This implies that the @-prefixed keys is not longer valid and allows a more flexible event data.
The original mapping can be found at oldlogstashjson.rb file.

There is one unmapped field "@source".  I will turn that into "source" field anyways.


