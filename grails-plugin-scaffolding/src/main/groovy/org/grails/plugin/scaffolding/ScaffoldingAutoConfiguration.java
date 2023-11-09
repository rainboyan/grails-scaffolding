package org.grails.plugin.scaffolding;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import grails.config.Config;
import grails.config.Settings;
import grails.core.GrailsApplication;
import grails.plugin.scaffolding.ScaffoldingViewResolver;
import grails.util.Environment;
import org.grails.gsp.GroovyPagesTemplateEngine;
import org.grails.web.gsp.io.GrailsConventionGroovyPageLocator;
import org.grails.web.servlet.view.GroovyPageViewResolver;
import org.grails.web.util.GrailsApplicationAttributes;

@AutoConfiguration
@AutoConfigureOrder
public class ScaffoldingAutoConfiguration {

    private static final String GSP_RELOAD_INTERVAL = "grails.gsp.reload.interval";

    @Bean
    @Order(-10)
    @ConditionalOnMissingBean
    public ScaffoldingViewResolver jspViewResolver(ObjectProvider<GrailsApplication> grailsApplication,
            ObjectProvider<GroovyPagesTemplateEngine> groovyPagesTemplateEngine,
            ObjectProvider<GrailsConventionGroovyPageLocator> groovyPageLocator) {

        Config config = grailsApplication.getIfAvailable().getConfig();
        Environment env = Environment.getCurrent();
        boolean developmentMode = Environment.isDevelopmentEnvironmentAvailable();
        boolean gspEnableReload = config.getProperty(Settings.GSP_ENABLE_RELOAD, Boolean.class, false);
        boolean enableReload = env.isReloadEnabled() || gspEnableReload || (developmentMode && env == Environment.DEVELOPMENT);
        long gspCacheTimeout = config.getProperty(GSP_RELOAD_INTERVAL, Long.class, (developmentMode && env == Environment.DEVELOPMENT) ? 0L : 5000L);

        boolean jstlPresent = ClassUtils.isPresent("javax.servlet.jsp.jstl.core.Config", Thread.currentThread().getContextClassLoader());

        final ScaffoldingViewResolver scaffoldingViewResolver = new ScaffoldingViewResolver();
        scaffoldingViewResolver.setPrefix(GrailsApplicationAttributes.PATH_TO_VIEWS);

        if (jstlPresent) {
            scaffoldingViewResolver.setSuffix(GroovyPageViewResolver.JSP_SUFFIX);
        }
        else {
            scaffoldingViewResolver.setSuffix(GroovyPageViewResolver.GSP_SUFFIX);
        }
        groovyPagesTemplateEngine.ifAvailable(scaffoldingViewResolver::setTemplateEngine);
        groovyPageLocator.ifAvailable(scaffoldingViewResolver::setGroovyPageLocator);

        if (enableReload) {
            scaffoldingViewResolver.setCacheTimeout(gspCacheTimeout);
        }
        scaffoldingViewResolver.setOrder(Ordered.LOWEST_PRECEDENCE - 100);
        return scaffoldingViewResolver;
    }

}
