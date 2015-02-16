package com.cwbase.logback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;

/**
 * Adapt from XMLLayout
 * 
 * @author kmtong
 * 
 */
public class JSONEventLayout extends LayoutBase<ILoggingEvent> {

	private final int DEFAULT_SIZE = 256;
	private final int UPPER_LIMIT = 2048;
	private final static char DBL_QUOTE = '"';
	private final static char COMMA = ',';

	private StringBuilder buf = new StringBuilder(DEFAULT_SIZE);
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
	private Pattern MDC_VAR_PATTERN = Pattern.compile("\\@\\{([^}]*)\\}");

	private boolean locationInfo = false;
	private int callerStackIdx = 0;
	private boolean properties = false;

	String source;
	String sourceHost;
	String sourcePath;
	List<String> tags;
	String type;

	@Override
	public void start() {
		super.start();
	}

	/**
	 * The <b>LocationInfo</b> option takes a boolean value. By default, it is
	 * set to false which means there will be no location information output by
	 * this layout. If the the option is set to true, then the file name and
	 * line number of the statement at the origin of the log statement will be
	 * output.
	 * 
	 * <p>
	 * If you are embedding this layout within an
	 * <code>org.apache.log4j.net.SMTPAppender</code> then make sure to set the
	 * <b>LocationInfo</b> option of that appender as well.
	 */
	public void setLocationInfo(boolean flag) {
		locationInfo = flag;
	}

	/**
	 * Returns the current value of the <b>LocationInfo</b> option.
	 */
	public boolean getLocationInfo() {
		return locationInfo;
	}

	/**
	 * Sets whether MDC key-value pairs should be output, default false.
	 * 
	 * @param flag
	 *            new value.
	 * @since 1.2.15
	 */
	public void setProperties(final boolean flag) {
		properties = flag;
	}

	/**
	 * Gets whether MDC key-value pairs should be output.
	 * 
	 * @return true if MDC key-value pairs are output.
	 * @since 1.2.15
	 */
	public boolean getProperties() {
		return properties;
	}

	/**
	 * Formats a {@link ILoggingEvent} in conformity with the log4j.dtd.
	 */
	public synchronized String doLayout(ILoggingEvent event) {

		// Reset working buffer. If the buffer is too large, then we need a new
		// one in order to avoid the penalty of creating a large array.
		if (buf.capacity() > UPPER_LIMIT) {
			buf = new StringBuilder(DEFAULT_SIZE);
		} else {
			buf.setLength(0);
		}

		Map<String, String> mdc = event.getMDCPropertyMap();
		buf.append("{");
		appendKeyValue(buf, "source", source, mdc);
		buf.append(COMMA);
		appendKeyValue(buf, "host", sourceHost, mdc);
		buf.append(COMMA);
		appendKeyValue(buf, "path", sourcePath, mdc);
		buf.append(COMMA);
		appendKeyValue(buf, "type", type, mdc);
		buf.append(COMMA);
		appendKeyValue(buf, "tags", tags, mdc);
		buf.append(COMMA);
		appendKeyValue(buf, "message", event.getFormattedMessage(), null);
		buf.append(COMMA);
		appendKeyValue(buf, "@timestamp",
				df.format(new Date(event.getTimeStamp())), null);
		buf.append(COMMA);

		// ---- fields ----
		appendKeyValue(buf, "logger", event.getLoggerName(), null);
		buf.append(COMMA);
		appendKeyValue(buf, "level", event.getLevel().toString(), null);
		buf.append(COMMA);
		appendKeyValue(buf, "thread", event.getThreadName(), null);
		buf.append(COMMA);
		appendKeyValue(buf, "level", event.getLevel().toString(), null);
		IThrowableProxy tp = event.getThrowableProxy();
		if (tp != null) {
			buf.append(COMMA);
			String throwable = ThrowableProxyUtil.asString(tp);
			appendKeyValue(buf, "throwable", throwable, null);
		}
		if (locationInfo) {
			StackTraceElement[] callerDataArray = event.getCallerData();
			if (callerDataArray != null
					&& callerDataArray.length > callerStackIdx) {
				buf.append(COMMA);
				buf.append("\"location\":{");
				StackTraceElement immediateCallerData = callerDataArray[callerStackIdx];
				appendKeyValue(buf, "class",
						immediateCallerData.getClassName(), null);
				buf.append(COMMA);
				appendKeyValue(buf, "method",
						immediateCallerData.getMethodName(), null);
				buf.append(COMMA);
				appendKeyValue(buf, "file", immediateCallerData.getFileName(),
						null);
				buf.append(COMMA);
				appendKeyValue(buf, "line",
						Integer.toString(immediateCallerData.getLineNumber()),
						null);
				buf.append("}");
			}
		}

		/*
		 * <log4j:properties> <log4j:data name="name" value="value"/>
		 * </log4j:properties>
		 */
		if (properties) {
			Map<String, String> propertyMap = event.getMDCPropertyMap();
			if ((propertyMap != null) && (propertyMap.size() != 0)) {
				Set<Entry<String, String>> entrySet = propertyMap.entrySet();
				buf.append(COMMA);
				buf.append("\"properties\":{");
				Iterator<Entry<String, String>> i = entrySet.iterator();
				while (i.hasNext()) {
					Entry<String, String> entry = i.next();
					appendKeyValue(buf, entry.getKey(), entry.getValue(), null);
					if (i.hasNext()) {
						buf.append(COMMA);
					}
				}
				buf.append("}");
			}
		}
		buf.append("}");

		return buf.toString();
	}

	private void appendKeyValue(StringBuilder buf, String key, String value,
			Map<String, String> mdc) {
		if (value != null) {
			buf.append(DBL_QUOTE);
			buf.append(escape(key));
			buf.append(DBL_QUOTE);
			buf.append(':');
			buf.append(DBL_QUOTE);
			buf.append(escape(mdcSubst(value, mdc)));
			buf.append(DBL_QUOTE);
		} else {
			buf.append(DBL_QUOTE);
			buf.append(escape(key));
			buf.append(DBL_QUOTE);
			buf.append(':');
			buf.append("null");
		}
	}

	private void appendKeyValue(StringBuilder buf, String key,
			List<String> values, Map<String, String> mdc) {
		buf.append(DBL_QUOTE);
		buf.append(escape(key));
		buf.append(DBL_QUOTE);
		buf.append(':');
		buf.append('[');
		if (values != null) {
			Iterator<String> i = values.iterator();
			while (i.hasNext()) {
				String v = i.next();
				buf.append(DBL_QUOTE);
				buf.append(escape(mdcSubst(v, mdc)));
				buf.append(DBL_QUOTE);
				if (i.hasNext()) {
					buf.append(',');
				}
			}
		}
		buf.append(']');
	}

	private String mdcSubst(String v, Map<String, String> mdc) {
		if (mdc != null && v != null && v.contains("@{")) {
			Matcher m = MDC_VAR_PATTERN.matcher(v);
			StringBuffer sb = new StringBuffer(v.length());
			while (m.find()) {
				String val = mdc.get(m.group(1));
				if (val == null) {
					val = m.group(1) + "_NOT_FOUND";
				}
				m.appendReplacement(sb, Matcher.quoteReplacement(val));
			}
			m.appendTail(sb);
			return sb.toString();
		}
		return v;
	}

	private String escape(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				if (ch >= '\u0000' && ch <= '\u001F') {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}// for
		return sb.toString();
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceHost() {
		return sourceHost;
	}

	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCallerStackIdx() {
		return callerStackIdx;
	}

	/**
	 * Location information dump with respect to call stack level. Some
	 * framework (Play) wraps the original logging method, and dumping the
	 * location always log the file of the wrapper instead of the actual caller.
	 * For PlayFramework, I use 2.
	 * 
	 * @param callerStackIdx
	 */
	public void setCallerStackIdx(int callerStackIdx) {
		this.callerStackIdx = callerStackIdx;
	}

}
