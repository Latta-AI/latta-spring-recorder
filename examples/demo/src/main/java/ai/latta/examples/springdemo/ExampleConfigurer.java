package ai.latta.examples.springdemo;

import ai.latta.spring.interceptors.LattaInterceptor;
import ai.latta.spring.filters.LattaResponseFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class ExampleConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LattaInterceptor("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9qZWN0IjoiMWE3NTUyOWItZjUwNi00Yjk5LWI1ZTMtN2Y5ZjY5YzRmZDhmIiwiaWF0IjoxNzI3MjUzNjk5fQ.FejGfhY_Eqgkafd9_GvQrecvV8UuEk5gcT2s0XmPUDU")).addPathPatterns("/**");;
    }

    @Bean
    public FilterRegistrationBean<LattaResponseFilter> applyLattaResponseFilter() {
        FilterRegistrationBean<LattaResponseFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LattaResponseFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all requests
        return registrationBean;
    }
}
