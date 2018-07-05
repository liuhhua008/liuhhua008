package com.liu.springbootliu.jwt;

import com.liu.springbootliu.redis.CacheUtils;
import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * jwt构造器和jwt解析
 */
public class JwtHelper {
     public static class ErrorCodeException extends Exception{};
    //传入token字串，解密定义字串。将此token解析返回得到jwt的Claims对象
    public static Claims parseJWT(String jsonWebToken,String base64Security) throws ErrorCodeException {
        try{
            Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Security)).parseClaimsJws(jsonWebToken).getBody();
            //如果能正常解析并且是因为userCode和redis中存储的值不一样，就报出异常。
            if (!(CacheUtils.compareCode((String) claims.get("userid"),(String) claims.get("userCode")))&&(claims!=null)){
                throw new ErrorCodeException();
              }
            return claims;
        }catch (Exception ex){
           // ex.printStackTrace();//如果判断异常类型为JWT超时异常，则抛出异常。
            if(ex instanceof ExpiredJwtException){
                throw (ExpiredJwtException)ex;
            }else if (ex instanceof ErrorCodeException){
                throw (ErrorCodeException)ex;
            }else {
                return null;
            }

        }
    }

    /**
     * 通过传入下列的字串值生成JWT token
     * @param name 用户名
     * @param userId 用户ID
     * @param role  用户权限
     * @param audience token的受众标识
     * @param issuer  tokenr的发行者
     * @param TTLMillis  过期时长
     * @param base64Security 加密码token的密钥
     * @return  返回一个jwt token字符串
     */
    public static String createJWT(String name,String userId,String role,String audience,String issuer,long TTLMillis,String base64Security,String userCode){
        //指定JWT头部加密类型
        SignatureAlgorithm signatureAlgorithm=SignatureAlgorithm.HS256;
        long nowMillis =System.currentTimeMillis();
        Date now=new Date(nowMillis);
        //生成签名密钥
        byte[] apiKeySecretBytes =DatatypeConverter.parseBase64Binary(base64Security);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes,signatureAlgorithm.getJcaName());
        //在生成JWT前向redis中存入一个userCode
       // CacheUtils.setCode(userId,""+Math.random());
        //添加构成JWT的参数
        JwtBuilder builder= Jwts.builder().setHeaderParam("type","JWT")
                                         .claim("role",role)
                                         .claim("unique_name",name)
                                         .claim("userid",userId)
                                         .claim("userCode",userCode)//搞个随机值,可以让每次生成的JWT都不一样，同时还可以做注销判断
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
