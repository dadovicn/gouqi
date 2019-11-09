package com.cjyw.gouqi;

import com.cjyw.gouqi.core.RadioServer;
import com.cjyw.gouqi.core.report.mq.TargetReport;
import com.cjyw.gouqi.util.Convertor;
import com.cjyw.gouqi.util.config.PropertiesSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 主程序
 */
public class GouqiApplication {
    public static void main(String[] args) throws Exception {
        RadioServer server = new RadioServer();
        server.run(8887);
    }
}
