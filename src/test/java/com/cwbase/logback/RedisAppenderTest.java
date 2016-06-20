package com.cwbase.logback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RedisAppenderTest {

	String key = "logstash";
	Jedis redis;

	@Test
	public void logTest() throws Exception {
		// refer to logback.xml in test folder
		configLogger("/logback.xml");
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

		assertEquals("test-application", node.get("source").asText());
		assertEquals("Test Log #1", node.get("message").asText());
		assertEquals("MyValue", node.get("MyKey").asText());
		assertEquals("MyOtherValue", node.get("MySecondKey").asText());
	}

	@Test
	public void logTestMDC() throws Exception {
		// refer to logback-mdc.xml in test folder
		configLogger("/logback-mdc.xml");
		Logger logger = LoggerFactory.getLogger(RedisAppenderTest.class);
		MDC.put("mdcvar1", "test1");
		MDC.put("mdcvar2", "test2");
		logger.debug("Test MDC Log");

		String content = redis.lpop(key);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(content);
		ArrayNode tags = (ArrayNode) node.get("tags");
		assertEquals("test1", tags.get(0).asText());
		assertEquals("test2", tags.get(1).asText());
		assertEquals("test1 test2 mdcvar3_NOT_FOUND foo", tags.get(2).asText());
		assertEquals("test1", node.get("MyKey").asText());
		assertEquals("test2", node.get("MySecondKey").asText());
	}

	@Test
	public void logTestAsync() throws Exception {
		long SIZE = 100L;
		long WAIT = 5000L;
		// refer to logback-async.xml in test folder
		configLogger("/logback-async.xml");
		Logger logger = LoggerFactory.getLogger(RedisAppenderTest.class);
		for (long i = 0; i < SIZE; i++) {
			logger.debug("Test Async Log {}", i);
		}
		// probably not immediately have the same size
		long size0 = redis.llen(key);
		System.out.println("Log Size: " + size0);
		assertTrue(size0 < SIZE);

		Thread.sleep(WAIT);

		long size1 = redis.llen(key);
		System.out.println("Log Size After Wait: " + size1);
		assertTrue(size0 < size1);
	}

	@Test
	public void logTestCustomLayout() throws Exception {
		// refer to logback-custom-layout.xml in test folder
		configLogger("/logback-custom-layout.xml");
		Logger logger = LoggerFactory.getLogger(RedisAppenderTest.class);
		logger.debug("Test Custom Layout Log");
		long len = redis.llen(key);
		assertEquals(1L, len);
		String content = redis.lpop(key);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(content);

		assertEquals("test-application", node.get("source").asText());
		assertEquals("Test Custom Layout Log", node.get("message").asText());
		assertEquals(InetAddress.getLocalHost().getHostName(), node.get("host").asText());
	}

	@Before
	public void setUp() {
		System.out.println("Before Test, clearing Redis");
		JedisPool pool = new JedisPool("localhost");
		redis = pool.getResource();
		// clear the redis list first
		redis.ltrim(key, 1, 0);
	}

	protected void configLogger(String loggerxml) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(this.getClass().getResourceAsStream(loggerxml));
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}
}
