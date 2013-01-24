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
package org.springframework.mvc.atmosphere.common;

import java.util.concurrent.CountDownLatch;

import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mvc.atmosphere.common.AtmosphereApplicationEvent.AtmosphereEventType;

/**
 * @author Gunnar Hillert
 * @since 0.5
 */
public class SpringAtmosphereResourceEventListener implements
		AtmosphereResourceEventListener {

	public static final Logger LOG = LoggerFactory.getLogger(SpringAtmosphereResourceEventListener.class);

	final CountDownLatch countDownLatch = new CountDownLatch(1);
	private boolean triggerSpringEvents;
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 *
	 */
	public SpringAtmosphereResourceEventListener(ApplicationEventPublisher applicationEventPublisher, boolean triggerSpringEvents) {
		super();
		this.applicationEventPublisher = applicationEventPublisher;
		this.triggerSpringEvents = triggerSpringEvents;
	}

	/* (non-Javadoc)
	 * @see org.atmosphere.cpr.AtmosphereResourceEventListener#onSuspend(org.atmosphere.cpr.AtmosphereResourceEvent)
	 */
	@Override
	public void onSuspend(AtmosphereResourceEvent event) {
		countDownLatch.countDown();
		LOG.info("Suspending Client..." + event.getResource().uuid());
		event.getResource().removeEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.atmosphere.cpr.AtmosphereResourceEventListener#onResume(org.atmosphere.cpr.AtmosphereResourceEvent)
	 */
	@Override
	public void onResume(AtmosphereResourceEvent event) {
		LOG.info("Resuming Client..." + event.getResource().uuid());
	}

	/* (non-Javadoc)
	 * @see org.atmosphere.cpr.AtmosphereResourceEventListener#onDisconnect(org.atmosphere.cpr.AtmosphereResourceEvent)
	 */
	@Override
	public void onDisconnect(AtmosphereResourceEvent event) {
		LOG.info("Disconnecting Client..." + event.getResource().uuid());

		if (triggerSpringEvents) {
			this.applicationEventPublisher.publishEvent(new AtmosphereApplicationEvent(this, AtmosphereEventType.DISCONNECT, this.getAtmosphereMetrics(event)));
		}
	}

	/* (non-Javadoc)
	 * @see org.atmosphere.cpr.AtmosphereResourceEventListener#onBroadcast(org.atmosphere.cpr.AtmosphereResourceEvent)
	 */
	@Override
	public void onBroadcast(AtmosphereResourceEvent event) {
		LOG.info("Client is broadcasting..." + event.getResource().uuid());
	}

	/* (non-Javadoc)
	 * @see org.atmosphere.cpr.AtmosphereResourceEventListener#onThrowable(org.atmosphere.cpr.AtmosphereResourceEvent)
	 */
	@Override
	public void onThrowable(AtmosphereResourceEvent event) {
		LOG.error("Client error..." + event.getResource().uuid());
	}

	public void awaitSuspensions() {
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			LOG.error("Interrupted while trying to suspend resource.");
		}
	}

	private AtmosphereMetrics getAtmosphereMetrics(AtmosphereResourceEvent event) {
		AtmosphereMetrics metrics = new AtmosphereMetrics();

		metrics.setNumberOfSubscribers(event.broadcaster().getAtmosphereResources().size());
		metrics.setSubscriberId(event.getResource().uuid());

		return metrics;
	}
}
