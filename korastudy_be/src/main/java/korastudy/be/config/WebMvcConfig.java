package korastudy.be.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:3030", "http://localhost:8080", "http://localhost:3000",
                "https://witty-water-06494e100.6.azurestaticapps.net" //Trang server của user
        ).allowedMethods("*").allowedHeaders("*").allowCredentials(true);
    }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> customCharacterEncodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> filterRegBean = new FilterRegistrationBean<>();
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);

        filterRegBean.setFilter(filter);
        filterRegBean.addUrlPatterns("/*");
        filterRegBean.setOrder(1);

        return filterRegBean;
    }
}