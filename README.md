
[![Build Status](https://travis-ci.org/kmtong/logback-redis-appender.png?branch=master)](https://travis-ci.org/kmtong/logback-redis-appender)

Configurable Options
--------------------

* source: Logstash @source value
* sourceHost: Logstash @source_host value (default: current hostname)
* sourcePath: Logstash @source_path value
* tags: Comma-separated strings of Logstash tags
* type: Logstash type
* host: Redis Server Host (default: localhost)
* port: Redis Server Port (default: 6379)
* key: Redis Key to append the logs to
* timeout: Redis connection timeout (default: 2000)
* password: Redis connection password (default no password)
* database: Redis database number (default 0)

Example
-------

Logback XML Configuration:

    <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
        <source>myApplicationName</source>
        <sourcePath>myApplicationName</sourcePath>
        <type>myApplication</type>
        <tags>production</tags>
        <host>192.168.56.10</host>
        <port>6379</port>
        <key>logstash</key>
    </appender>

Logstash Configuration:

    input {
     redis {
      data_type => "list"
      format => "json_event"
      host => "192.168.56.10"
      key => "logstash"
      port => 6379
      type => "myApplication"
     }
    }
