package com.liu.springbootliu;

import com.liu.springbootliu.filter.HTTPBasicAuthorizeAttribute;
import com.liu.springbootliu.filter.HTTPBearerAuthorizeAttribute;
import com.liu.springbootliu.jwt.Audience;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
@EnableCaching
//@EnableConfigurationProperties(Audience.class)
public class SpringBootLiuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLiuApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean basicFilterRegistrationBean(){
        FilterRegistrationBean registrationBean =new FilterRegistrationBean();
        HTTPBasicAuthorizeAttribute httpBasicFilter = new HTTPBasicAuthorizeAttribute();
        registrationBean.setFilter(httpBasicFilter);
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/user/getuser");
        registrationBean.setUrlPatterns(urlPatterns);
        return registrationBean;
    }

    /**
     * 注册jwt-token验证的过滤器，并定义了哪些请求需要验证
     * @return
     */
    @Bean
    public FilterRegistrationBean jwtFilterRegistrationBean(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        HTTPBearerAuthorizeAttribute httpBearerFilter= new HTTPBearerAuthorizeAttribute();
        registrationBean.setFilter(httpBearerFilter);
        List<String> urlPatterns=new ArrayList<String>();
        urlPatterns.add("/user/getusers");
        registrationBean.setUrlPatterns(urlPatterns);
        return registrationBean;
    }
}
