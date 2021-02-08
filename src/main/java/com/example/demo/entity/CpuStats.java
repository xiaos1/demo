package com.example.demo.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/2/3 4:25 PM
 */
@Data
@Getter
@Setter
@Table("t_cpu_stats")
@NoArgsConstructor
public class CpuStats {
    @Id
    private long id;
    @Column("cpu")
    private double cpu;
    @Column("resource_type")
    private String resourceType;
    @Column("platform")
    private String platform;
    @Column("rid")
    private String rid = "";
    @Column("date")
    private String date;
    @Column("create_time")
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());
    @Column("update_time")
    private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

    public CpuStats(double cpu, String resourceType, String platform) {
        this.cpu = cpu;
        this.resourceType = resourceType;
        this.platform = platform;
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.date = getDate();
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        Date date = calendar.getTime();
        return sdf.format(date);
    }
}
