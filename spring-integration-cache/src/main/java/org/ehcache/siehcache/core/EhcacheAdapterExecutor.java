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
package org.ehcache.siehcache.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * Bundles common core logic for the EhcacheAdapter components.
 *
 * @author Glenn Renfro
 * @since 1.0
 *
 */
public class EhcacheAdapterExecutor implements InitializingBean {

	private static final Log logger = LogFactory.getLog(EhcacheAdapterExecutor.class);



	/**
	 * Constructor.
	 */
	public EhcacheAdapterExecutor() {}

	/**
	 * Verifies and sets the parameters. E.g. initializes the to be used
	 */
	public void afterPropertiesSet() {
		
	}

	/**
	 * Executes the outbound EhcacheAdapter Operation.
	 *
	 */
	public Object executeOutboundOperation(final Message<?> message) {


		return message.getPayload();

	}

	/**
	 * Execute the EhcacheAdapter operation. Delegates to
	 * {@link EhcacheAdapterExecutor#poll(Message)}.
	 */
	public Object poll() {
		return poll(null);
	}

	/**
	 * Execute a retrieving (polling) EhcacheAdapter operation.
	 *
	 * @param requestMessage May be null.
	 * @return The payload object, which may be null.
	 */
	public Object poll(final Message<?> requestMessage) {

		if (logger.isWarnEnabled()) {
			logger.warn("Logic not implemented, yet.");
		}

		return MessageBuilder.fromMessage(requestMessage).build();
	}

}
