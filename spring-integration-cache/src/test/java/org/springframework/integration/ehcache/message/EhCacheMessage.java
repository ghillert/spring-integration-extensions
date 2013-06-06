package org.springframework.integration.ehcache.message;

import java.util.HashMap;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;

public class EhCacheMessage implements Message<CacheMessage> {

	public MessageHeaders getHeaders() {
		// TODO Auto-generated method stub
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("hi","there");
		MessageHeaders headers = new MessageHeaders(map);
		return headers;
	}

	public CacheMessage getPayload() {

		return new CacheMessage();
	}

}
