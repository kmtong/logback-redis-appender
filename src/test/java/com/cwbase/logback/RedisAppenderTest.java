package com.cwbase.logback;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisAppenderTest {

	@Test
	public void logTest() throws Exception {
		// refer to logback.xml in test folder
		String key = "logstash";
		JedisPool pool = new JedisPool("localhost");
		Jedis redis = pool.getResource();
		// clear the redis list first
		redis.ltrim(key, 1, 0);

		Logger logger = LoggerFactory.getLogger(RedisAppenderTest.class);
		logger.debug("Test Log #1");
		logger.debug("Test Log #2");
		logger.debug("Test Log #3");
		logger.debug("Test Log #4");
		logger.debug("Test Log #5");

		// list length check
		long len = redis.llen(key);
		assertEquals(5L, len);

		// Use Jackson to check JSON content
		String content = redis.lpop(key);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(content);

		assertEquals("test-application", node.get("@source").asText());
		assertEquals("Test Log #1", node.get("@message").asText());
	}
}
