package com.example.demo.dao.impl;

import com.example.demo.bean.ReturningData;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.CpuStats;
import com.example.demo.entity.PhyResource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 11:41 AM
 */
@Slf4j
@Repository
public class CollectDaoImpl implements CollectDao {

    @Autowired
    Dao dao;

    @Override
    public List<PhyResource> fetchResource() {
        return dao.query(PhyResource.class, Cnd.where("is_deleted", "=", 1));
    }

    @Override
    public void insertCpuStats(List<CpuStats> cpuStats) {
        dao.insert(cpuStats);
    }

    @Override
    public List<ReturningData> queryCpuStats() {
        Sql sql = Sqls.create("select cpu as total, resource_type, platform , date from t_cpu_stats");
        sql.setCallback((connection, rs, sql1) -> {
            Map<String, List<ReturningData>> daily_sale_map = new HashMap<>();
            List<ReturningData> list1 = new ArrayList<>();
            while (null != rs && rs.next()) {
                ReturningData returningData = new ReturningData();
                returningData.setTotal(rs.getDouble("total"));
                returningData.setPlatform(rs.getString("platform"));
                returningData.setResourceType(rs.getString("resource_type"));
                returningData.setDate(rs.getString("date"));
                list1.add(returningData);
            }
            return list1;
        });
        dao.execute(sql);
        return sql.getList(ReturningData.class);
    }
}
