package com.liu.springbootliu.bean;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserInfoRepository extends CrudRepository<UserInfo,Integer> {
    UserInfo findUserInfoById(int id);
    List<UserInfo> findUserInfoByRole(String role);

    @Query(value="select * from t_user limit ?1",nativeQuery = true)
    List<UserInfo> findAllUserByCount(int count);

    UserInfo findUserInfoByName(String userName);
}
