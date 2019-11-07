
[![Build Status](https://travis-ci.org/kmtong/logback-redis-appender.png?branch=master)](https://travis-ci.org/kmtong/logback-redis-appender)

# Usage

Now in Maven Central Repository:

```xml
<dependency>
  <groupId>com.cwbase</groupId>
  <artifactId>logback-redis-appender</artifactId>
  <version>1.1.6</version>
</dependency>
```

# Configurable Options

## Redis Related (RedisAppender Attributes)

* **key**: (required) Redis Key to append the logs to
* **host**: (optional, default: localhost) Redis Server Host
* **port**: (optional, default: 6379) Redis Server Port
* **timeout**: (optional, default: 2000) Redis connection timeout
* **password**: (optional, default: no password) Redis connection password
* **database**: (optional, default: 0) Redis database number

## Event Related (JSONEventLayout Attributes)

* **source**: (optional) Logstash Event [source] value
* **sourceHost**: (optional, default: current hostname) Logstash Event [host] value
* **sourcePath**: (optional) Logstash Event [path] value
* **tags**: (optional) Comma-separated strings of Logstash [tags]
* **type**: (optional) Logstash Event [type] value

Since 1.1.1 these fields support MDC property resolution by @{varname}.

## Logback/Java Specific (JSONEventLayout Attributes)

* **mdc**: (optional, default: false) Set to true if you want to log MDC properties
* **location**: (optional, default: false) Set to true if you want to log the source file
* **callerStackIndex**: (optional, default: 0) As location is determined by call stack, if you use some
  log wrapper, the location will always be the wrapper instead.
  Set it to 1 or higher to specify the particular call stack level

# Note
## Custom Layout

If you want to use other Layout (e.g. net.logstash.logback.layout.LogstashLayout) instead of our
own JSONEventLayout, see the sample configuration below.  (Since version 1.1.5)

## Logging Asynchronously

As this appender would synchronously log to the Redis server, this may cause the logging thread
to be hanged on some error conditions (network timeout or so).  One resolution would be using the
[AsyncAppender](http://logback.qos.ch/manual/appenders.html#AsyncAppender) provided by standard
logback. Please refer to the below example configurations.
(Thanks GuiSim for pointing this out)

## Default values for MDC properties
MDC properties can be configured with default values by using the `:-` signifier. For example: `@{varname:-foo}` will result in `foo` if the `varname` property is not defined.

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

## Use with AsyncAppender:

    <configuration>
      <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
        <source>mySource</source>
        <sourcePath>mySourcePath</sourcePath>
        <type>myApplication</type>
        <tags>production</tags>
        <host>192.168.56.10</host>
        <port>6379</port>
        <key>logstash</key>
      </appender>
      <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="LOGSTASH" />
      </appender>
      <root level="DEBUG">
        <appender-ref ref="ASYNC" />
      </root>
    </configuration>

## Use Custom Layout:

    <configuration>
	  <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
	    <!-- RedisAppender Attributes Here -->
        <host>192.168.56.10</host>
        <port>6379</port>
        <key>logstash</key>
        <!-- Use your own Custom Layout here -->
		<layout class="com.cwbase.logback.JSONEventLayout">
	      <!-- JSONEventLayout Attributes Here -->
          <source>mySource</source>
          <sourcePath>mySourcePath</sourcePath>
          <type>myApplication</type>
          <tags>production</tags>
		</layout>
	  </appender>
      <root level="DEBUG">
        <appender-ref ref="LOGSTASH" />
      </root>
    </configuration>

# ChangeLogs

## Version 1.1.6 -> 1.1.5

* Upgrade Jedis version to 3.1.0 and Logback to 1.2.3

## Version 1.1.5 -> 1.1.4

* Ability to set custom Layout (Thanks brynjargles suggestion)

## Version 1.1.4 -> 1.1.2

* Add support to provide additional fields to the JSON object sent to redis. (Thanks kevinvandervlist)
* Fix MDC properties in additional fields. (Thanks kevinvandervlist)

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
