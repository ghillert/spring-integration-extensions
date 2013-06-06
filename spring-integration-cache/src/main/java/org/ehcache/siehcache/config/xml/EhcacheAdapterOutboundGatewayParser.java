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
package org.ehcache.siehcache.config.xml;

import org.ehcache.siehcache.outbound.EhcacheAdapterOutboundGateway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The Parser for EhcacheAdapter Outbound Gateway.
 *
 * @author Glenn Renfro
 * @since 1.0
 *
 */
public class EhcacheAdapterOutboundGatewayParser extends AbstractConsumerEndpointParser  {

	@Override
	protected BeanDefinitionBuilder parseHandler(Element gatewayElement, ParserContext parserContext) {

		final BeanDefinitionBuilder ehcacheadapterOutboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(EhcacheAdapterOutboundGateway.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(ehcacheadapterOutboundGatewayBuilder, gatewayElement, "reply-timeout", "sendTimeout");

		final String replyChannel = gatewayElement.getAttribute("reply-channel");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(ehcacheadapterOutboundGatewayBuilder, gatewayElement, "ehcache-xml","ehcacheXml");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(ehcacheadapterOutboundGatewayBuilder, gatewayElement, "cache");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(ehcacheadapterOutboundGatewayBuilder, gatewayElement, "cache-manager-name","cacheManagerName");


		if (StringUtils.hasText(replyChannel)) {
			ehcacheadapterOutboundGatewayBuilder.addPropertyReference("outputChannel", replyChannel);
		}

		final BeanDefinitionBuilder ehcacheadapterExecutorBuilder = EhcacheAdapterParserUtils.getEhcacheAdapterExecutorBuilder(gatewayElement, parserContext);

		final BeanDefinition ehcacheadapterExecutorBuilderBeanDefinition = ehcacheadapterExecutorBuilder.getBeanDefinition();
		final String gatewayId = this.resolveId(gatewayElement, ehcacheadapterOutboundGatewayBuilder.getRawBeanDefinition(), parserContext);
		final String ehcacheadapterExecutorBeanName = gatewayId + ".ehcacheadapterExecutor";

		parserContext.registerBeanComponent(new BeanComponentDefinition(ehcacheadapterExecutorBuilderBeanDefinition, ehcacheadapterExecutorBeanName));

		ehcacheadapterOutboundGatewayBuilder.addConstructorArgReference(ehcacheadapterExecutorBeanName);
		return ehcacheadapterOutboundGatewayBuilder;

	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}

}
