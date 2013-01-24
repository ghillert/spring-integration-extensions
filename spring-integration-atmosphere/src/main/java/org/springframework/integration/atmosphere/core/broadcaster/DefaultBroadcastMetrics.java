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

/**
 *
 * @author Gunnar Hillert
 * @since 0.5
 */
public class DefaultBroadcastMetrics implements BroadcastMetrics {
	private Integer numberOfSubscribers = Integer.valueOf(0);
	private Collection<String> subscriberIds = new ArrayList<String>(0);

	public Integer getNumberOfSubscribers() {
		return this.numberOfSubscribers;
	}

	public void setNumberOfSubscribers(Integer numberOfSubscribers) {
		this.numberOfSubscribers = numberOfSubscribers;
	}

	public Collection<String> getSubscriberIds() {
		return this.subscriberIds;
	}

	public void setSubscriberIds(Collection<String> subscriberIds) {
		this.subscriberIds = subscriberIds;
	}
}
