package com.cjyw.gouqi;

//import com.cjyw.gouqi.fake.Fake;

import com.cjyw.gouqi.core.RadioServer;

/**
 * 主程序
 */
public class GouqiApplication {
    public static void main(String[] args) throws Exception {
        RadioServer server = new RadioServer();
        server.run(8887);

//        Fake.init();

    }
}
