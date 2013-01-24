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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.atmosphere.cpr.Serializer;

/**
*
* @author Gunnar Hillert
* @since 0.5
*/
public class AtmosphereSerializer implements Serializer {

	private MediaType mediaType;
	final HttpMessageConverter<Object> messageConverter;

	public void write(OutputStream os, Object o) throws IOException {

		SimleHttpOutputMessage simpleHttpOutputMessage = new SimleHttpOutputMessage(os);
		simpleHttpOutputMessage.getHeaders().setContentType(this.mediaType);
		this.messageConverter.write(o, this.mediaType, new SimleHttpOutputMessage(os));
	}

	@SuppressWarnings("unchecked")
	public AtmosphereSerializer(MediaType mediaType, HttpMessageConverter<?> messageConverter) {
		Assert.notNull(mediaType, "'mediaType' must not be null.");
		Assert.notNull(messageConverter, "'messageConverter' must not be null.");

		this.mediaType = mediaType;
		this.messageConverter = (HttpMessageConverter<Object>) messageConverter;
	}

	public String writeToString(Object content) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();

		write(bs, content);

		try {
			return bs.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Problem converting serialized message (ByteArrayOutputStream) to String", e);
		}

	}

	private class SimleHttpOutputMessage implements HttpOutputMessage {
		private OutputStream os;
		private HttpHeaders httpHeaders = new HttpHeaders();

		public SimleHttpOutputMessage(OutputStream os) {
			this.os = os;
		}

		public HttpHeaders getHeaders() {
			return this.httpHeaders;
		}

		public OutputStream getBody() throws IOException {
			return this.os;
		}
	}
}
