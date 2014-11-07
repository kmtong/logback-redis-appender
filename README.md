
[![Build Status](https://travis-ci.org/kmtong/logback-redis-appender.png?branch=master)](https://travis-ci.org/kmtong/logback-redis-appender)

# Usage

Now in Maven Central Repository:

```xml
<dependency>
  <groupId>com.cwbase</groupId>
  <artifactId>logback-redis-appender</artifactId>
  <version>1.1.0</version>
</dependency>
```

# Configurable Options

## Event Related

* source: Logstash Event [source] value
* sourceHost: Logstash Event [host] value (default: current hostname)
* sourcePath: Logstash Event [path] value
* tags: Comma-separated strings of Logstash [tags]
* type: Logstash Event [type] value

## Redis Related

* host: Redis Server Host (default: localhost)
* port: Redis Server Port (default: 6379)
* key: Redis Key to append the logs to
* timeout: Redis connection timeout (default: 2000)
* password: Redis connection password (default no password)
* database: Redis database number (default 0)

## Logback/Java Specific

* mdc: Set to true if you want to log MDC properties (default false)
* location: Set to true if you want to log the source file (default false)
* callerStackIndex: As location is determined by call stack, if you use some
  log wrapper, the location will always be the wrapper instead. 
  Set it to 1 or higher to specify the particular call stack level (default 0)


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


