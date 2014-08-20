package com.cwbase.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONEventLayoutTest {
  private ObjectMapper mapper;

  @Before public void before() {
    mapper = new ObjectMapper();
  }

  @Test public void timestampFormat() throws IOException {
    JSONEventLayout layout = new JSONEventLayout();

    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.getTimeStamp()).thenReturn(new DateTime("2014-01-01", DateTimeZone.forOffsetHours(-8)).getMillis());
    when(event.getLevel()).thenReturn(Level.INFO);

    String msg = layout.doLayout(event);

    ObjectNode node = (ObjectNode) mapper.readTree(msg.getBytes("UTF-8"));

    Assert.assertEquals("2014-01-01T00:00:00.0-0800", node.get("@timestamp").asText());
  }
}
