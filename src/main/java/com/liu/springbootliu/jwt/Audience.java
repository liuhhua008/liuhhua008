package com.liu.springbootliu.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 从porperises配置文件中获取属性值。封装成一个对象.这里面的信息可以在客户端向服务器获取Token的时候用来验证
 * 是否是来自我们自己的客户端。base64Secret是用来生成和解密token的必要密钥
 */
@Component
@PropertySource("jwt/jwt.properties")
@ConfigurationProperties(prefix = "audience")
public class Audience {
    private String clientId;
    private String base64Secret;
    private String name;
    private int expiresSecond;
    private int refreshSecond;

    public int getRefreshSecond() {
        return refreshSecond;
    }

    public void setRefreshSecond(int refreshSecond) {
        this.refreshSecond = refreshSecond;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBase64Secret() {
        return base64Secret;
    }

    public void setBase64Secret(String base64Secret) {
        this.base64Secret = base64Secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExpiresSecond() {
        return expiresSecond;
    }

    public void setExpiresSecond(int expiresSecond) {
        this.expiresSecond = expiresSecond;
    }
}
