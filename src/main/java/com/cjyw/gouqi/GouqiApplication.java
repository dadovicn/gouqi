package com.cjyw.gouqi;

import com.cjyw.gouqi.core.RadioServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 主程序
 */
public class GouqiApplication {
    public static void main(String[] args) throws Exception {
        RadioServer server = new RadioServer();
        server.run(8887);
//        byte[] arr = new byte[] {
//                0x11, 0x22
//        };
//        System.out.println(Convertor.bytesToBinary(arr));
//
//        System.out.println(StringUtils.leftPad("123", 8, '0'));
//        int[] mm = Stream.of(Convertor.bytesToBinary(arr).split("")).mapToInt(Integer::parseInt).toArray();
////        Stream.of(Convertor.bytesToBinary(arr).toCharArray()).mapToInt();
//        String[] aa = "abc".split("");
//        System.out.println();
//
//        List<Integer> mm = new ArrayList<Integer>() {{
//            add(1);
//            add(0);
//            add(1);
//        }};
//        System.out.println(mm);
//        int xx = Integer.valueOf(String.valueOf(mm.stream().reduce((x,y) -> Integer.valueOf(x + String.valueOf(y))).get()), 2);
//        System.out.println(xx);
    }
}
