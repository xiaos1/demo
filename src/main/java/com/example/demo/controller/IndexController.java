package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
public class IndexController extends BaseCtrl {

    @GetMapping(value = "/")
    public void index(HttpServletResponse response) throws IOException {
        responseJson(response, 0, "success", "System Started.");
    }
}