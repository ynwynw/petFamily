package com.example.petback.controller.email;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/")
public class WebTest {
    @GetMapping("/")
    public String hello(){
        return "hello";
    }
}
