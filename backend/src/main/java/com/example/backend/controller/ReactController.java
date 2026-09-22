package com.example.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactController {

    @RequestMapping(value = "/GPF/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/GPF/index.html";
    }
}