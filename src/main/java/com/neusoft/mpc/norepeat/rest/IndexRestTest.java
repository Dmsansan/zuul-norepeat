package com.neusoft.mpc.norepeat.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author siss
 * @date 2020/9/14  9:14
 */
@RestController
@RequestMapping(value = "/index")
public class IndexRestTest {

    @GetMapping(value = "/test")
    public String index(){
        return "request success";
    }
}
