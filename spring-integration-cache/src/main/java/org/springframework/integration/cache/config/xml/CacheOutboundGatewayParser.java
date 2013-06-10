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
package org.springframework.integration.cache.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.cache.outbound.CacheOutboundGateway;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The Parser for Cache Outbound Gateway.
 *
 * @author Glenn Renfro
 * @since 1.0
 *
 */
public class CacheOutboundGatewayParser extends AbstractConsumerEndpointParser  {

	@Override
	protected BeanDefinitionBuilder parseHandler(Element gatewayElement, ParserContext parserContext) {

		final BeanDefinitionBuilder cacheOutboundGatewayBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CacheOutboundGateway.class);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(cacheOutboundGatewayBuilder, gatewayElement, "reply-timeout", "sendTimeout");

		final String replyChannel = gatewayElement.getAttribute("reply-channel");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(cacheOutboundGatewayBuilder, gatewayElement, "cache-name", "cacheName");

		if (StringUtils.hasText(replyChannel)) {
			cacheOutboundGatewayBuilder.addPropertyReference("outputChannel", replyChannel);
		}

		final BeanDefinitionBuilder cacheExecutorBuilder = CacheOutboundGatewayParserUtils.getCacheExecutorBuilder(gatewayElement, parserContext);
		final BeanDefinition cacheExecutorBuilderBeanDefinition = cacheExecutorBuilder.getBeanDefinition();
		final String gatewayId = this.resolveId(gatewayElement, cacheOutboundGatewayBuilder.getRawBeanDefinition(), parserContext);
		final String cacheExecutorBeanName = gatewayId + ".cacheExecutor";

		parserContext.registerBeanComponent(new BeanComponentDefinition(cacheExecutorBuilderBeanDefinition, cacheExecutorBeanName));

		cacheOutboundGatewayBuilder.addConstructorArgReference(cacheExecutorBeanName);
		registerManager(gatewayElement,cacheExecutorBuilder);
		return cacheOutboundGatewayBuilder;

	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}

	protected void registerManager(Element element, BeanDefinitionBuilder builder){
		String dataSourceRef = element.getAttribute("cache-manager-ref");
		builder.addPropertyReference("cacheManager", dataSourceRef);
	}


}
