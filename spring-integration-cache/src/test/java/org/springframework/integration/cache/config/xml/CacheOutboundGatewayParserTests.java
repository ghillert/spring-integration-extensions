/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.cache.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.cache.core.CacheOutboundGatewayExecutor;
import org.springframework.integration.cache.message.IntCacheMessage;
import org.springframework.integration.cache.outbound.CacheOutboundGateway;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Glenn Renfro
 * @since 1.0
 *
 */
public class CacheOutboundGatewayParserTests {

	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testGetCacheName() throws Exception {
		setUp("CacheOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		 CacheOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);
		assertEquals(
				"mycache",
				ehcacheadapterOutboundGateway.getCacheName());
		setUp("CacheOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundWithCacheManagerNoXml");
	}

	@Test
	public void testInputChannelParser() throws Exception {

			setUp("CacheOutboundGatewayParserTests.xml", getClass(),

				"ehcacheadapterOutboundGateway");

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				this.consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("getDataOutputChannel", inputChannel.getComponentName());

	}

	@Test
	public void testJpaExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"CacheOutboundGatewayParserTests.xml", getClass());
		assertNotNull(context.getBean(
				"ehcacheadapterOutboundGateway.cacheExecutor",
				CacheOutboundGatewayExecutor.class));

	}

	@Test
	public void timeOutTest() {
		setUp("CacheOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		final CacheOutboundGateway cacheOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						CacheOutboundGateway.class);

		long sendTimeout = TestUtils.getPropertyValue(
				cacheOutboundGateway, "messagingTemplate.sendTimeout",
				Long.class);

		assertEquals(100, sendTimeout);
	}
	@Test
	public void executorTest() {
		setUp("CacheOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		final CacheOutboundGatewayExecutor cacheOutboundGatewayExecutor = TestUtils
				.getPropertyValue(this.consumer,
						"handler.cacheOutboundGatewayExecutor",
						CacheOutboundGatewayExecutor.class);
		assertNotNull(cacheOutboundGatewayExecutor);
		IntCacheMessage msg = new IntCacheMessage();
		cacheOutboundGatewayExecutor.executeOutboundOperation(msg);

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
