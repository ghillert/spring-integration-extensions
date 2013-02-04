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
package org.springframework.integration.atmosphere.config.xml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.atmosphere.inbound.AtmosphereRequestHandlingMessagingGateway;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.http.inbound.HttpRequestHandlingController;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for the 'inbound-channel-adapter' and 'inbound-gateway' elements of
 * the 'atmoshere' namespace. The constructor's boolean value specifies whether a
 * reply is to be expected. This value should be 'false' for the
 * 'inbound-channel-adapter' and 'true' for the 'inbound-gateway'.
 *
 * @author Gunnar Hillert
 * @since 0.5
 *
 */
public class AtmosphereInboundEndpointParser extends AbstractSingleBeanDefinitionParser {

	private final boolean expectReply;

	public AtmosphereInboundEndpointParser(boolean expectReply) {
		this.expectReply = expectReply;
	}

	@Override
	protected Class<?> getBeanClass(Element element) {
		if (element.hasAttribute("custom-implementation-class")) {
			try {
				return Class.forName(element.getAttribute("custom-implementation-class"));
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Error parsing 'custom-implementation-class' attribute.", e);
			}
		}
		return element.hasAttribute("view-name") ? HttpRequestHandlingController.class
				: AtmosphereRequestHandlingMessagingGateway.class;
	}

	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String id = super.resolveId(element, definition, parserContext);
		if (!StringUtils.hasText(id)) {
			id = element.getAttribute("name");
		}
		if (!StringUtils.hasText(id)) {
			parserContext.getReaderContext().error(
					"The 'id' or 'name' is required.", element);
		}
		return id;
	}

	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		builder.addConstructorArgValue(Boolean.valueOf(this.expectReply));
		String inputChannelAttributeName = getInputChannelAttributeName();
		String inputChannelRef = element
				.getAttribute(inputChannelAttributeName);
		if (!StringUtils.hasText(inputChannelRef)) {
			parserContext.getReaderContext().error(
					"a '" + inputChannelAttributeName + "' reference is required", element);
		}
		builder.addPropertyReference("requestChannel", inputChannelRef);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder,
				element, "error-channel");
		if (this.expectReply) {
			IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "reply-channel");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "request-timeout");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "reply-timeout");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "extract-reply-payload");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "reply-key");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "convert-exceptions");
		} else {
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "send-timeout", "requestTimeout");
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "supported-methods", "supportedMethodNames");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "request-payload-type");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "view-name");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "errors-key");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "error-code");
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "message-converters");

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "broadcaster-manager");

		String headerMapper = element.getAttribute("header-mapper");
		String mappedRequestHeaders = element.getAttribute("mapped-request-headers");
		String mappedResponseHeaders = element.getAttribute("mapped-response-headers");

		if (StringUtils.hasText(headerMapper)) {
			if ((StringUtils.hasText(mappedRequestHeaders))
					|| (StringUtils.hasText(mappedResponseHeaders))) {
				parserContext
						.getReaderContext()
						.error("Neither 'mappped-request-headers' or 'mapped-response-headers' attributes are allowed when a 'header-mapper' has been specified.",
								parserContext.extractSource(element));
			}
			builder.addPropertyReference("headerMapper", headerMapper);
		} else {
			BeanDefinitionBuilder headerMapperBuilder = BeanDefinitionBuilder
					.genericBeanDefinition("org.springframework.integration.http.support.DefaultHttpHeaderMapper");
			headerMapperBuilder.setFactoryMethod("inboundMapper");

			IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, "mapped-request-headers", "inboundHeaderNames");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, "mapped-response-headers", "outboundHeaderNames");
			builder.addPropertyValue("headerMapper", headerMapperBuilder.getBeanDefinition());
		}
	}

	private String getInputChannelAttributeName() {
		return this.expectReply ? "request-channel" : "channel";
	}
}
