package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.ResourceClassification;
import com.example.demo.bean.ReturningData;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.CpuStats;
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
@Service
public class CollectServiceImpl implements CollectService {

    private static final Logger log = LoggerFactory.getLogger(CollectServiceImpl.class);

    @Autowired
    CollectDao collectDao;

    OkHttpClient client = new OkHttpClient();

    @Override
    public Map<String, ResourceClassification> doCollectData() {
        double beCpu = 0.0d, normalCpu = 0.0d, stableCpu = 0.0d;
        String defaultResourcePriority = "STABLE";
        List<PhyResource> resources = collectDao.fetchResource();
        Map<String, ResourceClassification> platformResourceMap = new HashMap<>();
        Map<String, Double> cpuResourceMap;
        for (PhyResource r : resources) {
            String cluster = r.getClusterName();
            String phy = r.getPhysicalQueue();
            String platform = r.getPlatform();
            if (!platformResourceMap.containsKey(platform)) {
                cpuResourceMap = new HashMap<>();
                cpuResourceMap.put("normal", normalCpu);
                cpuResourceMap.put("be", beCpu);
                cpuResourceMap.put("stable", stableCpu);
                platformResourceMap.putIfAbsent(platform, new ResourceClassification(cpuResourceMap));
            }
            boolean isQianXun = r.isQianXun();
//        Request request = new Request.Builder()
//                .get()
//                .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/scheduler_" + phy + ".json")
//                .build();
            Request request = null;
            try {
                request = new Request.Builder()
                        .get()
                        .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/" + phy + "-resource.json")
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
                    if (!o.containsKey("resource.priority") || !o.getString("resource.priority").equals("UNSTABLE")) {
                        o.put("resource.priority", defaultResourcePriority);
                    }
                    if (isQianXun) {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            normalCpu = platformResourceMap.get(platform).getCpuResourceMap().get("normal") + cpu;
                        }
                        if (o.getString("resource.priority").equals("STABLE")) {
                            beCpu = platformResourceMap.get(platform).getCpuResourceMap().get("be") + cpu;
                        }
                    } else {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            beCpu = platformResourceMap.get(platform).getCpuResourceMap().get("be") + cpu;
                        }
                        if (o.getString("resource.priority").equals("STABLE")) {
                            stableCpu = platformResourceMap.get(platform).getCpuResourceMap().get("stable") + cpu;
                        }
                    }
                    platformResourceMap.get(platform).getCpuResourceMap().put("normal", normalCpu);
                    platformResourceMap.get(platform).getCpuResourceMap().put("be", beCpu);
                    platformResourceMap.get(platform).getCpuResourceMap().put("stable", stableCpu);
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
            } catch (Exception e) {
                log.error("ops, the http request {} was wrong!", request == null ? null : request.url().toString());
            }
        }
        Map<String, Double> ret = new HashMap<>();
        ret.put("BE", beCpu);
        ret.put("normal", normalCpu);
        ret.put("stable", stableCpu);
        return platformResourceMap;
    }

    @Override
    public void doRecordData() {
        List<CpuStats> list = new ArrayList<>();
        Map<String, ResourceClassification> rcMap = doCollectData();
        for (String s : rcMap.keySet()) {
            Map<String, Double> dMap = rcMap.get(s).getCpuResourceMap();
            for (String ss : dMap.keySet()) {
                list.add(new CpuStats(dMap.get(ss), ss, s));
            }
        }
        collectDao.insertCpuStats(list);
    }

    @Override
    public List<ReturningData> doGetData() {
        return collectDao.queryCpuStats();
    }
}

