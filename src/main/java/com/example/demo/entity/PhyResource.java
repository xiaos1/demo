package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.json.JsonField;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/1/28 8:11 PM
 */
@Data
@Getter
@Setter
@Table("t_global_cluster_quota")
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
    @Column("is_qianxun")
    private int isQianXun;

    public boolean isQianXun() {
        return isQianXun == 1;
    }

    public boolean isUnknown() {
        return isQianXun != 1 && isQianXun != 2;
    }
}
