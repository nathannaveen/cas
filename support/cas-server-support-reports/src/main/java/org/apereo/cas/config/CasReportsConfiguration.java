package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.report.AuditLogEndpoint;
import org.apereo.cas.web.report.CasInfoEndpointContributor;
import org.apereo.cas.web.report.CasReleaseAttributesReportEndpoint;
import org.apereo.cas.web.report.CasResolveAttributesReportEndpoint;
import org.apereo.cas.web.report.CasRuntimeModulesEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationHandlersEndpoint;
import org.apereo.cas.web.report.RegisteredAuthenticationPoliciesEndpoint;
import org.apereo.cas.web.report.RegisteredServicesEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionStatusEndpoint;
import org.apereo.cas.web.report.SingleSignOnSessionsEndpoint;
import org.apereo.cas.web.report.SpringWebflowEndpoint;
import org.apereo.cas.web.report.StatisticsEndpoint;
import org.apereo.cas.web.report.StatusEndpoint;
import org.apereo.cas.web.report.TicketExpirationPoliciesEndpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceEndpoint;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This this {@link CasReportsConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Configuration(value = "CasReportsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Reports)
public class CasReportsConfiguration {

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SpringWebflowEndpoint springWebflowEndpoint(final CasConfigurationProperties casProperties,
                                                       final ConfigurableApplicationContext applicationContext) {
        return new SpringWebflowEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditLogEndpoint auditLogEndpoint(
        @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
        final ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new AuditLogEndpoint(auditTrailExecutionPlan, casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRuntimeModulesEndpoint casRuntimeModulesEndpoint(
        @Qualifier("casRuntimeModuleLoader")
        final ObjectProvider<CasRuntimeModuleLoader> casRuntimeModuleLoader,
        final CasConfigurationProperties casProperties) {
        return new CasRuntimeModulesEndpoint(casProperties, casRuntimeModuleLoader);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RegisteredServicesEndpoint registeredServicesReportEndpoint(
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new RegisteredServicesEndpoint(casProperties, servicesManager,
            webApplicationServiceFactory,
            CollectionUtils.wrapList(new RegisteredServiceYamlSerializer(), new RegisteredServiceJsonSerializer()));
    }


    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RegisteredAuthenticationHandlersEndpoint registeredAuthenticationHandlersEndpoint(
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new RegisteredAuthenticationHandlersEndpoint(casProperties, authenticationEventExecutionPlan);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RegisteredAuthenticationPoliciesEndpoint registeredAuthenticationPoliciesEndpoint(
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan,
        final CasConfigurationProperties casProperties) {
        return new RegisteredAuthenticationPoliciesEndpoint(casProperties, authenticationEventExecutionPlan);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casInfoEndpointContributor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasInfoEndpointContributor casInfoEndpointContributor(
        @Qualifier("casRuntimeModuleLoader")
        final CasRuntimeModuleLoader casRuntimeModuleLoader) {
        return new CasInfoEndpointContributor(casRuntimeModuleLoader);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final ObjectProvider<CentralAuthenticationService> centralAuthenticationService,
        @Qualifier("defaultSingleLogoutRequestExecutor")
        final ObjectProvider<SingleLogoutRequestExecutor> defaultSingleLogoutRequestExecutor,
        final CasConfigurationProperties casProperties) {
        return new SingleSignOnSessionsEndpoint(centralAuthenticationService,
            casProperties, defaultSingleLogoutRequestExecutor);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint(
        @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return new SingleSignOnSessionStatusEndpoint(ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public StatisticsEndpoint statisticsReportEndpoint(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final ObjectProvider<CentralAuthenticationService> centralAuthenticationService,
        final CasConfigurationProperties casProperties) {
        return new StatisticsEndpoint(centralAuthenticationService, casProperties);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasResolveAttributesReportEndpoint resolveAttributesReportEndpoint(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
        final CasConfigurationProperties casProperties) {
        return new CasResolveAttributesReportEndpoint(casProperties, defaultPrincipalResolver);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint
    public TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint(
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        final List<ExpirationPolicyBuilder> builders) {
        return new TicketExpirationPoliciesEndpoint(casProperties, builders,
            servicesManager, webApplicationServiceFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint(endpoint = HttpTraceEndpoint.class)
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasReleaseAttributesReportEndpoint releaseAttributesReportEndpoint(
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("principalFactory")
        final PrincipalFactory principalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new CasReleaseAttributesReportEndpoint(casProperties,
            servicesManager, authenticationSystemSupport,
            webApplicationServiceFactory, principalFactory);
    }

    /**
     * This this {@link StatusEndpointConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 6.0.0
     * @deprecated since 6.2.0
     */
    @Configuration(value = "StatusEndpointConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Slf4j
    @Deprecated(since = "6.2.0")
    public static class StatusEndpointConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public StatusEndpoint statusEndpoint(
            final ObjectProvider<HealthEndpoint> healthEndpoint,
            final CasConfigurationProperties casProperties) {
            return new StatusEndpoint(casProperties, healthEndpoint);
        }

        @Bean
        @ConditionalOnAvailableEndpoint(endpoint = StatusEndpoint.class)
        public InitializingBean statusEndpointInitializer() {
            return () ->
                LOGGER.warn("The status actuator endpoint is deprecated and is scheduled to be removed from CAS in the future. "
                            + "To obtain status and health information, please configure and use the health endpoint instead.");
        }
    }
}
