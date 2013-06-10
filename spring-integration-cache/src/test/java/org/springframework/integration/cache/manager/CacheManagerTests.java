/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.cache.manager;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Glenn Renfro
 * @since 1.0
 *
 */
public class CacheManagerTests {

	private ConfigurableApplicationContext context;

	@Test
	public void testCacheAccess() throws Exception {
			this.context = new ClassPathXmlApplicationContext(
					"CacheAccessTests.xml", getClass());
			CacheManager mgr = context.getBean("cacheManager", CacheManager.class);
			assertNotNull(mgr);
			mgr.getCache("mycache").put("1", "my one");
			assertNotNull(mgr.getCache("mycache").get("1"));

	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

}
