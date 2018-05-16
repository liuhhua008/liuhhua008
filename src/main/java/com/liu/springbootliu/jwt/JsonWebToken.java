package com.liu.springbootliu.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootliu.bean.UserInfo;
import com.liu.springbootliu.bean.UserInfoRepository;
import com.liu.springbootliu.utils.MyUtils;
import com.liu.springbootliu.utils.ResultMsg;
import com.liu.springbootliu.utils.ResultStatusCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 获取token的接口，通过传入用户认证信息(用户名、密码)进行认证获取token的类
 */
@RestController
public class JsonWebToken {
    @Autowired
    private UserInfoRepository userRepositoy;
    @Autowired
    private Audience audienceEntity;

    /**
     * 用户登录验证返回token
     * @param loginPara
     * @return 如果用户密码无误，返回ResultMsg对象。其中最后一个属性为AccessToken对像，包含：
     *     String access_token;//jwt值
     *     String token_type;//token的类型
     *     long expires_in;//过期时长
     */
    @RequestMapping("oauth/token")
    public Object getAccessToken(@RequestBody LoginPara loginPara){
        ResultMsg resultMsg;
        try {
            //判断CLIENTID是否错误
            if (loginPara.getClientId()==null || (loginPara.getClientId().compareTo(audienceEntity.getClientId())!=0)){
                resultMsg = new ResultMsg(ResultStatusCode.INVALID_CLIENTID.getErrcode(),ResultStatusCode.INVALID_CLIENTID.getErrmsg(),null);
                return resultMsg;
            }

            //验证用户名密码
            UserInfo user = userRepositoy.findUserInfoByName(loginPara.getUserName());
            if (user == null)
            {
                resultMsg = new ResultMsg(ResultStatusCode.INVALID_PASSWORD.getErrcode(),
                        ResultStatusCode.INVALID_PASSWORD.getErrmsg(), null);
                return resultMsg;
            }
            else
            {
                String md5Password = MyUtils.getMD5(loginPara.getPassWord()+user.getSalt());

                if (md5Password.compareTo(user.getPassword()) != 0)
                {
                    resultMsg = new ResultMsg(ResultStatusCode.INVALID_PASSWORD.getErrcode(),
                            ResultStatusCode.INVALID_PASSWORD.getErrmsg(), null);
                    return resultMsg;
                }

            }
            //生成双token返回对象
            AccessToken accessTokenEntity= getToken(loginPara.getUserName(), String.valueOf(user.getId()),
                    user.getRole());
            resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(),
                    ResultStatusCode.OK.getErrmsg(), accessTokenEntity);
            return resultMsg;
        }catch (Exception ex){
            resultMsg = new ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),ResultStatusCode.SYSTEM_ERR.getErrmsg(),null);
            return resultMsg;
        }
    }

    /**
     * 生成包含双token的AccessToken对象
     * @param userName
     * @param userId
     * @param userRole
     * @return
     */
    private AccessToken getToken(String userName,String userId,String userRole) {
        //拼装accessToken 传入
        String accessToken = JwtHelper.createJWT(userName, userId,
                userRole, audienceEntity.getClientId(), audienceEntity.getName(),
                audienceEntity.getExpiresSecond() * 1000, audienceEntity.getBase64Secret());
        //拼装refreshToken

        String refreshToken=JwtHelper.createJWT(userName, userId,
                userRole, audienceEntity.getClientId(), audienceEntity.getName(),
                audienceEntity.getRefreshSecond() * 1000, audienceEntity.getBase64Secret());
        //返回accessToken
        AccessToken accessTokenEntity = new AccessToken();
        //设置jwt字符串到accessTokenEntity
        accessTokenEntity.setAccess_token(accessToken);
        //设置jwt字符串到refreshTokenEntity
        accessTokenEntity.setRefresh_token(refreshToken);
        //从audience配置中获取过期时长并赋值给accessTokenEntity
        accessTokenEntity.setExpires_in(audienceEntity.getExpiresSecond());
        //设置authorization的验证类型为"bearer"
        accessTokenEntity.setToken_type("bearer");
        return accessTokenEntity;
    }

    /**
     * 用户注册
     * @param userPara
     * @return
     */
    @RequestMapping("oauth/register")
    public Object register(@RequestBody LoginPara userPara) {
        ResultMsg resultMsg;
        try {
            //判断CLIENTID是否错误
            if (userPara.getClientId() == null || (userPara.getClientId().compareTo(audienceEntity.getClientId()) != 0)) {
                resultMsg = new ResultMsg(ResultStatusCode.INVALID_CLIENTID.getErrcode(), ResultStatusCode.INVALID_CLIENTID.getErrmsg(), null);
                return resultMsg;
            }

            //验证用户名密码
            UserInfo user = userRepositoy.findUserInfoByName(userPara.getUserName());
            if (user == null) {//用户名还没有被注册
                //1.拿到用户名,并生成加盐密钥，再根据加盐密钥生成密码，然后调用数据库插入数据
                UserInfo userInfo = new UserInfo();
                userInfo.setName(userPara.getUserName());
                //生成随机加盐字符串
                Random random = new SecureRandom();
                byte[] slat = new byte[16];
                random.nextBytes(slat);
                String strSlat=new String(slat, "UTF-8");
                userInfo.setSalt(strSlat);
                String md5Password = MyUtils.getMD5(userPara.getPassWord()+strSlat);
                userInfo.setPassword(md5Password);
                userRepositoy.save(userInfo);
                return  new ResultMsg(ResultStatusCode.OK.getErrcode(),ResultStatusCode.OK.getErrmsg(),null);
            }else{
                return new ResultMsg(ResultStatusCode.USERALREADY_REGISTERED.getErrcode(),ResultStatusCode.USERALREADY_REGISTERED.getErrmsg(),null);
            }
        } catch (Exception ex) {
            resultMsg=new ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),ResultStatusCode.SYSTEM_ERR.getErrmsg(),null);
            return resultMsg;
        }
    }

    /**
     * jwt Token自动刷新，使用RefreshToken来获取新的jwt-Token，如果它也有问题或是过期了，
     * 那就返回错误信息要求用户重新登录。
     * @return
     */
    @RequestMapping("oauth/refreshToken")
    public Object refreshAccessToken(@RequestHeader("Authorization") String authorization ,@RequestBody String clientId,HttpServletResponse httpResponse){

            //判断CLIENTID是否错误
            if (clientId==null || (clientId.compareTo(audienceEntity.getClientId())!=0)){
                return "error clientId";
            }
            if (authorization!=null) {
                String HeadStr = authorization.substring(0, 6).toLowerCase();
                //之前在生成token entity 通过json http body返回给客户端的时候带了一个Token_type的属性值就是"bearer"
                if (HeadStr.compareTo("bearer") == 0) {
                    authorization = authorization.substring(7, authorization.length());//拿到jwt字串
                    //对refresh-jwt字串进行解析得到Claims载体信息,这里并没有具体看里面到底有什么，只看能不能正常对验证签名拿到返回对象。
                    try {
                        if (JwtHelper.parseJWT(authorization, audienceEntity.getBase64Secret()) != null) {
                           //证明refresh-jwt没有问题，生成新的accessToken返回给客户端
                            Claims claims= JwtHelper.parseJWT(authorization, audienceEntity.getBase64Secret());
                            //把原来refresh-jwt中包含的信息用来生成新的token
                            AccessToken accessToken=getToken(claims.get("unique_name").toString(),claims.get("userid").toString(),claims.get("role").toString());
                            //封装结果消息返回给客户端
                            return new ResultMsg(ResultStatusCode.OK.getErrcode(),
                                    ResultStatusCode.OK.getErrmsg(), accessToken);
                        }
                    } catch (ExpiredJwtException ex) {
                        //如果refresh-jwt也过期，接住JWT的超时异常,返回Token超时信息
                        //HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                        httpResponse.setCharacterEncoding("UTF-8");
                        httpResponse.setContentType("application/json;charset=utf-8");
                        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        httpResponse.setHeader("Authorization", "expires");
                       // ObjectMapper mapper = new ObjectMapper();
                        ResultMsg resultMsg = new ResultMsg(ResultStatusCode.EXPIRES_TOKEN.getErrcode(), ResultStatusCode.EXPIRES_TOKEN.getErrmsg(), null);
                        try {
                            //httpResponse.getWriter().write(mapper.writeValueAsString(resultMsg));
                            return resultMsg;
                        }catch (Exception e){
                            return new ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),ResultStatusCode.SYSTEM_ERR.getErrmsg(),null);
                        }

                    }
                }
            }

        return new ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),ResultStatusCode.SYSTEM_ERR.getErrmsg(),null);
  }
}
