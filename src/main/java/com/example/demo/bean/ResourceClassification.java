package com.example.demo.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/3 3:53 PM
 */
@Data
@Setter
@Getter
public class ResourceClassification {

    Map<String, Double> cpuResourceMap;
    Map<String, Double> gMemResourceMap;
    Map<String, Double> gpuResourceMap;

    public ResourceClassification(Map<String, Double> cpuResourceMap) {
        this.cpuResourceMap = cpuResourceMap;
    }

    public ResourceClassification(Map<String, Double> cpuResourceMap, Map<String, Double> gMemResourceMap, Map<String, Double> gpuResourceMap) {
        this.cpuResourceMap = cpuResourceMap;
        this.gMemResourceMap = gMemResourceMap;
        this.gpuResourceMap = gpuResourceMap;
    }

}
