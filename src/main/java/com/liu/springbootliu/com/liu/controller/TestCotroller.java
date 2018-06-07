package com.liu.springbootliu.com.liu.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class TestCotroller {
    @RequestMapping("test")
    public String test(){
        return "hello it's test";
    }
}
