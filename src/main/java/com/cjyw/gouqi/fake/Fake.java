package com.cjyw.gouqi.fake;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static com.cjyw.gouqi.fake.WebSocketServerHandler.sendHttpResponse;

/**
 * 模拟发射数据
 */
public class Fake {

    public static void test () throws IOException, InterruptedException {
        Fake.class.
                getClassLoader().getResourceAsStream("da/radar.txt");
        FileReader fr = new FileReader("/Users/duandongdong/daodao/open/gouqi/src/main/resources/ds/radar.txt");
        BufferedReader br=new BufferedReader(fr);
        String line="";
        while ((line=br.readLine())!=null) {
            String tmp =line.split("-->")[1];
            String id = tmp.split(",")[0].split(":")[1];
            String con = tmp.split(",")[1].split(":")[1];
            String range = tmp.split(",")[2].split(":")[1];
            String mm = (id + ": " + con + ":" + range);
            Thread.sleep(1000);
            WebSocketServerHandler.sendText(mm);
        }
        br.close();
        fr.close();
    }
}
