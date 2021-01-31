package com.example.demo.dao;

import com.example.demo.entity.Resource;

import java.util.List;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 8:19 PM
 */
public interface CollectDao {
    List<Resource> fetchResource();
}
