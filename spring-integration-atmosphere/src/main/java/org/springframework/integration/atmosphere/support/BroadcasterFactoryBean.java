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
package org.springframework.integration.atmosphere.support;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.springframework.beans.factory.FactoryBean;

/**
*
* @author Gunnar Hillert
* @since 0.5
*/
public class BroadcasterFactoryBean implements FactoryBean<Broadcaster> {

	private String broadcasterId;

	@Override
	public Broadcaster getObject() throws Exception {

		final Broadcaster broadcaster;

		if (this.broadcasterId == null) {
			broadcaster = BroadcasterFactory.getDefault().get();
		}
		else {
			broadcaster = BroadcasterFactory.getDefault().get(this.broadcasterId);
		}

		return broadcaster;

	}

	@Override
	public Class<?> getObjectType() {
		return Broadcaster.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setBroadcasterId(String broadcasterId) {
		this.broadcasterId = broadcasterId;
	}

}
