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
package org.springframework.integration.atmosphere.core.broadcaster;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;

/**
*
* @author Gunnar Hillert
* @since 0.5
*/
public final class BroadcasterUtil {

	private static final Log LOG = LogFactory.getLog(BroadcasterUtil.class);

	public static final BroadcastMetrics getBroadcastMetrics(Broadcaster bc) {

		final DefaultBroadcastMetrics broadcastMetrics = new DefaultBroadcastMetrics();

		int numberOfSubscribers = bc.getAtmosphereResources().size();

		final Collection<String> subscriberIds = new ArrayList<String>(numberOfSubscribers);

		for (AtmosphereResource resource : bc.getAtmosphereResources()) {
			HttpSession session = resource.getRequest().getSession();
			subscriberIds.add(resource.uuid());

			LOG.info("Subscriber added with id " + resource.uuid());
		}

		broadcastMetrics.setNumberOfSubscribers(Integer
				.valueOf(numberOfSubscribers));
		broadcastMetrics.setSubscriberIds(subscriberIds);

		return broadcastMetrics;
	}
}
