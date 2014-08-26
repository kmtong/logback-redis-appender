package com.cwbase.logback;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class RedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	JedisPool pool;

	JSONEventLayout layout;

	// logger configurable options
	String host = "localhost";
	int port = Protocol.DEFAULT_PORT;
	String key = null;
	int timeout = Protocol.DEFAULT_TIMEOUT;
	String password = null;
	int database = Protocol.DEFAULT_DATABASE;

	public RedisAppender() {
		layout = new JSONEventLayout();
		try {
			setSourceHost(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
		}
	}

	@Override
	protected void append(ILoggingEvent event) {
		Jedis client = pool.getResource();
		try {
			String json = layout.doLayout(event);
			client.rpush(key, json);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(client);
			client = null;
		} finally {
			if (client != null) {
				pool.returnResource(client);
			}
		}
	}

	public String getSource() {
		return layout.getSource();
	}

	public void setSource(String source) {
		layout.setSource(source);
	}

	public String getSourceHost() {
		return layout.getSourceHost();
	}

	public void setSourceHost(String sourceHost) {
		layout.setSourceHost(sourceHost);
	}

	public String getSourcePath() {
		return layout.getSourcePath();
	}

	public void setSourcePath(String sourcePath) {
		layout.setSourcePath(sourcePath);
	}

	public String getTags() {
		if (layout.getTags() != null) {
			Iterator<String> i = layout.getTags().iterator();
			StringBuffer sb = new StringBuffer();
			while (i.hasNext()) {
				sb.append(i.next());
				if (i.hasNext()) {
					sb.append(',');
				}
			}
			return sb.toString();
		}
		return null;
	}

	public void setTags(String tags) {
		if (tags != null) {
			String[] atags = tags.split(",");
			layout.setTags(Arrays.asList(atags));
		}
	}

	public String getType() {
		return layout.getType();
	}

	public void setType(String type) {
		layout.setType(type);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public void setMdc(boolean flag) {
		layout.setProperties(flag);
	}

	public boolean getMdc() {
		return layout.getProperties();
	}

	public void setLocation(boolean flag) {
		layout.setLocationInfo(flag);
	}

	public boolean getLocation() {
		return layout.getLocationInfo();
	}

	public void setCallerStackIndex(int index) {
		layout.setCallerStackIdx(index);
	}

	public int getCallerStackIndex() {
		return layout.getCallerStackIdx();
	}

	@Override
	public void start() {
		super.start();
		pool = new JedisPool(new GenericObjectPoolConfig(), host, port,
				timeout, password, database);
	}

	@Override
	public void stop() {
		super.stop();
		pool.destroy();
	}

}
