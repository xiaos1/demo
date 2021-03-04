package com.example.demo.service;

import com.example.demo.bean.ResourceClassification;
import com.example.demo.bean.ReturningData;

import java.util.List;
import java.util.Map;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 8:31 PM
 */
public interface CollectService {

    Map<String, ResourceClassification> doCollectData();

    List<ReturningData> doGetData(String date);

    void doRecordData();
}
