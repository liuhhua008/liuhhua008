package com.liu.springbootliu.jwt;

/**
 * 用户登录时获取TOKEN所需要验证的内容
 */
public class LoginPara {
    private String clientId;
    private String userName;
    private String passWord;
    private String captchaCode;
    private String captchaValue;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getCaptchaValue() {
        return captchaValue;
    }

    public void setCaptchaValue(String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
