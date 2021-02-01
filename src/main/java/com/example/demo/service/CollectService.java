package com.example.demo.service;

import java.io.IOException;
import java.util.Map;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 8:31 PM
 */
public interface CollectService {

    Map<String, Double> getSummary() throws IOException;
}
