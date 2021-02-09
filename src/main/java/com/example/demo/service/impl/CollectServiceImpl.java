package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.ResourceClassification;
import com.example.demo.bean.ReturningData;
import com.example.demo.dao.CollectDao;
import com.example.demo.entity.CpuStats;
import com.example.demo.entity.PhyResource;
import com.example.demo.entity.UserQueue;
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
        double pBeCpu, pNormalCpu, pStableCpu;
        double ridBeCpu, ridNormalCpu, ridStableCpu;
        double tBeCpu = 0.0d, tNormalCpu = 0.0d, tStableCpu = 0.0d;
        double bufferBeCpu = 0.0d, bufferNormalCpu = 0.0d, bufferStableCpu = 0.0d;
        double stableQuota = 0.0d;
        String defaultResourcePriority = "STABLE";
        String defaultResourceAccountId = "";
        List<PhyResource> resources = collectDao.fetchResource();
        Map<String, ResourceClassification> anykeyResourceMap = new HashMap<>();
        Map<String, Double> bufferCpuResourceMap = new HashMap<>();
        bufferCpuResourceMap.put("normal", 0d);
        bufferCpuResourceMap.put("be", 0d);
        bufferCpuResourceMap.put("stable", 0d);
        Map<String, Double> totalCpuResourceMap = new HashMap<>();
        totalCpuResourceMap.put("normal", 0d);
        totalCpuResourceMap.put("be", 0d);
        totalCpuResourceMap.put("stable", 0d);
        anykeyResourceMap.putIfAbsent("EMRBUFFER", new ResourceClassification(bufferCpuResourceMap));
        anykeyResourceMap.putIfAbsent("TOTAL", new ResourceClassification(totalCpuResourceMap));
        Map<String, Double> mpiCpuResourceMap = new HashMap<>();
        mpiCpuResourceMap.put("normal", 0d);
        mpiCpuResourceMap.put("be", 0d);
        mpiCpuResourceMap.put("stable", 0d);
        anykeyResourceMap.putIfAbsent("MPI", new ResourceClassification(mpiCpuResourceMap));
        Map<String, String> map = fetchQueue();
        for (PhyResource r : resources) {
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
            boolean isQianXun = r.isQianXun();
            boolean unknown = r.isUnknown();
//        Request request = new Request.Builder()
//                .get()
//                .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/scheduler_" + phy + ".json")
//                .build();
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
                    stableQuota += Double.parseDouble(obj.getJSONObject(0).getString("totalCPUMemDisk").split("/")[1]);
                    anykeyResourceMap.get("MPI").getCpuResourceMap().put("stable", stableQuota);
                    continue;
                } else if (!platform.equals("STREAM")) {
                    request = new Request.Builder()
                            .get()
                            .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/filetree?action=cat&path=/" + phy + "-resource.json")
                            .build();
                } else {
                    request = new Request.Builder()
                            .get()
                            .url("http://" + cluster + ".dmop.baidu.com:8025/filetree?action=cat&path=/" + phy + "-resource.json")
                            .build();
                }
                Call call = client.newCall(request);
                Response response = call.execute();
                String res = Objects.requireNonNull(response.body()).string();
                JSONObject obj = JSON.parseObject(res);//queue obj list under a physical queue
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
                    if (o.getString("name").contains("buffer") && o.getString("name").contains("_emr_")
                            && o.getString("name").contains("_preempt-standard_")) {
                        bufferBeCpu += cpu;
                        tBeCpu += cpu;
                        log.warn("EMRBUFFER - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);
                    } else if (o.getString("name").contains("buffer") && o.getString("name").contains("_emr_")
                            && o.getString("name").contains("_preempt-high_")) {
                        bufferNormalCpu += cpu;
                        tNormalCpu += cpu;
                        log.warn("EMRBUFFER - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);
                    } else if (o.getString("name").contains("buffer") && o.getString("name").contains("_emr_")
                            && o.getString("name").contains("_standard_")) {
                        bufferStableCpu += cpu;
                        tStableCpu += cpu;
                        log.warn("EMRBUFFER - cluster name: {}, physical queue: {}, logical queue: {}, stable quota: {}", cluster, phy, o.getString("name"), cpu);
                    } else {
                        if (unknown) {
                            isQianXun = false;
                        }
                        if ("EMR+ (buffer excluded)".equals(ridPlatformMap.get(rid))) {
                            String k = o.getString("name").toLowerCase() + " " + cluster.toLowerCase() + " " + phy.toLowerCase();
                            if (map.containsKey(k)) {
                                if ("stable".equals(map.get(k))) {
                                    ridStableCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("stable") + cpu;
                                    anykeyResourceMap.get(rid).getCpuResourceMap().put("stable", ridStableCpu);
                                    log.info("EMRGENERAL - cluster name: {}, physical queue: {}, logical queue: {}, stable quota: {}", cluster, phy, o.getString("name"), cpu);

                                    tStableCpu += cpu;
                                } else if ("be".equals(map.get(k))) {
                                    ridBeCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("be") + cpu;
                                    anykeyResourceMap.get(rid).getCpuResourceMap().put("be", ridBeCpu);
                                    log.info("EMRGENERAL - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);

                                    tBeCpu += cpu;
                                } else if ("normal".equals(map.get(k))) {
                                    ridNormalCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("normal") + cpu;
                                    anykeyResourceMap.get(rid).getCpuResourceMap().put("normal", ridNormalCpu);
                                    log.info("EMRGENERAL - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);

                                    tNormalCpu += cpu;
                                } else {
                                    log.error("EMRGENERAL - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);
                                }
                            } else {
                                log.error("NOT EMRGENERAL - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);
                            }
                        } else if (isQianXun) {
                            if (o.getString("resource.priority").equals("UNSTABLE")) {
                                ridNormalCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("normal") + cpu;
                                anykeyResourceMap.get(rid).getCpuResourceMap().put("normal", ridNormalCpu);
                                log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, normal quota: {}", cluster, phy, o.getString("name"), cpu);
                                pNormalCpu = anykeyResourceMap.get(platform).getCpuResourceMap().get("normal") + cpu;
                                anykeyResourceMap.get(platform).getCpuResourceMap().put("normal", pNormalCpu);

                                tNormalCpu += cpu;
                            } else if (o.getString("resource.priority").equals("STABLE")) {
                                ridBeCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("be") + cpu;
                                anykeyResourceMap.get(rid).getCpuResourceMap().put("be", ridBeCpu);
                                log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);
                                pBeCpu = anykeyResourceMap.get(platform).getCpuResourceMap().get("be") + cpu;
                                anykeyResourceMap.get(platform).getCpuResourceMap().put("be", pBeCpu);

                                tBeCpu += cpu;
                            }
                        } else {
                            if (o.getString("resource.priority").equals("UNSTABLE")) {
                                ridBeCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("be") + cpu;
                                anykeyResourceMap.get(rid).getCpuResourceMap().put("be", ridBeCpu);
                                log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, be quota: {}", cluster, phy, o.getString("name"), cpu);
                                pBeCpu = anykeyResourceMap.get(platform).getCpuResourceMap().get("be") + cpu;
                                anykeyResourceMap.get(platform).getCpuResourceMap().put("be", pBeCpu);

                                tBeCpu += cpu;
                            } else if (o.getString("resource.priority").equals("STABLE")) {
                                ridStableCpu = anykeyResourceMap.get(rid).getCpuResourceMap().get("stable") + cpu;
                                anykeyResourceMap.get(rid).getCpuResourceMap().put("stable", ridStableCpu);
                                log.info("GENERAL - cluster name: {}, physical queue: {}, logical queue: {}, stable quota: {}", cluster, phy, o.getString("name"), cpu);
                                pStableCpu = anykeyResourceMap.get(platform).getCpuResourceMap().get("stable") + cpu;
                                anykeyResourceMap.get(platform).getCpuResourceMap().put("stable", pStableCpu);

                                tStableCpu += cpu;
                            }
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
//                log.info("{} {} queue counted done", queues.toString(), queues.size());
            } catch (Exception e) {
                log.error("ops, the http request {} was wrong!", request == null ? null : request.url().toString());
            }
        }
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("stable", tStableCpu);
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("be", tBeCpu);
        anykeyResourceMap.get("TOTAL").getCpuResourceMap().put("normal", tNormalCpu);
        anykeyResourceMap.get("EMRBUFFER").getCpuResourceMap().put("be", bufferBeCpu);
        anykeyResourceMap.get("EMRBUFFER").getCpuResourceMap().put("normal", bufferNormalCpu);
        anykeyResourceMap.get("EMRBUFFER").getCpuResourceMap().put("stable", bufferStableCpu);
        return anykeyResourceMap;
    }

    static Map<String, String> ridPlatformMap = new HashMap<String, String>() {{
        put("5486b59407d6484ca0815893e589194b", "EMR+ (buffer excluded)");
        put("b7f0ca8bed474aa792fd7d5a9c0b2309", "BVC");
        put("a06acc9457a54f7789c4ebf9019e2fed", "ADU");
        put("aa60e652ea7f404ab6474da26d6dcb30", "batch-test");
    }};

    @Override
    public void doRecordData() {
        Double bvcStable = 0.0d;
        List<CpuStats> list = new ArrayList<>();
        Map<String, ResourceClassification> rcMap = doCollectData();
        for (String s : rcMap.keySet()) {
            Map<String, Double> dMap = rcMap.get(s).getCpuResourceMap();
            for (String ss : dMap.keySet()) {
                if (s.equals("STREAM") || s.equals("MPI") || s.equals("EMRBUFFER") || s.equals("TOTAL")) {
                    list.add(new CpuStats(dMap.get(ss), ss, s));
                } else if (!s.equals("ADU") && !s.equals("EMR") && !s.equals("BVC") && !s.equals("COMPASS") && !s.equals("OTHER")) {
                    if ("BVC".equals(ridPlatformMap.get(s))) {
                        bvcStable += dMap.get(ss);
                    } else {
                        list.add(new CpuStats(dMap.get(ss), ss, ridPlatformMap.getOrDefault(s, "")));
                    }
                }
            }
        }
        list.add(new CpuStats(bvcStable, "stable", "BVC"));
        list.add(new CpuStats(0.0d, "normal", "BVC"));
        list.add(new CpuStats(0.0d, "be", "BVC"));
        collectDao.insertCpuStats(list);
    }

    @Override
    public List<ReturningData> doGetData() {
        return collectDao.queryCpuStats();
    }

    @Override
    public Map<String, String> fetchQueue() {
        List<UserQueue> list = collectDao.fetchQueue();
        Map<String, String> queueRtypeMap = new HashMap<>();
        for (UserQueue q : list) {
            queueRtypeMap.putIfAbsent(q.getQueueName().toLowerCase() + " " + q.getClusterName().toLowerCase() + " " +
                    q.getPhysicalQueue().toLowerCase(), q.getResourceType());
        }
        return queueRtypeMap;
    }
}

