package id.co.awan.walle;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource

@TestConfiguration
public class RestTemplateTestConfiguration {

    @Bean
    fun restTemplateBuilder() = RestTemplateBuilder()

    @Bean
    fun yamlPropertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
        val configurer = PropertySourcesPlaceholderConfigurer()
        val yaml = YamlPropertiesFactoryBean()
        yaml.setResources(ClassPathResource("application.yml"))
        configurer.setProperties(yaml.getObject()!!)
        return configurer
    }

}
