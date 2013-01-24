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
package org.springframework.integration.atmosphere.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.integration.Message;
import org.springframework.integration.atmosphere.AtmosphereHeaders;
import org.springframework.mvc.atmosphere.common.SpringAtmosphereResourceEventListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Gunnar Hillert
 * @since  0.5
 *
 */
public final class AtmosphereManager implements ApplicationEventPublisherAware {

	public static final Logger LOG = LoggerFactory.getLogger(AtmosphereManager.class);

	private ApplicationEventPublisher applicationEventPublisher;
	private boolean triggerSpringEvents;

	private Broadcaster defaultBroadcaster;
	private Map<String, Broadcaster> customBroadcasters = new HashMap<String, Broadcaster>(0);

	/**
	 * The Atmosphere suspendTimeout. By default the value is -1L, which means
	 * the response is suspended indefinitely (Integer.MAX_VALUE under the covers
	 * of Atmosphere.)
	 * */
	private long suspendTimeOut = -1L;

	public AtmosphereManager() {
		this.defaultBroadcaster = AtmosphereManager.lookupBroadcaster();
	}

	public AtmosphereManager(Broadcaster defaultBroadcaster) {
		Assert.notNull(defaultBroadcaster);
		this.defaultBroadcaster = defaultBroadcaster;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void setSuspendTimeOut(long suspendTimeOut) {
		this.suspendTimeOut = suspendTimeOut;
	}

	public void setTriggerSpringEvents(boolean triggerSpringEvents) {
		this.triggerSpringEvents = triggerSpringEvents;
	}

	public void setCustomBroadcasters(Set<Broadcaster> broadcasters) {
		Assert.notEmpty(broadcasters);

		for (Broadcaster broadcaster : broadcasters) {
			String broadcasterId = broadcaster.getID();
			Assert.doesNotContain(this.defaultBroadcaster.getID(),
					broadcasterId, "Cannot add the default broadcaster twice.");
			this.customBroadcasters.put(broadcaster.getID(), broadcaster);
		}
	}

	public Broadcaster getBroadcaster(String broadcasterId) {
		if (this.defaultBroadcaster.getID().equals(broadcasterId)) {
			return this.defaultBroadcaster;
		}
		return this.customBroadcasters.get(broadcasterId);
	}

	public Broadcaster getDefaultBroadcaster() {
		return this.defaultBroadcaster;
	}

	public AtmosphereResource getAtmosphereResource(String uuid) {

		Assert.hasText(uuid, "The provided uuid must not be empty.");
		final AtmosphereResource resource = AtmosphereResourceFactory.getDefault().find(uuid);

//		for (AtmosphereResource atmosphereResource : this.defaultBroadcaster.getAtmosphereResources()) {
//			if (uuid.equals(atmosphereResource.uuid())) {
//				return atmosphereResource;
//			}
//		}

		return resource;
	}

	public static AtmosphereResource getAtmosphereResource(HttpServletRequest request) {
		return getMeteor(request).getAtmosphereResource();
	}

	public static Meteor getMeteor(HttpServletRequest request) {
		return Meteor.build(request);
	}

	public void suspend(final AtmosphereResource resource) {

		final SpringAtmosphereResourceEventListener listener = new SpringAtmosphereResourceEventListener(applicationEventPublisher, this.triggerSpringEvents);

		resource.addEventListener(listener);

		AtmosphereManager.lookupBroadcaster().addAtmosphereResource(resource);

		if (AtmosphereResource.TRANSPORT.LONG_POLLING.equals(resource.transport())) {
			resource.resumeOnBroadcast(true).suspend(suspendTimeOut, false);
		} else {
			resource.suspend(suspendTimeOut);
		}

		listener.awaitSuspensions();

	}

	public static Broadcaster lookupBroadcaster() {
		Broadcaster b = BroadcasterFactory.getDefault().get();
		return b;
	}

	public static Set<String> getBroadcasterIds(Message<?> message) {

		final Object broadcastersNamesHeader = message.getHeaders().get(AtmosphereHeaders.BROADCASTER_NAME);

		if (broadcastersNamesHeader == null) {
			return new HashSet<String>(0);
		}

		Assert.isInstanceOf(String.class, broadcastersNamesHeader,
			String.format("The value of the messages header '%s' must be an " +
					"instance of String.", AtmosphereHeaders.BROADCASTER_NAME));

		return StringUtils.commaDelimitedListToSet((String) broadcastersNamesHeader);

	}

}
