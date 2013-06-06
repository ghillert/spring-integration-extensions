package org.ehcache.siehcache.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.ehcache.siehcache.EhcacheAdapterHeaders;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

public class EhcacheAdapterOutboundGatewayTests {
	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testNoCacheManager() {
		String key = "1";
		String value = "1 value";
		//Test the Put

		Message msg = MessageBuilder.withPayload(value).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.PUT)
				.setHeader("replyChannel", "endChannel").setHeader(EhcacheAdapterHeaders.KEY, key).build();

		setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
				"noCacheManagerGateway");

		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		ehcacheadapterOutboundGateway.handleMessage(msg);
		Element message = ehcacheadapterOutboundGateway.mgr.getCache(ehcacheadapterOutboundGateway.getCache()).get("1");
		assertNotNull(message);
		assertEquals("1 value",message.getValue());
		ehcacheadapterOutboundGateway.getDefaultCacheManager();
		CacheManager mgr = ehcacheadapterOutboundGateway.mgr;
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		assertEquals(mgr,ehcacheadapterOutboundGateway.getDefaultCacheManager());
		assertEquals("myCache", ehcacheadapterOutboundGateway.getCache());
	}


	@Test
	public void testCreateNewManager() {
		setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
				"ehcacheadapterOutboundWithCacheManager");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		ehcacheadapterOutboundGateway.getDefaultCacheManager();
		CacheManager mgr = ehcacheadapterOutboundGateway.mgr;
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		assertEquals(mgr,ehcacheadapterOutboundGateway.getDefaultCacheManager());

		assertEquals("mycache", ehcacheadapterOutboundGateway.getCache());	}


	@Test
	public void testCreateCache() {
		//Test the Put
		String key = "2";
		String value = "2 value";
		Message msg = MessageBuilder.withPayload(value).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.PUT)
				.setHeader("replyChannel", "endChannel").setHeader(EhcacheAdapterHeaders.KEY, key).build();

		setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
				"noCacheGateway");

		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		ehcacheadapterOutboundGateway.handleMessage(msg);
		Element message = ehcacheadapterOutboundGateway.mgr.getCache(ehcacheadapterOutboundGateway.getCache()).get("2");
		assertNotNull(message);
		assertEquals("2 value",message.getValue());
		ehcacheadapterOutboundGateway.getDefaultCacheManager();
		CacheManager mgr = ehcacheadapterOutboundGateway.mgr;
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
		assertEquals(mgr,ehcacheadapterOutboundGateway.getDefaultCacheManager());
		assertEquals("noCache", ehcacheadapterOutboundGateway.getCache());
	}


	@Test
	public void testDefaults() {
		setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
				"defaultSettingGateway");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
	}
	@Test
	public void testBadEhcacheXML() {
		setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
				"badSettingGateway");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertNotNull(ehcacheadapterOutboundGateway.mgr);
	}

	@Test
	public void testHandleRequestInvalid() {
		//Test the Put
		String key = "3";
		String value = "3 value";
		 Message msg = MessageBuilder.withPayload(value).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.PUT)
			.setHeader("replyChannel", "endChannel").setHeader(EhcacheAdapterHeaders.KEY, key).build();
		 setUp("EhcacheAdapterOutboundGatewayTests.xml", getClass(),
					"ehcacheadapterOutboundWithCacheManager");
			final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
					.getPropertyValue(this.consumer, "handler",
							EhcacheAdapterOutboundGateway.class);
			ehcacheadapterOutboundGateway.handleMessage(msg);
			Element message = ehcacheadapterOutboundGateway.mgr.getCache(ehcacheadapterOutboundGateway.getCache()).get("3");
			assertNotNull(message);
			assertEquals("3 value",message.getValue());
		//Test the Get
			msg = MessageBuilder.withPayload(message).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.GET)
					.setHeader("replyChannel", "endChannel").build();
			ehcacheadapterOutboundGateway.handleMessage(msg);
			Cache c = ehcacheadapterOutboundGateway.mgr.getCache(ehcacheadapterOutboundGateway.getCache());
			int size = c.getSize();
			//Test a bad put
			msg = MessageBuilder.withPayload("crap").setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.PUT)
					.setHeader("replyChannel", "endChannel").build();
				ehcacheadapterOutboundGateway.handleMessage(msg);
			assertEquals(size, c.getSize());
			message = ehcacheadapterOutboundGateway.mgr.getCache(ehcacheadapterOutboundGateway.getCache()).get("3");
			assertNotNull(message);
			assertEquals("3 value",message.getValue());

	}

	@Test
	public void testGetElement() {
		//fail("Not yet implemented");
	}
	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String gatewayId) {
		context = new ClassPathXmlApplicationContext(name, cls);
		consumer = this.context.getBean(gatewayId, EventDrivenConsumer.class);
	}
}
