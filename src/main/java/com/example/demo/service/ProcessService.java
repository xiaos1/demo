package com.example.demo.service;

import com.example.demo.bean.StorageObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/4/24 5:47 PM
 */
public interface ProcessService {
    List<StorageObject> getDataSource(String dateA, String dateB, File confFile, File logFile) throws IOException, ParseException;

    void processData(List<StorageObject> dataA, List<StorageObject> dataB);
}
