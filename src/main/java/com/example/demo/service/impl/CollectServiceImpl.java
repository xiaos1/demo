package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.ResourceClassification;
import com.example.demo.bean.ReturningData;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.CpuStats;
import com.example.demo.entity.PhyResource;
import com.example.demo.service.CollectService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
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
        double ridBeCpu, ridNormalCpu, ridStableCpu;
        double tBeCpu = 0.0d, tNormalCpu = 0.0d, tStableCpu = 0.0d; // total
        double stableQuota;
        String defaultResourcePriority = "STABLE";
        String defaultResourceAccountId = "";
        List<PhyResource> resources = getPhys();
        List<PhyResource> MpiAndStreamResources = collectDao.fetchMpiStreamResource();
        Map<String, ResourceClassification> anykeyResourceMap = new HashMap<>();
        Map<String, Double> totalCpuResourceMap = new HashMap<>();
        totalCpuResourceMap.put("normal", 0d);
        totalCpuResourceMap.put("be", 0d);
        totalCpuResourceMap.put("stable", 0d);
        anykeyResourceMap.putIfAbsent("TOTAL", new ResourceClassification(totalCpuResourceMap));
        Map<String, Double> mpiCpuResourceMap = new HashMap<>();
        mpiCpuResourceMap.put("normal", 0d);
        mpiCpuResourceMap.put("be", 0d);
        mpiCpuResourceMap.put("stable", 0d);
        anykeyResourceMap.putIfAbsent("MPI", new ResourceClassification(mpiCpuResourceMap));
        Map<String, Double> streamCpuResourceMap = new HashMap<>();
        streamCpuResourceMap.put("normal", 0d);
        streamCpuResourceMap.put("be", 0d);
        streamCpuResourceMap.put("stable", 0d);
        anykeyResourceMap.putIfAbsent("STREAM", new ResourceClassification(streamCpuResourceMap));
//  对MPI和STREAM集群处理
        for (PhyResource r : MpiAndStreamResources) {
            String cluster = r.getClusterName();
            String phy = r.getPhysicalQueue();
            String platform = r.getPlatform();
            if (!anykeyResourceMap.containsKey(platform)) {
                Map<String, Double> cpuResourceMap = new HashMap<>();
                cpuResourceMap.put("normal", 0d);
                cpuResourceMap.put("be", 0d);
                cpuResourceMap.put("stable", 0d);
                anykeyResourceMap.putIfAbsent(platform, new ResourceClassification(cpuResourceMap));
            }
            Request request = null;
            try {
                if ("MPI".equals(platform)) {
                    request = new Request.Builder()
                            .get()
                            .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/tracker?action=physical&physical=" + phy)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    String res = Objects.requireNonNull(response.body()).string();
                    JSONArray obj = JSON.parseObject(res).getJSONArray("physicals");//queue obj list under a physical queue
                    String quota = obj.getJSONObject(0).getString("totalCPUMemDisk").split("/")[0];
                    log.warn("MPI - cluster name: {}, physical queue: {}, be quota: {}", cluster, phy, quota);
                    stableQuota = anykeyResourceMap.get("MPI").getCpuResourceMap().get("stable");
                    stableQuota += Double.parseDouble(quota);
                    anykeyResourceMap.get("MPI").getCpuResourceMap().put("stable", stableQuota);
                } else if ("STREAM".equals(platform)) {
                    request = new Request.Builder()
                            .get()
                            .url("http://" + cluster + ".dmop.baidu.com:8025/tracker?action=physical&physical=" + phy)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    String res = Objects.requireNonNull(response.body()).string();
                    JSONArray obj = JSON.parseObject(res).getJSONArray("physicals");//queue obj list under a physical queue
                    String quota = obj.getJSONObject(0).getString("totalCPUMemDisk").split("/")[0];
                    log.warn("STREAM - cluster name: {}, physical queue: {}, be quota: {}", cluster, phy, quota);
                    stableQuota = anykeyResourceMap.get("STREAM").getCpuResourceMap().get("stable");
                    stableQuota += Double.parseDouble(quota);
                    anykeyResourceMap.get("STREAM").getCpuResourceMap().put("stable", stableQuota);
                }
            } catch (Exception e) {
                log.error("ops, the http request {} was wrong!", request == null ? null : request.url().toString());
            }
        }
        // 对其他集群处理
        for (PhyResource r : resources) {
            String cluster = r.getClusterName();
            String phy = r.getPhysicalQueue();
            boolean isQianXun = false;
            Request request = null;
            try {
                request = new Request.Builder()
                        .get()
                        .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/scheduler_" + phy + ".json")
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                String res = Objects.requireNonNull(response.body()).string();
                JSONObject jsonObject = JSON.parseObject(res);//queue obj list under a physical queue
                String conf = jsonObject.getString("ResourceTreePath").replace("./conf/", "");
                request = new Request.Builder()
                        .get()
                        .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/" + conf)
                        .build();
                call = client.newCall(request);
                response = call.execute();
                res = Objects.requireNonNull(response.body()).string();
                JSONObject obj = JSON.parseObject(res);//queue obj list under a physical queue
                List<String> queues = new ArrayList<>();
                for (String key : obj.keySet()) {
                    JSONObject o = obj.getJSONObject(key);
                    if (o.getString("type").equals("PHYSICAL")) {
                        try {
                            if (o.getString("STABLE.P0").equals("BESTEFFORT") || o.getString("STABLE.P0").equals("NORMAL")) {
                                r.setIsQianXun(1);
                                isQianXun = r.isQianXun();
                            }
                            log.info("Resource type of {}: STABLE.P0 denotes {}, UNSTABLE.P0 denotes {}", phy,
                                    o.getOrDefault("STABLE.P0", ""),
                                    o.getOrDefault("UNSTABLE.P0", ""));
                        } catch (Exception ignored) {
                        }
                    }
                }
                for (String key : obj.keySet()) {
                    JSONObject o = obj.getJSONObject(key);
                    if (!o.getString("type").equals("LOGICAL")) {
                        continue;
                    }
                    String queueName = o.getString("name");
                    queues.add(queueName);
                    double cpu = o.getDouble("CPU");
                    //some configuration do not contain this property MPI
                    if (!o.containsKey("resource.priority") || !o.getString("resource.priority").equals("UNSTABLE")) {
                        o.put("resource.priority", defaultResourcePriority);
                    }
                    if (!o.containsKey("resource.account.id")) {
                        o.put("resource.account.id", defaultResourceAccountId);
                    }
                    String rid = o.getString("resource.account.id");
                    if (!anykeyResourceMap.containsKey(rid)) {
                        Map<String, Double> ridCpuResourceMap = new HashMap<>();
                        ridCpuResourceMap.put("normal", 0d);
                        ridCpuResourceMap.put("be", 0d);
                        ridCpuResourceMap.put("stable", 0d);
                        anykeyResourceMap.putIfAbsent(rid, new ResourceClassification(ridCpuResourceMap));
                    }
                    if (!StringUtils.isEmpty(rid)) {
                        log.info("resource.account.id: {}", rid);
                    }
                    if (isQianXun) {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            ridNormalCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("normal") + cpu;
                            anykeyResourceMap.get(rid).getCpuResourceMap().put("normal", ridNormalCpu);
                            log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);

                            tNormalCpu += cpu;
                        } else if (o.getString("resource.priority").equals("STABLE")) {
                            ridBeCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("be") + cpu;
                            anykeyResourceMap.get(rid).getCpuResourceMap().put("be", ridBeCpu);
                            log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);

                            tBeCpu += cpu;
                        }
                    } else {
                        if (o.getString("resource.priority").equals("UNSTABLE")) {
                            ridBeCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("be") + cpu;
                            anykeyResourceMap.get(rid).getCpuResourceMap().put("be", ridBeCpu);
                            log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);

                            tBeCpu += cpu;
                        } else if (o.getString("resource.priority").equals("STABLE")) {
                            ridStableCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("stable") + cpu;
                            anykeyResourceMap.get(rid).getCpuResourceMap().put("stable", ridStableCpu);
                            log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, stable quota: {}", cluster, phy, o.getString("name"), cpu);

                            tStableCpu += cpu;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("ops, the http request {} was wrong!", request == null ? null : request.url().toString());
            }
        }
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("stable", tStableCpu +
                anykeyResourceMap.get("MPI").getCpuResourceMap().get("stable") +
                anykeyResourceMap.get("STREAM").getCpuResourceMap().get("stable"));
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("be", tBeCpu);
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("normal", tNormalCpu);
        return anykeyResourceMap;
    }

    static Map<String, String> ridPlatformMap = new HashMap<String, String>() {{
        put("5486b59407d6484ca0815893e589194b", "EMR");
        put("b7f0ca8bed474aa792fd7d5a9c0b2309", "BVC");
        put("a06acc9457a54f7789c4ebf9019e2fed", "ADU");
        put("aa60e652ea7f404ab6474da26d6dcb30", "batch-test");
    }};

    @Override
    public void doRecordData() {
        List<CpuStats> list = new ArrayList<>();
        Map<String, ResourceClassification> rcMap = doCollectData();
        for (String s : rcMap.keySet()) { //s : platform
            Map<String, Double> dMap = rcMap.get(s).getCpuResourceMap();
            for (String ss : dMap.keySet()) {// ss : resourceType
                if (s.equals("STREAM") || s.equals("MPI") || s.equals("TOTAL")) {
                    list.add(new CpuStats(dMap.get(ss), ss, s));
                } else if (!s.equals("ADU") && !s.equals("EMR") && !s.equals("BVC") && !s.equals("COMPASS") && !s.equals("OTHER")) {
                    list.add(new CpuStats(dMap.get(ss), ss, ridPlatformMap.getOrDefault(s, "")));
                }
            }
        }
        collectDao.insertCpuStats(list);
    }

    @Override
    public List<ReturningData> doGetData(String date) {
        return collectDao.queryCpuStats(date);
    }

    private List<PhyResource> getPhys() {
        List<PhyResource> list = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .get()
                    .url("http://10.83.128.26:8055/wucaishi/cluster/phyMap")
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String res = Objects.requireNonNull(response.body()).string();
            JSONArray data = JSON.parseObject(res).getJSONArray("data");
            JSONObject obj = data.getJSONObject(0);
            for (String cluster : obj.keySet()) {
                JSONArray clusterName = obj.getJSONArray(cluster);
                for (Object phyName : clusterName) {
                    PhyResource phyResource = new PhyResource();
                    phyResource.setClusterName(cluster);
                    phyResource.setPhysicalQueue(String.valueOf(phyName));
                    phyResource.setPlatform("");
                    list.add(phyResource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

