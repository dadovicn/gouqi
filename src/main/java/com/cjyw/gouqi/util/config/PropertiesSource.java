package com.cjyw.gouqi.util.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public enum PropertiesSource implements InitializeProperties{
    /** 属性容器 */
    INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesSource.class);
    private EnvConfig config;

    @Override
    public void initProps() {
        InputStream ins = this.getClass().
                getClassLoader().getResourceAsStream("application.yml");
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> app = (Map<String, Map<String, String>>) yaml.load(ins);
        String env = app.get("profiles").get("active");
        String location = "application-"+ env + ".yml";
        InputStream mmm = this.getClass().getClassLoader().getResourceAsStream(location);
        config = yaml.loadAs(mmm, EnvConfig.class);
    }

    PropertiesSource() {
        initProps();
    }

    public EnvConfig getConfig() {
        return config;
    }

}