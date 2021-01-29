package com.example.demo.dao.impl;

import com.example.demo.dao.CollectDao;
import com.example.demo.entity.Resource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 11:41 AM
 */
@Slf4j
@Repository
public class CollectDaoImpl implements CollectDao {

    @Autowired
    Dao dao;

    @Override
    public Resource fetchResource() {
        return dao.fetch(Resource.class, Cnd.where("id", "=", 1));
    }
}
