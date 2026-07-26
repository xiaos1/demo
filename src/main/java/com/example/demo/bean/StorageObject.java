package com.example.demo.bean;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author by songxiao02 <songxiao02@baidu.com> on 2021/4/24 5:48 PM
 */
public class StorageObject {
    public static final Logger logger = LoggerFactory.getLogger(StorageObject.class);
    public String clusterName;
    public String phyQueue;
    public String logicalQueue;
    public String quotaA;
    public String quotaB;
    public String diff;
    public String resourceType;
    public String platform;

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Request request = null;
        try (FileInputStream excelFileInputStream = new FileInputStream("/Users/songxiao02/Downloads/Quota与RT数据校对.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook(excelFileInputStream);
            Sheet sheet = workbook.getSheet("Sheet3");
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            if (null == firstRow) {
                logger.warn("解析Excel失败，在第一行没有读取到任何数据！");
            }

            // 解析每一行的数据，构造数据对象
            int rowStart = firstRowNum;
            int rowEnd = sheet.getPhysicalNumberOfRows();
            System.out.println(rowEnd);
            for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                Row row = sheet.getRow(rowNum);

                if (null == row) {
                    continue;
                }
                String logicalQueue = row.getCell(2).toString();
                System.out.println(logicalQueue);
//                request = new Request.Builder()
//                        .get()
//                        .url("http://" + cluster + "-normandy.dmop.baidu.com:8033/tracker?action=physical&physical=" + phy)
//                        .build();
//                Call call = client.newCall(request);
//                Response response = call.execute();
//                String res = Objects.requireNonNull(response.body()).string();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
