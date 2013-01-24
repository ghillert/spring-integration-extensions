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
package org.springframework.integration.samples.atmosphere;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.atmosphere.AtmosphereHeaders;
import org.springframework.integration.atmosphere.core.AtmosphereManager;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class ServiceActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceActivator.class);

	@Autowired
	@Qualifier("twitter")
	private SourcePollingChannelAdapter twitter;

	@Autowired
	private Broadcaster timeBroadCaster;

	@Autowired
	AtmosphereManager atmosphereManager;

	public Message<String> execute(Message<String> message) {

		final String uuid = (String) message.getHeaders().get(AtmosphereHeaders.ATMOSHERE_RESOURCE_UUID);
		final AtmosphereResource atmosphereResource = this.atmosphereManager.getAtmosphereResource(uuid);

		if ("startTwitter".equalsIgnoreCase(message.getPayload())) {
			twitter.start();
			LOGGER.info("Twitter Adapter started.");
		} else if ("stopTwitter".equalsIgnoreCase(message.getPayload())) {
			twitter.stop();
			LOGGER.info("Twitter Adapter stopped.");
		} else if ("subscribeToTimeService".equalsIgnoreCase(message.getPayload())) {

			if (atmosphereResource != null) {
				timeBroadCaster.addAtmosphereResource(atmosphereResource);
				LOGGER.info("Subscribed to timeService.");
				return MessageBuilder.withPayload("Subscribed to timeService.").build();
			} else {
				LOGGER.info("Not subscribed to timeService since atmosphereResource is null.");
			}

		} else if ("unsubscribeFromTimeService".equalsIgnoreCase(message.getPayload())) {

			if (atmosphereResource != null) {
				timeBroadCaster.removeAtmosphereResource(atmosphereResource);
				LOGGER.info("Unsubscribed from timeService.");
				return MessageBuilder.withPayload("Unsubscribed from timeService.").build();
			} else {
				LOGGER.info("Not unsubscribed to timeService since atmosphereResource is null.");
			}

		}

		return null;
	}

}
