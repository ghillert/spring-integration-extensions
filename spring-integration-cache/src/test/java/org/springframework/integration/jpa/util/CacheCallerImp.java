/*
 * Copyright 2002-2012 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.springframework.integration.jpa.util;

import net.sf.ehcache.Element;

import org.ehcache.siehcache.EhcacheAdapterHeaders;
import org.springframework.integration.support.MessageBuilder;

/**
 * Provides string manipulation services.
 */
public class CacheCallerImp{

	/**
	 * Converts a String to Upper Case.
	 *
	 * @author Your Name Here
	 * @version 1.0
	 *
	 * @param stringToConvert The string to convert to upper case
	 * @return The converted upper case string.
	 */

	public Object putData(Object message){
		return MessageBuilder.withPayload(message).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.PUT)
				.build();
	}

	public Object getData(Object message){
		return MessageBuilder.withPayload(message).setHeader(EhcacheAdapterHeaders.COMMAND,EhcacheAdapterHeaders.GET)
				.build();
	}
	public Object printData(Object message){
		Object payload = message;
		System.out.println("Payload is "+payload);

		return null;
	}
}
