package org.springframework.integration.cache.outbound;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class CacheGateway {
	CacheManager mgr;
	Cache cache;

	public CacheGateway(CacheManager mgr, String cacheName){
		this.mgr = mgr;
		cache = mgr.getCache(cacheName);
	}
	public Object get(Object key){
		return cache.get(key);
	}
	public void put (Object key, Object value){
		cache.put(key,value);
	}
}
