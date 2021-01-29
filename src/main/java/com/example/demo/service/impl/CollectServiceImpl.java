package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.Resource;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public double[] getSummary() throws IOException {
        double beCpu = 0.0d, normalCpu = 0.0d, stableCpu = 0.0d;
        Resource resource = collectDao.fetchResource();
        String cluster = resource.getClusterName();
        String phy = resource.getPhysicalQueue();
        String resourceTypes = resource.getResourceTypes();
        boolean isQianXun = resource.isQianXun();
//        Request request = new Request.Builder()
//                .get()
//                .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/scheduler_" + phy + ".json")
//                .build();
        try {
            Request request = new Request.Builder()
                    .get()
                    .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/" + phy + "-resource.json")
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String res = Objects.requireNonNull(response.body()).string();
            JSONObject obj = JSON.parseObject(res);
            List<String> queues = new ArrayList<>();
            for (int i = 2; i < obj.size(); i++) {
                JSONObject o = obj.getJSONObject(i + 1 + "");
                String queueName = o.getString("name");
                queues.add(queueName);
                double cpu = o.getDouble("CPU");
                if (isQianXun) {
                    if (resourceTypes.contains("be")) {
                        stableCpu += cpu;
                    }
                    if (resourceTypes.contains("stable")) {
                        beCpu += cpu;
                    }
                } else {
                    if (resourceTypes.contains("be")) {
                        beCpu += cpu;
                    }
                    if (resourceTypes.contains("stable")) {
                        stableCpu += cpu;
                    }
                }
                if (resourceTypes.contains("normal")) {
                    normalCpu += cpu;
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

        }
        return new double[]{beCpu, normalCpu, stableCpu};
    }
}
