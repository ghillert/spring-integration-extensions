Spring Integration Cache Outbound Gateway
=================================================

Welcome to the Spring Integration Caching Outbound Gateway . The purpose of this gateway is to access to JSR-107 caches and common caching  frameworks for your EIP based application.  

# STS issues to be aware of

* the ehcache gateway does not appear in the Spring Integration Graph.  

# FAQ


## More to come...

# Context

##Namespace and Schema Declaration
Your XML Namespace will need to have the cache namespace as follows:
xmlns:int-cache="http://www.springframework.org/schema/integration/cache"
You will also need to include the Springs Caching framework in your xml schema for example (Note In the example below I included integration cache as well)
http://www.springframework.org/schema/cache
http://www.springframework.org/schema/cache/spring-cache.xsd
http://www.springframework.org/schema/integration/cache
http://www.springframework.org/schema/integration/cache/spring-integration-cache.xsd

## Setting up the cache manager

You can use whatever cachemanager that is supported by Spring Caching framework.  Thus you can take advantage of JSR-107, Ehcache or the SimpleCacheManager.
<!-- **************** Simple Cache Manager Example ********************* -->
<bean id="simpleCacheManager" class="org.springframework.cache.support.SimpleCacheManager">
	<property name="caches">
		<set>
			<bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="default"/>
			<bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="mycache"/>
		</set>
	</property>
</bean>

<!-- **************** Ehcache Manager Example ********************* -->
	<bean id="cacheManager" class="org.springframework.cache.ehcache.EhCacheCacheManager" p:cache-manager-ref="ehcache"/>

<!-- Ehcache library setup -->
	<bean id="ehcache" class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean" p:shared="true" p:config-location="classpath:ehcache.xml"/>

## Setting up the Gateway
<int-cache:outbound-gateway
	id="defaultSettingGateway"
	cache-name="mycache" auto-startup="true" order="1" request-channel="getDataOutputChannel" cache-manager-ref="cacheManager"
	reply-channel="endChannel" reply-timeout="100" />

