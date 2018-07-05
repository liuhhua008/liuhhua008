package com.liu.springbootliu.utils;

public enum ResultStatusCode {
    OK(0,"OK"),
    SYSTEM_ERR(30001,"System error"),
    PERMISSION_DENIED(40001,"permission_denied"),
    INVALID_CLIENTID(50001,"invalid_clientid"),
    INVALID_PASSWORD(50002,"invalid_password"),
    INVALID_TOKEN(60001,"invalid_token"),
    EXPIRES_TOKEN(60002,"expires_token"),
    USERCODE_ERR(70002,"userCode is not compareCode"),
    USERALREADY_REGISTERED(70001,"Already registered"),
    UPFILE_SCUESS(80000,"上传成功!"),
    UPFILE_ERR(80001,"upfile error"),
    UPFILE_TYPEERR(80002,"上传的文件不是图片类型，请重新上传!"),
    UPFILE_NOFILE(80003,"上传失败，请选择要上传的图片!");

    private int errcode;
    private String errmsg;
    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    private ResultStatusCode(int Errode, String ErrMsg)
    {
        this.errcode = Errode;
        this.errmsg = ErrMsg;
    }
}
