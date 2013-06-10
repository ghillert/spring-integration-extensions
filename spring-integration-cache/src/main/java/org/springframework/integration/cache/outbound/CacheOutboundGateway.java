/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.cache.outbound;


import org.springframework.cache.CacheManager;
import org.springframework.integration.Message;
import org.springframework.integration.cache.CacheOutboundGatewayHeaders;
import org.springframework.integration.cache.core.CacheOutboundGatewayExecutor;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 *
 * @author Glenn Renfro
 * @since 0.5
 *
 */
public class CacheOutboundGateway extends
		AbstractReplyProducingMessageHandler {

	private final CacheOutboundGatewayExecutor cacheOutboundGatewayExecutor;
	private boolean producesReply = true; // false for outbound-channel-adapter,
											// true for outbound-gateway
	public CacheGateway cache;
	public CacheManager cacheManager = null;
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public String cacheName;

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * Constructor taking an {@link CacheOutboundGatewayExecutor} that wraps common
	 * Cache Gateway Operations.
	 *
	 * @param cacheOutboundGatewayExecutor
	 *            Must not be null
	 *
	 */
	public CacheOutboundGateway(
			CacheOutboundGatewayExecutor cacheOutboundGatewayExecutor) {

		Assert.notNull(cacheOutboundGatewayExecutor,
				"cacheOutboundGatewayExecutor must not be null.");
		this.cacheOutboundGatewayExecutor = cacheOutboundGatewayExecutor;
		cacheManager = cacheOutboundGatewayExecutor.getCacheManager();
	}

	/**
	 *
	 */
	@Override
	protected void onInit() {
		super.onInit();
		cache = new CacheGateway(cacheManager,cacheName);
	}


	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {

		final Object result;

		result = this.cacheOutboundGatewayExecutor
				.executeOutboundOperation(requestMessage);
			if (getCommand(requestMessage).equals(CacheOutboundGatewayHeaders.PUT)) {
				putData(requestMessage);
			} else {
				if (getCommand(requestMessage)
						.equals(CacheOutboundGatewayHeaders.GET)) {
					return getData(requestMessage);
				}
			}
		if (result == null || !producesReply) {
			return null;
		}
		return MessageBuilder.withPayload(result)
				.copyHeaders(requestMessage.getHeaders()).build();
	}

	private String getCommand(Message msg) {
		return (String) msg.getHeaders().get(CacheOutboundGatewayHeaders.COMMAND);
	}
	private Object getKey(Message msg) {
		return msg.getHeaders().get(CacheOutboundGatewayHeaders.KEY);
	}

	/**
	 * If set to 'false', this component will act as an Outbound Channel
	 * Adapter. If not explicitly set this property will default to 'true'.
	 *
	 * @param producesReply
	 *            Defaults to 'true'.
	 *
	 */
	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

	public void putData(Message message) {
		cache.put(getKey(message),message.getPayload());
	}

	public Object getData(Message message) {
		return cache.get(getKey(message));
	}


}
