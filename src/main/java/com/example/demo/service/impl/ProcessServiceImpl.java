package com.example.demo.service.impl;

import com.example.demo.bean.StorageObject;
import com.example.demo.service.ProcessService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/4/24 5:52 PM
 */
@Service
public class ProcessServiceImpl implements ProcessService {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    static SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Data from two dates to be compared.
     *
     * @param dateA    format 210601
     * @param dateB
     * @param confFile confFile containing rid platform mapping
     * @param logFile  Data source from log file
     * @return StorageObject list
     */
    @Override
    public List<StorageObject> getDataSource(String dateA, String dateB, File confFile, File logFile) throws IOException, ParseException {
        Date day = sdf.parse(dateA);
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date dAfter = cal.getTime();
        String date = logDate.format(dAfter);
        String bashCommand = "grep '" + date +"' catalina.out |grep -v 'STREAM'| grep -v 'MPI'|grep -v 'Resource type'" +
                "|awk -F \\| '{print $6}' |grep -A 1 'resource.account.id: 5486b59407d6484ca0815893e589194b' |" +
                "grep -v 'resource.account.id: 5486b59407d6484ca0815893e58919494b'|grep ' quota:' ";
        System.out.println(bashCommand);
        Runtime runtime = Runtime.getRuntime();
        Process pro = runtime.exec(bashCommand);

        return null;
    }

    /**
     * Data lists to be differed and processed.
     *
     * @param dataA
     * @param dataB
     */
    @Override
    public void processData(List<StorageObject> dataA, List<StorageObject> dataB) {

    }

    public static void main(String[] args) throws ParseException {
        Date day = sdf.parse("210516");
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date dAfter = cal.getTime();
        String date = logDate.format(dAfter);
        String bashCommand = "grep '" + date +"' catalina.out |grep -v 'STREAM'| grep -v 'MPI'|grep -v 'Resource type'" +
                "|awk -F \\| '{print $6}' |grep -A 1 'resource.account.id: 5486b59407d6484ca0815893e589194b' |" +
                "grep -v 'resource.account.id: 5486b59407d6484ca0815893e58919494b'|grep ' quota:' ";
        System.out.println(bashCommand);
        Integer a = 9;
        Long b = 9L;
        System.out.println(b.equals(a));
    }
}
