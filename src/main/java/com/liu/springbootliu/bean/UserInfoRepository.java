package com.liu.springbootliu.bean;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserInfoRepository extends CrudRepository<UserInfo,Integer> {

    List<UserInfo> findUserInfoByRole(String role);

    @Query(value="select * from t_user limit ?1",nativeQuery = true)
    List<UserInfo> findAllUserByCount(int count);

    UserInfo findUserInfoByName(String userName);

    UserInfo findUserInfoByUid(String uid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update t_user set user_head=?1 where uid=?2",nativeQuery = true)
    public void upDateHead(String fiePath,String uid);
}
