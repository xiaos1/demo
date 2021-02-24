package com.example.demo.dao.impl;

import com.example.demo.bean.ReturningData;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.CpuStats;
import com.example.demo.entity.PhyResource;
import com.example.demo.entity.UserQueue;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Sql sql = Sqls.create("select distinct cluster_name, physical_queue, platform, is_qianxun  from t_global_cluster_quota_resource");
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(PhyResource.class));
        dao.execute(sql);
        return sql.getList(PhyResource.class);
    }

    @Override
    public List<UserQueue> fetchQueue() {
        Sql sql = Sqls.create("select queue_name, cluster_name, resource_type, physical_queue from t_user_queue");
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(UserQueue.class));
        dao.execute(sql);
        return sql.getList(UserQueue.class);
    }

    @Override
    public void insertCpuStats(List<CpuStats> cpuStats) {
        dao.insert(cpuStats);
    }

    @Override
    public List<ReturningData> queryCpuStats(String date) {
        Sql sql;
        if (StringUtils.isEmpty(date)) {
            sql = Sqls.create("select cpu as total, resource_type, rid, platform, date from t_cpu_stats");
        } else {
            if (!selectDates().contains(date)) {
                date = selectDate();
            }
            sql = Sqls.create("select cpu as total, resource_type, rid, platform, date from t_cpu_stats where date = '" + date + "'");
        }
        sql.setCallback((connection, rs, sql1) -> {
            List<ReturningData> list1 = new ArrayList<>();
            while (null != rs && rs.next()) {
                ReturningData returningData = new ReturningData();
                returningData.setTotal(rs.getDouble("total"));
                returningData.setRid(rs.getString("rid"));
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

    private Set<String> selectDates() {
        Sql sql = Sqls.create("select distinct date from t_cpu_stats");
        sql.setCallback(Sqls.callback.strs());
        dao.execute(sql);
        return new HashSet<>(sql.getList(String.class));
    }

    private String selectDate() {
        Sql sql = Sqls.create("select date from t_cpu_stats where id = (select max(id) from t_cpu_stats)");
        sql.setCallback(Sqls.callback.str());
        dao.execute(sql);
        return sql.getString();
    }
}
