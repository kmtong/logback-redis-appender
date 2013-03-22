Configurable Options
--------------------

* source: Logstash @source value
* sourceHost: Logstash @sourceHost value (default: current hostname)
* sourcePath: Logstash @sourcePath value
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

  <appender name="LOGSTASH" class="com.cwbase.logback.RedisAppender">
    <source>myApplicationName</source>
    <sourcePath>myApplicationName</sourcePath>
    <type>myApplication</type>
    <tags>production</tags>
    <host>192.168.56.10</host>
    <port>6380</port>
    <key>logstash</key>
  </appender>

