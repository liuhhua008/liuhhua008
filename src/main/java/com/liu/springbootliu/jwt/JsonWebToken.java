package com.liu.springbootliu.jwt;

import com.liu.springbootliu.bean.UserInfo;
import com.liu.springbootliu.bean.UserInfoRepository;
import com.liu.springbootliu.utils.MyUtils;
import com.liu.springbootliu.utils.ResultMsg;
import com.liu.springbootliu.utils.ResultStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            //拼装accessToken 传入
            String accessToken = JwtHelper.createJWT(loginPara.getUserName(), String.valueOf(user.getId()),
                    user.getRole(), audienceEntity.getClientId(), audienceEntity.getName(),
                    audienceEntity.getExpiresSecond() * 1000, audienceEntity.getBase64Secret());

            //返回accessToken
            AccessToken accessTokenEntity = new AccessToken();
            //设置jwt字符串到accessTokenEntity
            accessTokenEntity.setAccess_token(accessToken);
            //从audience配置中获取过期时长并赋值给accessTokenEntity
            accessTokenEntity.setExpires_in(audienceEntity.getExpiresSecond());
            //设置authorization的验证类型为"bearer"
            accessTokenEntity.setToken_type("bearer");
            resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(),
                    ResultStatusCode.OK.getErrmsg(), accessTokenEntity);
            return resultMsg;
        }catch (Exception ex){
            resultMsg = new ResultMsg(ResultStatusCode.SYSTEM_ERR.getErrcode(),ResultStatusCode.SYSTEM_ERR.getErrmsg(),null);
            return resultMsg;
        }
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

}
