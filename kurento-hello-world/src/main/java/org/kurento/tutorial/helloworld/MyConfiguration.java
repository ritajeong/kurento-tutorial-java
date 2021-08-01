package org.kurento.tutorial.helloworld;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    
    	// js, css, img파일들에 대해서 dist와 static 파일을 참조한다.
        registry.addResourceHandler(
        		"/js/**",
                "/css/**",
                "/img/**"
                )
		        .addResourceLocations(
		                "classpath:/dist/js/",
		                "classpath:/dist/css/",
		                "classpath:/dist/img/",
		                "classpath:/static/js/",
		                "classpath:/static/css/",
		                "classpath:/static/img/"
		        )
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES));
    }
}
