package org.springframework.integration.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.cache.outbound.CacheOutboundGateway;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

public class CacheOutboundGatewayTests {
	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testDefaults() {
		setUp("CacheOutboundGatewayTests.xml", getClass(),
				"defaultSettingGateway");
		final CacheOutboundGateway cacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		assertNotNull(cacheOutboundGateway.cacheManager);
	}

	@Test
	public void testBadCommand(){
		String key = "3";
		String value = "3 value";
		Message msg = MessageBuilder
				.withPayload(value)
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						"dfafa")
				.setHeader("replyChannel", "endChannel")
				.setHeader(CacheOutboundGatewayHeaders.KEY, key).build();
		setUp("CacheOutboundGatewayTests.xml", getClass(),
				"cacheOutboundWithCacheManager");
		final CacheOutboundGateway cacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		cacheOutboundGateway.handleMessage(msg);

	}

	@Test
	public void testBadEhcacheXML() {
		setUp("CacheOutboundGatewayTests.xml", getClass(),
				"badSettingGateway");
		final CacheOutboundGateway cacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		assertNotNull(cacheOutboundGateway.cacheManager);
	}

	@Test
	public void testHandleRequestInvalid() {
		// Test the Put
		String key = "3";
		String value = "3 value";
		Message msg = MessageBuilder
				.withPayload(value)
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.PUT)
				.setHeader("replyChannel", "endChannel")
				.setHeader(CacheOutboundGatewayHeaders.KEY, key).build();
		setUp("CacheOutboundGatewayTests.xml", getClass(),
				"cacheOutboundWithCacheManager");
		final CacheOutboundGateway cacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		cacheOutboundGateway.handleMessage(msg);
		ValueWrapper message = cacheOutboundGateway.cacheManager
				.getCache(cacheOutboundGateway.getCacheName())
				.get("3");
		assertNotNull(message);
		assertEquals("3 value", message.get());
		// Test the Get
		msg = MessageBuilder
				.withPayload(message)
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.GET)
				.setHeader("replyChannel", "endChannel").build();
		cacheOutboundGateway.handleMessage(msg);
		Cache c = cacheOutboundGateway.cacheManager
				.getCache(cacheOutboundGateway.getCacheName());

		// Test a bad put
		msg = MessageBuilder
				.withPayload("Test 2nd Put")
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.PUT)
				.setHeader("replyChannel", "endChannel").build();
		cacheOutboundGateway.handleMessage(msg);
		message = cacheOutboundGateway.cacheManager.getCache(
				cacheOutboundGateway.getCacheName()).get("3");
		assertNotNull(message);
		assertEquals("3 value", message.get());

	}

	@Test
	public void testSimpleCacheAdapter() {
		// Test the Put
		String key = "3";
		String value = "3 value";
		Message msg = MessageBuilder
				.withPayload(value)
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.PUT)
				.setHeader("replyChannel", "endChannel")
				.setHeader(CacheOutboundGatewayHeaders.KEY, key).build();
		setUp("CacheOutboundGatewayTests.xml", getClass(),
				"simpleCacheManagerOutboundGateway");
		final CacheOutboundGateway simpleCacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		simpleCacheOutboundGateway.handleMessage(msg);
		ValueWrapper message = simpleCacheOutboundGateway.cacheManager
				.getCache(simpleCacheOutboundGateway.getCacheName()).get("3");
		assertNotNull(message);
		assertEquals("3 value", message.get());
		// Test the Get
		msg = MessageBuilder
				.withPayload(message)
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.GET)
				.setHeader("replyChannel", "endChannel").build();
		simpleCacheOutboundGateway.handleMessage(msg);
		Cache c = simpleCacheOutboundGateway.cacheManager
				.getCache(simpleCacheOutboundGateway.getCacheName());

		// Test a bad put
		msg = MessageBuilder
				.withPayload("Test Second Put")
				.setHeader(CacheOutboundGatewayHeaders.COMMAND,
						CacheOutboundGatewayHeaders.PUT)
				.setHeader("replyChannel", "endChannel").build();
		simpleCacheOutboundGateway.handleMessage(msg);
		message = simpleCacheOutboundGateway.cacheManager.getCache(
				simpleCacheOutboundGateway.getCacheName()).get("3");
		assertNotNull(message);
		assertEquals("3 value", message.get());
	}

	@Test
	public void testGetElement() {
		// fail("Not yet implemented");
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
