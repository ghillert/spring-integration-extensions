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

import org.springframework.context.ApplicationEvent;

/**
 * If a supporting Broadcaster is used, this ApplicationEvent is triggered each
 * time a user is subscribed or disconnected.
 *
 * @author Gunnar Hillert
 * @since 0.5
 */
public class AtmosphereApplicationEvent extends ApplicationEvent {

	/** serialVersionUID. */
	private static final long serialVersionUID = 6128193221167362164L;

	/** Broadcast related statistical information. */
	private final AtmosphereMetrics metrics;

	/**
	 * Indicates the type of event, e.g. subscription or disconnect.
	 */
	private AtmosphereEventType atmosphereEventType;

	public AtmosphereApplicationEvent(Object source, AtmosphereEventType type,
			AtmosphereMetrics metrics) {
		super(source);
		this.metrics = metrics;
		this.atmosphereEventType = type;
	}

	public AtmosphereMetrics getBroadcastMetrics() {
		return this.metrics;
	}

	/**
	 * Indicates the type of event, e.g. subscription or disconnect.
	 */
	public AtmosphereEventType getAtmosphereEventType() {
		return this.atmosphereEventType;
	}

	public static enum AtmosphereEventType {
		SUBSCRIPTION, DISCONNECT;
	}
}
