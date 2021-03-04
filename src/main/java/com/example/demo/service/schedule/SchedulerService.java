package com.example.demo.service.schedule;

import com.example.demo.service.CollectService;
import com.example.demo.service.impl.CollectServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/3 2:59 PM
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CollectServiceImpl.class);

    @Autowired
    CollectService collectService;

    @Scheduled(zone = "Asia/Shanghai", cron = "0 40 3 * * ?")
//    @Scheduled(zone = "Asia/Shanghai",cron = "* */20 * * * ?")
//    @PostConstruct
    public void doStats() {
        log.warn("scheduler coming");
        collectService.doRecordData();
    }
}
