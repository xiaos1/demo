package com.example.demo.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/28 8:11 PM
 */
@Data
@Getter
@Setter
@Table("t_global_cluster_quota_resource")
public class PhyResource {
    @Id
    private long id;
    @Column("cluster_name")
    private String clusterName;
    @Column("port")
    private String port;
    @Column("platform")
    private String platform;
    @Column("physical_queue")
    private String physicalQueue;

    private int isQianXun;

    public void setIsQianXun(int isQianXun) {
        this.isQianXun = isQianXun;
    }

    public boolean isQianXun() {
        return isQianXun == 1;
    }

}
