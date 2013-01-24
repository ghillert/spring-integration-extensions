/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.atmosphere.outbound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageDeliveryException;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.atmosphere.core.AtmosphereManager;
import org.springframework.integration.atmosphere.support.AtmosphereSerializer;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * Extension of Spring Integration's {@link AbstractMessageHandler}.
 *
 * Messages that come into this component from a Spring Integration {@link MessageChannel}
 * are pushed to subscribed clients.
 *
 * @author Jeremy Grelle, Josh Long, Gunnar Hillert
 * @version 0.5
 *
 */
public class AtmosphereBroadcastingMessageHandler extends AbstractMessageHandler {

	/**  Logger declaration. */
	private static final Log LOG = LogFactory.getLog(AtmosphereBroadcastingMessageHandler.class);
	private AtmosphereManager broadcasterManager;

	/** The default Accept media type to use. Defaults to MediaType.APPLICATION_JSON.*/
	private MediaType defaultAcceptMediaType = MediaType.APPLICATION_JSON;

	private boolean useExclusivelyDefaultAcceptContentType = false;
	public static final String ENDPOINT_PATH_HEADER = "endpoint-path";
	private volatile HeaderMapper<HttpHeaders> headerMapper = new DefaultHttpHeaderMapper();

	/**
	 * The message converter for used for converting outbound Spring Integration
	 * messages. This converter is optional and by default no conversion for
	 * outbound messages will be applied.
	 */
	private HttpMessageConverter<?> messageConverter = new MappingJacksonHttpMessageConverter(); //FIXME

	/**
	 * Shall messaged cached up to a certain threshold before they are
	 * broadcasted? The default is '0', which means that messages are broadcasted
	 * immediately. However, keep in mind that message are cached in cased no
	 * Broadcaster (no subscribed clients are available).
	 */
	private volatile int messageThreshold = 0;

	/** Caches messages to be sent. */
	private volatile BlockingQueue<Message<?>> messageQueue;

	private volatile boolean extractPayload = true;

	/**
	 * The Atmosphere suspendTimeout. By default the value is -1L, which means
	 * the response is suspended indefinitely (Integer.MAX_VALUE under the covers
	 * of Atmosphere.)
	 */
	private long suspendTimeOut = -1L;

	protected void onInit() throws Exception {
		super.onInit();
		this.messageQueue = new LinkedBlockingQueue<Message<?>>();
	}

	/**
	 * Messages messages are broadcasted to suspended {@link AtmosphereResource}s
	 * using {@link Broadcaster#broadcast(Object)}.
	 *
	 * @param message the Spring Integration message
	 */
	public void handleMessageInternal(Message<?> message)
			throws MessageHandlingException, MessageDeliveryException {
		try {
			Message<?> springIntegrationMessage = MessageBuilder
					.fromMessage(message)
					.setHeaderIfAbsent("endpoint-path", getComponentName())
					.build();

			LOG.debug("Processing message: " + message.toString());

			this.messageQueue.add(springIntegrationMessage);

			if (this.messageQueue.size() <= this.messageThreshold) {
				if (LOG.isInfoEnabled()) {
					LOG.info(String
							.format("We have '%s' messages enqueued but the threshold is '%s'. Not broadcasting message: %s",
									new Object[] {
											Integer.valueOf(this.messageQueue
													.size()),
											Integer.valueOf(this.messageThreshold),
											springIntegrationMessage }));
				}
				return;
			}

			List<Message<?>> broadcastMessages = new ArrayList<Message<?>>(0);

			this.messageQueue.drainTo(broadcastMessages);

			if (broadcastMessages.size() > 0) {
				Map<Broadcaster, List<Message<?>>> messagesPerBroadcaster = new HashMap<Broadcaster, List<Message<?>>>(0);

				for (Message<?> broadcastMessage : broadcastMessages) {

					final Set<String> broadcasterIds = AtmosphereManager.getBroadcasterIds(broadcastMessage);

					if (!broadcasterIds.isEmpty()) {
						for (String broadcasterId : broadcasterIds) {
							Broadcaster broadcaster = this.broadcasterManager
									.getBroadcaster(broadcasterId);

							if (broadcaster != null) {
								if (messagesPerBroadcaster.get(broadcaster) == null) {
									messagesPerBroadcaster.put(broadcaster,
											new ArrayList<Message<?>>());
								}

								(messagesPerBroadcaster.get(broadcaster))
										.add(broadcastMessage);
							} else {
								throw new IllegalStateException(
										String.format(
												"No broadcaster found for broadcasterId '%s'",
												new Object[] { broadcasterId }));
							}
						}
					} else {
						if (messagesPerBroadcaster.get(this.broadcasterManager
								.getDefaultBroadcaster()) == null) {
							messagesPerBroadcaster.put(this.broadcasterManager
									.getDefaultBroadcaster(), new ArrayList<Message<?>>());
						}
						(messagesPerBroadcaster.get(this.broadcasterManager
										.getDefaultBroadcaster()))
								.add(broadcastMessage);
					}

				}

				for (Map.Entry<Broadcaster, List<Message<?>>> entry : messagesPerBroadcaster.entrySet()) {
					doBroadcast(entry.getKey(), entry.getValue());
				}
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Broadcast failed for message: "
					+ message, ex);
		}
	}

	private void doBroadcast(Broadcaster broadcaster,
			List<Message<?>> broadcastMessages) throws IOException,
			ExecutionException, InterruptedException {
		if (LOG.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append(String
					.format("Broadcasting %s  message(s) using the following Broadcaster: %s (Subscribers: '%s').",
							new Object[] {
									Integer.valueOf(broadcastMessages.size()),
									broadcaster.getID(),
									Integer.valueOf(broadcaster
											.getAtmosphereResources().size()) }));

			if (LOG.isDebugEnabled()) {
				sb.append(": \n");
				for (Message<?> bs : broadcastMessages) {
					sb.append("Broadcasting message: " + bs.toString() + "\n");
				}
			}

			LOG.info(sb.toString());
		}

		Message<?> mergedMessage = mergeMessagesForBroadcast(broadcastMessages);
		Object content;

		if (this.extractPayload) {
			content = mergedMessage.getPayload();
		} else {
			content = mergedMessage;
		}

		List<MediaType> acceptMediatypes = new ArrayList<MediaType>();
		acceptMediatypes.add(this.defaultAcceptMediaType);

		final Object broadcastContent;

		if (this.messageConverter != null) {
			AtmosphereSerializer serializer = new AtmosphereSerializer(
					this.defaultAcceptMediaType, this.messageConverter);
			broadcastContent = serializer.writeToString(content);
		}
		else if (content instanceof String) {
			broadcastContent = content;
		} else { //TODO see AbstractReflectorAtmosphereHandler#onStateChange
			throw new IllegalStateException("Broadcast Message must resolve to a " +
					"String. No message converters were specified and the message " +
					"to broadcast was an instance of " + content.getClass().getName());
		}

		Future<Object> future = broadcaster.broadcast(broadcastContent);

		if (future != null) {
			future.get();
		}

		if (LOG.isDebugEnabled())
			LOG.debug("Broadcast operation for " + broadcastMessages
					+ " executed.");
	}

	private Message<?> mergeMessagesForBroadcast(List<Message<?>> messages) {
		List<Object> payloads = new ArrayList<Object>();

		for (Message<?> message : messages) {
			payloads.add(message.getPayload());
		}

		return MessageBuilder.withPayload(payloads).build();
	}

	/**
	 * Cache Messages up to the set threshold.
	 * @param messageThreshold Default value = '0' (Messages are only cached if no subscribers)
	 */
	public void setMessageThreshold(int messageThreshold) {
		this.messageThreshold = messageThreshold;
	}

	/**
	 * Do you want to serialize the payload or the entire message?
	 *
	 * @param extractPayload Default to "true"
	 */
	public void setExtractPayload(boolean extractPayload) {
		this.extractPayload = extractPayload;
	}

	/**
	 * Default content type for the outbound HttpMessageConverter.
	 * If you don't set this parameter, the default "application/json" is used.
	 *
	 * @param defaultAcceptContentType Must not be empty.
	 */
	public void setDefaultAcceptContentType(String defaultAcceptContentType) {
		Assert.hasText(defaultAcceptContentType,
				"If you set it, please define a value.");

		String[] acceptMediatypeFragments = defaultAcceptContentType.split("/");

		Assert.isTrue(acceptMediatypeFragments.length == 2);

		this.defaultAcceptMediaType = new MediaType(
				acceptMediatypeFragments[0], acceptMediatypeFragments[1]);
	}

	/**
	 * Ignore any incoming Accepttype. Only use the defaultAcceptContentType to select
	 * the HttpMessageConverter to use for message serialization.
	 *
	 * @param useExclusivelyDefaultAcceptContentType Default to <code>true</code>
	 */
	public void setUseExclusivielyDefaultAcceptContentType(
			boolean useExclusivelyDefaultAcceptContentType) {
		this.useExclusivelyDefaultAcceptContentType = useExclusivelyDefaultAcceptContentType;
	}

	public void setBroadcasterManager(AtmosphereManager broadcasterManager) {
		this.broadcasterManager = broadcasterManager;
	}

	public void setMessageConverter(HttpMessageConverter<?> messageConverter) {
		this.messageConverter = messageConverter;
	}
}
