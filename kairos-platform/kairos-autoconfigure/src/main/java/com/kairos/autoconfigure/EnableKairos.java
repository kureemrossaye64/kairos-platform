package com.kairos.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.kairos.autoconfigure.properties.KairosProperties;

/**
 * Marks the main application class for KAIROS Platform.
 * <p>
 * This annotation combines Spring Boot's auto-configuration with KAIROS-specific
 * defaults and component scanning. It enables:
 * <ul>
 *   <li>Auto-configuration of AI/LLM components (if dependencies present)</li>
 *   <li>Auto-configuration of Vector Stores (in-memory for dev, PgVector for prod)</li>
 *   <li>Security configuration (JWT)</li>
 *   <li>Component scanning for @KairosTool annotated classes</li>
 * </ul>
 * 
 * Example usage:
 * <pre>
 * &#64;EnableKairos
 * public class MyComplianceApp {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyComplianceApp.class, args);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication
@Import(KairosAutoConfiguration.class)
@EnableConfigurationProperties(KairosProperties.class)
public @interface EnableKairos {

    /**
     * Alias for {@link SpringBootApplication#scanBasePackages}.
     * Automatically includes "com.kairos" for framework components.
     */
    @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackages")
    String[] scanBasePackages() default {};

    /**
     * Exclude specific KAIROS features from auto-configuration.
     */
    @AliasFor(annotation = SpringBootApplication.class, attribute = "exclude")
    Class<?>[] exclude() default {};
    
}