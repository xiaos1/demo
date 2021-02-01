package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.PhyResource;
import com.example.demo.service.CollectService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/29 11:40 AM
 */
@Slf4j
@Service
public class CollectServiceImpl implements CollectService {

    private static final Logger log = LoggerFactory.getLogger(CollectServiceImpl.class);

    @Autowired
    CollectDao collectDao;

    OkHttpClient client = new OkHttpClient();

    @Override
    public Map<String, Double> getSummary() {
        double beCpu = 0.0d, normalCpu = 0.0d, stableCpu = 0.0d;
        List<PhyResource> resources = collectDao.fetchResource();
        for (PhyResource r : resources) {
            String cluster = r.getClusterName();
            String phy = r.getPhysicalQueue();
            String port = r.getPort();
            boolean isQianXun = r.isQianXun();
//        Request request = new Request.Builder()
//                .get()
//                .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/scheduler_" + phy + ".json")
//                .build();
            try {
                Request request = new Request.Builder()
                        .get()
                        .url("http://" + cluster + "-normandy.dmop.baidu.com:" + port + "/filetree?action=cat&path=/" + phy + "-resource.json")
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                String res = Objects.requireNonNull(response.body()).string();
                JSONObject obj = JSON.parseObject(res);
                List<String> queues = new ArrayList<>();
                for (String key : obj.keySet()) {
                    JSONObject o = obj.getJSONObject(key);
                    if (!o.getString("type").equals("LOGICAL")) {
                        continue;
                    }
                    String queueName = o.getString("name");
                    queues.add(queueName);
                    double cpu = o.getDouble("CPU");
                    if (isQianXun) {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            normalCpu += cpu;
                        }
                        if (o.getString("resource.priority").equals("STABLE")) {
                            beCpu += cpu;
                        }
                    } else {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            beCpu += cpu;
                        }
                        if (o.getString("resource.priority").equals("STABLE")) {
                            stableCpu += cpu;
                        }
                    }
//            Request req = new Request.Builder()
//                    .get()
//                    .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/tracker?action=queue&queue=" + queueName + "&physical="+phy)
//                    .build();
//            JSONArray arr = JSON.parseObject(client.newCall(req).execute().body().string()).getJSONArray("queues");
//            for (Object o : arr) {
//                long cpu = Long.parseLong(JSON.parseObject(o.toString()).getString("staticCpuMemDisk").split("/")[0]);
//                summary += cpu;
//            }
                }
                log.info("{} {} queue counted done", queues.toString(), queues.size());
            } catch (Exception ignored) {
                log.debug("ops, the http request was wrong!");
            }
        }
        Map<String, Double> ret = new HashMap<>();
        ret.put("BE", beCpu);
        ret.put("normal", normalCpu);
        ret.put("stable", stableCpu);
        return ret;
    }
}

