package org.dromara.ecology.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 泛微审批中心配置。 */
@Configuration
@EnableConfigurationProperties(EcologyProperties.class)
public class EcologyConfiguration {
}
