package com.example.demo.controller;

import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/1 3:51 PM
 */
public abstract class BaseCtrl {
    /**
     * print json output
     *
     * @param response
     * @param object
     * @throws IOException
     */
    protected void responseJson(HttpServletResponse response, int code, String msg, Object object) throws IOException {
        response.setContentType("application/json;charset=utf8");
        response.getWriter().print(JSON.toJSONString(
                new HashMap<String, Object>() {{
                    put("code", code);
                    put("msg", msg);
                    put("result", object);
                }}
        ));
    }
}
