package com.example.demo.controller;

import com.example.demo.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/28 4:37 PM
 */
@RequestMapping(value = "/stats")
@RestController
public class DemoController {

    @Autowired
    CollectService collectService;

    @ResponseBody
    @RequestMapping(value = "/reload", method = RequestMethod.GET, produces = "application/json")
    public String reload() throws IOException {
        double[] summary = collectService.getSummary();
        return "[BE, batch, stable] = " + Arrays.toString(summary);
    }
}
