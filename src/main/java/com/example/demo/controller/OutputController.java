package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/5/14 5:20 PM
 */
@RestController
@RequestMapping(value = "/output")
public class OutputController {

    @ResponseBody
    @RequestMapping(value = "/output", method = RequestMethod.POST, produces = "application/json")
    public void output(MultipartHttpServletRequest request, HttpServletResponse response){
        MultipartFile file1 = request.getFile("Filedata1");
        MultipartFile file2 = request.getFile("Filedata2");
    }
}
