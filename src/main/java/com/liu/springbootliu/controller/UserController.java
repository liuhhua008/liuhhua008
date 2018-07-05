package com.liu.springbootliu.controller;

import com.liu.springbootliu.bean.UserInfo;
import com.liu.springbootliu.bean.UserInfoRepository;
import com.liu.springbootliu.jwt.Audience;
import com.liu.springbootliu.utils.ResultMsg;
import com.liu.springbootliu.utils.ResultStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private Audience audience;
    @Autowired
    private UserInfoRepository userRepositoy;

    @RequestMapping("getaudience")
    public Object getAudience() {
        ResultMsg resultMsg=new ResultMsg(ResultStatusCode.OK.getErrcode(),ResultStatusCode.OK.getErrmsg(),audience);
        return resultMsg;
    }

   @RequestMapping("getuser")
    public Object getUser(String id){
       UserInfo userEntity = userRepositoy.findUserInfoByUid(id);
       ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(), ResultStatusCode.OK.getErrmsg(), userEntity);
       return resultMsg;
   }
    @RequestMapping("getusers")
    public Object getUsers(String role)
    {
        List<UserInfo> userEntities = userRepositoy.findUserInfoByRole(role);
        ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(), ResultStatusCode.OK.getErrmsg(), userEntities);
        return resultMsg;
    }

    @Modifying
    @RequestMapping("adduser")
    public Object addUser(@RequestBody UserInfo userEntity)
    {
        userRepositoy.save(userEntity);
        ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(), ResultStatusCode.OK.getErrmsg(), userEntity);
        return resultMsg;
    }

    @Modifying
    @RequestMapping("updateuser")
    public Object updateUser(@RequestBody UserInfo userEntity)
    {
        UserInfo user = userRepositoy.findUserInfoByUid(userEntity.getUid());
        if (user != null)
        {
            user.setName(userEntity.getName());
            userRepositoy.save(user);
        }
        ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(), ResultStatusCode.OK.getErrmsg(), null);
        return resultMsg;
    }

    @Modifying
    @RequestMapping("deleteuser")
    public Object deleteUser(int id)
    {
        userRepositoy.deleteById(id);
        ResultMsg resultMsg = new ResultMsg(ResultStatusCode.OK.getErrcode(), ResultStatusCode.OK.getErrmsg(), null);
        return resultMsg;
    }



}
