package com.liu.springbootliu.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * jwt构造器和jwt解析
 */
public class JwtHelper {
    //传入token字串，解密定义字串。将此token解析返回得到jwt的Claims对象
    public static Claims parseJWT(String jsonWebToken,String base64Security){
        try{
            Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Security)).parseClaimsJws(jsonWebToken).getBody();
            return claims;
        }catch (Exception ex){
            return null;
        }
    }

    /**
     * 通过传入下列的字串值生成JWT token
     * @param name
     * @param userId
     * @param role
     * @param audience
     * @param issuer
     * @param TTLMillis
     * @param base64Security
     * @return
     */
    public static String createJWT(String name,String userId,String role,String audience,String issuer,long TTLMillis,String base64Security){
        //指定JWT头部加密类型
        SignatureAlgorithm signatureAlgorithm=SignatureAlgorithm.HS256;
        long nowMillis =System.currentTimeMillis();
        Date now=new Date(nowMillis);
        //生成签名密钥
        byte[] apiKeySecretBytes =DatatypeConverter.parseBase64Binary(base64Security);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes,signatureAlgorithm.getJcaName());

        //添加构成JWT的参数
        JwtBuilder builder= Jwts.builder().setHeaderParam("type","JWT")
                                         .claim("role",role)
                                         .claim("unique_name",name)
                                         .claim("userid",userId)
                                         .setIssuer(issuer)
                                         .setAudience(audience)
                                         .signWith(signatureAlgorithm,signingKey);//指定JWT尾部加密 头base64加密+载体base64加密 使用签名密钥进行用头部定义hs256加密
        //添加Token过期时间
        if (TTLMillis >= 0) {
            long expMillis = nowMillis + TTLMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp).setNotBefore(now);
        }
        //生成JWT
        return builder.compact();
    }

}
