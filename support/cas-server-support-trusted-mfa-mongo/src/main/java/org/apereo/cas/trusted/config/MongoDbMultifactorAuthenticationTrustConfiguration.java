package org.apereo.cas.trusted.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MongoDbMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "mongo")
@Configuration(value = "MongoDbMultifactorAuthenticationTrustConfiguration", proxyBeanMethods = false)
public class MongoDbMultifactorAuthenticationTrustConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceMfaTrustedAuthnExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "mongoMfaTrustedAuthnTemplate")
    public MongoOperations mongoMfaTrustedAuthnTemplate(final CasConfigurationProperties casProperties,
                                                        @Qualifier(CasSSLContext.BEAN_NAME)
                                                      final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getMfa().getTrusted().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoMfaTrustedAuthnTemplate")
        final MongoOperations mongoMfaTrustedAuthnTemplate,
        @Qualifier("mfaTrustRecordKeyGenerator")
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
        @Qualifier("mfaTrustCipherExecutor")
        final CipherExecutor mfaTrustCipherExecutor) {
        return new MongoDbMultifactorAuthenticationTrustStorage(
            casProperties.getAuthn().getMfa().getTrusted(), mfaTrustCipherExecutor,
            mongoMfaTrustedAuthnTemplate, keyGenerationStrategy);
    }
}
