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

package org.ehcache.siehcache;

/**
 * EhcacheAdapter adapter specific message headers.
 *
 * @author Glenn Renfro
 * @since 1.0
 */
public class EhcacheAdapterHeaders {

	private static final String PREFIX = "ehcacheadapter_";

	public static final String EXAMPLE = PREFIX + "example_";
	
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String GET_WITH_READER = "GET_WITH_READER";
	public static final String COMMAND = "COMMAND";
	public static final String PUT_WITH_WRITER = "PUT_WITH_WRITER";
	public static final String KEY="KEY";
	public static final String FOUND="FOUND";
	

	/** Noninstantiable utility class */
	private EhcacheAdapterHeaders() {
		throw new AssertionError();
	}

}
