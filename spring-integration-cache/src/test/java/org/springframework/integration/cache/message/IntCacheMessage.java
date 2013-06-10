package org.springframework.integration.cache.message;

import java.util.HashMap;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;

public class IntCacheMessage implements Message<CacheMessage> {

	public MessageHeaders getHeaders() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		MessageHeaders headers = new MessageHeaders(map);
		return headers;
	}

	public CacheMessage getPayload() {

		return new CacheMessage();
	}

}
