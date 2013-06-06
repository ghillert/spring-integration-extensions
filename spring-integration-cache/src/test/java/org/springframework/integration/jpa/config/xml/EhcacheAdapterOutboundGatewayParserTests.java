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
package org.springframework.integration.jpa.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.ehcache.siehcache.core.EhcacheAdapterExecutor;
import org.ehcache.siehcache.outbound.EhcacheAdapterOutboundGateway;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.ehcache.message.EhCacheMessage;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Glenn Renfro
 * @since 1.0
 * 
 */
public class EhcacheAdapterOutboundGatewayParserTests {

	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testGetEhcacheXml() throws Exception {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		 EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertEquals(
				"/Users/glennrenfro/Documents/workspace-sts-2.9.2.RELEASE/SIEhCacheTestApp/configs/ehcache.xml",
				ehcacheadapterOutboundGateway.getEhcacheXml());
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundWithCacheManagerNoXml");
		ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);
		assertEquals(null,ehcacheadapterOutboundGateway.getEhcacheXml());
		
	}

	@Test
	public void testInputChannelParser() throws Exception {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(
				this.consumer, "inputChannel", AbstractMessageChannel.class);

		assertEquals("getDataOutputChannel", inputChannel.getComponentName());

	}

	@Test
	public void testJpaExecutorBeanIdNaming() throws Exception {

		this.context = new ClassPathXmlApplicationContext(
				"EhcacheAdapterOutboundGatewayParserTests.xml", getClass());
		assertNotNull(context.getBean(
				"ehcacheadapterOutboundGateway.ehcacheadapterExecutor",
				EhcacheAdapterExecutor.class));

	}


	@Test
	public void timeOutTest() {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);

		long sendTimeout = TestUtils.getPropertyValue(
				ehcacheadapterOutboundGateway, "messagingTemplate.sendTimeout",
				Long.class);

		assertEquals(100, sendTimeout);
	}
	@Test
	public void cacheManagerTest() {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundWithCacheManager");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);

		assertEquals("myManager", ehcacheadapterOutboundGateway.getCacheManagerName());
	}
	@Test
	public void executorTest() {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundGateway");
		final EhcacheAdapterExecutor ehcacheadapterExecutor = TestUtils
				.getPropertyValue(this.consumer,
						"handler.ehcacheadapterExecutor",
						EhcacheAdapterExecutor.class);
		assertNotNull(ehcacheadapterExecutor);
		EhCacheMessage msg = new EhCacheMessage();
		ehcacheadapterExecutor.executeOutboundOperation(msg);

	}
	@Test
	public void cacheNameTest() {
		setUp("EhcacheAdapterOutboundGatewayParserTests.xml", getClass(),
				"ehcacheadapterOutboundWithCacheManager");
		final EhcacheAdapterOutboundGateway ehcacheadapterOutboundGateway = TestUtils
				.getPropertyValue(this.consumer, "handler",
						EhcacheAdapterOutboundGateway.class);

		assertEquals("mycache", ehcacheadapterOutboundGateway.getCache());
	

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
