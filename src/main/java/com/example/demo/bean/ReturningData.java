package com.example.demo.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/3 8:13 PM
 */
@Data
@NoArgsConstructor
public class ReturningData {
    private double total;
    private String resourceType;
    private String platform;
    private String date;
}
