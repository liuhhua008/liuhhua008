package com.liu.springbootliu;

import com.liu.springbootliu.redis.User;
import com.liu.springbootliu.redis.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootLiuApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @Test
    public void contextLoads() {
    }
    @Test
    //直接使用redisTemplate存取字符串
    public void setAndGet() {
        redisTemplate.opsForValue().set("test:set", "testValue1");
        Assert.assertEquals("testValue1", redisTemplate.opsForValue().get("test:set"));
    }

    //直接使用redisTemplate存取对象
    @Test
    public void setAndGetUser(){
        User user=new User("Tom",10);
        redisTemplate.opsForValue().set("abc:setUser",user);
        Assert.assertEquals(user.getUsername(), ((User) redisTemplate.opsForValue().get("abc:setUser")).getUsername());
    }

    @Test
    //使用Redis缓存对象，getUser只会被调用一次
    public void testCache() {
        User user;
        user = userService.getUser("Same");
        user = userService.getUser("Same");
        user = userService.getUser("Same");

    }
}
