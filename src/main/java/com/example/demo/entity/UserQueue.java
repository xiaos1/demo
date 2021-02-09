package com.example.demo.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/9 6:26 PM
 */
@Data
@Getter
@Setter
@Table("t_user_queue")
@NoArgsConstructor
public class UserQueue {
    @Id
    private long id;
    @Column("resource_type")
    private String resourceType;
    @Column("queue_name")
    private String queueName;
    @Column("physical_queue")
    private String physicalQueue;
    @Column("cluster_name")
    private String clusterName;
}
