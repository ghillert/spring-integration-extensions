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
package org.springframework.integration.atmosphere.inbound;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.atmosphere.AtmosphereHeaders;
import org.springframework.integration.atmosphere.core.AtmosphereManager;
import org.springframework.integration.atmosphere.outbound.AtmosphereBroadcastingMessageHandler;
import org.springframework.integration.http.inbound.HttpRequestHandlingEndpointSupport;
import org.springframework.web.HttpRequestHandler;

/**
 * Extension of Spring Integration's {@link HttpRequestHandlingEndpointSupport}.
 *
 * This component is handling client Http requests for subscribing to the {@link Broadcaster}.
 * The component also handles messages being push to other subscribed users.
 * Messages are not directly pushed to subscribed clients but rather forwarded
 * to a Spring Integration channel and then broadcasted using a separate
 * {@link AtmosphereBroadcastingMessageHandler}.
 *
 * @author Gunnar Hillert
 * @since 0.5
 *
 */
public class AtmosphereRequestHandlingMessagingGateway extends HttpRequestHandlingEndpointSupport
					implements HttpRequestHandler {

	/**  Logger declaration. */
	private static final Log LOG = LogFactory.getLog(AtmosphereRequestHandlingMessagingGateway.class);

	protected AtmosphereManager atmosphereManager;

	private volatile boolean convertExceptions;

	public static final String ENDPOINT_PATH_HEADER = "endpoint-path";

	public AtmosphereRequestHandlingMessagingGateway() {
		super(true);
		super.setHeaderMapper(org.springframework.mvc.atmosphere.common.DefaultHttpHeaderMapper.inboundMapper());
	}

	public void setBroadcasterManager(AtmosphereManager broadcasterManager) {
		this.atmosphereManager = broadcasterManager;
	}

	/**
	 * Handles the HTTP request by generating a Message and sending it to the
	 * request channel. If this gateway's 'expectReply' property is true, it will
	 * also generate a response from the reply Message once received. That response
	 * will be written by the {@link HttpMessageConverter}s.
	 *
	 * @param servletRequest
	 * @param servletResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public final void handleRequest(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws ServletException,
			IOException {

		final AtmosphereResource resource = AtmosphereManager.getAtmosphereResource(servletRequest);
		resource.setBroadcaster(this.atmosphereManager.getDefaultBroadcaster());

		HttpMethod httpMethod = HttpMethod.valueOf(servletRequest.getMethod());

		switch (httpMethod) {
		case GET:
			handleGet(resource);
			break;
		case POST:
			handlePost(servletRequest, servletResponse);
			break;
		default:
			throw new IllegalStateException("Unsupported Http Method: "
					+ httpMethod);
		}
	}

	protected void handlePost(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException {
		publish(servletRequest, servletResponse);
	}

	protected void handleGet(AtmosphereResource resource)
			throws IOException {
		subscribe(resource);
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
	}

	@Override
	protected void doStart() {
		super.doStart();
	}

	@Override
	protected void doStop() {
		super.doStop();
	}

	private void publish(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {

		final AtmosphereResource atmosphereResource = AtmosphereManager.getAtmosphereResource(servletRequest);

		//see https://github.com/Atmosphere/atmosphere/wiki/Understanding-AtmosphereResource
		final String originalUuid = (String) servletRequest.getAttribute(ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID);

		Object responseContent = null;

		CustomHeaderWrapper hw = new CustomHeaderWrapper(servletRequest);
		hw.getCustomHeaders().put(AtmosphereHeaders.ATMOSHERE_RESOURCE_UUID, originalUuid);

		try {
			responseContent = super.doHandleRequest(hw, servletResponse);
		}
		catch (Exception e) {
			responseContent = this.handleExceptionInternal(e);
		}

		if (responseContent != null) {

			if (responseContent instanceof Message<?>) {
				Message<?> responseMessage = (Message<?>) responseContent;
				atmosphereResource.getBroadcaster().broadcast(responseMessage.getPayload());
			}
			else {
				atmosphereResource.getBroadcaster().broadcast(responseContent);
			}

		}

	}

	private void subscribe(AtmosphereResource resource) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Handling subscription request for resource with broadcaster ID: "
					+ resource.getBroadcaster().getID());
		}

		this.atmosphereManager.suspend(resource);

	}

	private Object handleExceptionInternal(Exception e) throws IOException {
		if (this.convertExceptions && isExpectReply()) {
			return e;
		}
		else {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			else {
				throw new MessagingException("error occurred handling HTTP request", e);
			}
		}
	}

//	@SuppressWarnings({"unchecked", "rawtypes"})
//	private void writeResponse(Object content, ServletServerHttpResponse response, List<MediaType> acceptTypes) throws IOException {
//		if (CollectionUtils.isEmpty(acceptTypes)) {
//			acceptTypes = Collections.singletonList(MediaType.ALL);
//		}
//		for (HttpMessageConverter converter : this.getMessageConverters()) {
//			for (MediaType acceptType : acceptTypes) {
//				if (converter.canWrite(content.getClass(), acceptType)) {
//					converter.write(content, acceptType, response);
//					return;
//				}
//			}
//		}
//		throw new MessagingException("Could not convert reply: no suitable HttpMessageConverter found for type ["
//				+ content.getClass().getName() + "] and accept types [" + acceptTypes + "]");
//	}

	private class CustomHeaderWrapper extends HttpServletRequestWrapper {
		public CustomHeaderWrapper(HttpServletRequest request) {
			super(request);
		}

		private Map<String, String> customHeaders = new HashMap<String, String>();

		public String getHeader(String name) {
			String header = super.getHeader(name);
			return (header != null) ? header : customHeaders.get(name);
		}

		@Override
		public Enumeration getHeaders(String name) {

			Vector<String> vector = new Vector<String>();

			for (Enumeration<?> headerValues = super.getHeaders(name); headerValues
					.hasMoreElements();) {
				String headerValue = (String) headerValues.nextElement();
				vector.add(headerValue);
			}

			if (customHeaders.containsKey(name)) {
				vector.add(customHeaders.get(name));
			}

			return vector.elements();

		}

		@Override
		public Enumeration getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			names.addAll(customHeaders.keySet());
			return Collections.enumeration(names);
		}

		public Map<String, String> getCustomHeaders() {
			return customHeaders;
		}

	}
}
