package com.example.demo.dao;

import com.example.demo.bean.ReturningData;
import com.example.demo.entity.CpuStats;
import com.example.demo.entity.PhyResource;

import java.util.List;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 8:19 PM
 */
public interface CollectDao {
    List<PhyResource> fetchResource();

    void insertCpuStats(List<CpuStats> cpuStats);

    List<ReturningData> queryCpuStats();
}
