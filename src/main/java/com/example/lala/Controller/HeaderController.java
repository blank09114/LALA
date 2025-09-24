package com.example.lala.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HeaderController {

    @GetMapping("/header")
    public String header(){
        return "shared/header";
    }
}
