package com.cjyw.gouqi.core;

import com.alibaba.fastjson.JSON;
import com.cjyw.gouqi.core.report.mq.TargetReport;
import com.cjyw.gouqi.entity.Target;
import com.cjyw.gouqi.util.config.PropertiesSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Map.*;

/**
 * 目标拟合算法
 */
public class TargetFitting {
    private static final double  ANGLE_SPLIT = 0d;
    private static final Logger log = LoggerFactory.getLogger(TargetFitting.class);
    private static AtomicInteger targetId = new AtomicInteger(0);

    /** <k, range> */
    private static Cache<Integer, Double> fitMap = CacheBuilder.newBuilder()
            .maximumSize(32).expireAfterAccess(3, TimeUnit.SECONDS).build();

    /**
     * 目标跟踪
     * @param cur 当前目标
     */
    public static void trace(Target cur) {
        if(cur.getTraceVal().intValue() == 1 && cur.getRangeVal() < 60) {
            // 目标出现
            if(fitMap.size() == 0) {
                fitMap.put(targetId.addAndGet(1), cur.getRangeVal());
                // 第一个目标
            } else {
                if(checkTarget(cur.getRangeVal())) {
                    // 新目标出现
                    log.info("新目标出现: ", targetId.toString() );
                    fitMap.put(targetId.addAndGet(1), cur.getRangeVal());
                } else {
                    // 已有目标, 更新目标
                    log.info("先找目标, 然后更新, 更新模式");
                    updatePosition(cur.getRangeVal(), cur);
                }
            }
        }
        if(cur.getTraceVal().intValue() == 3) {
            // 已经被跟踪的目标
            updatePosition(cur.getRangeVal(), cur);
        }
    }

    /**
     * 检查是否为新目标
     * @return true: 新目标, false 已有目标
     */
    public static boolean checkTarget(Double range) {
        log.info("尺寸: {}" , fitMap.size());
        Optional<Entry<Integer, Double>> lastCur = fitMap.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? x: y);
        if(lastCur.isPresent()) {
            log.info("找到的最小值为 {}", lastCur.get().getValue());
            return range < lastCur.get().getValue();
        } else {
            log.info("没找到最小值");
        }
        return false;
    }

    /**
     * 找到 k , 并更新 range
     * @param range
     * @param cur
     * @return
     */
    public static void updatePosition(Double range, Target cur) {
        // 规约开始
        if(range > 150.0) {
            // 直接丢掉
            return;
        }
        Optional<Entry<Integer, Double>> max = fitMap.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? y: x);
        if(max.isPresent()) {
            double maxRange = max.get().getValue();
            int pos = max.get().getKey();
            if(range > maxRange) {
                cur.setCanId(pos);
                log.info(cur.toString());
                fitMap.put(pos, range);
                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
            }
        }
        fitMap.put(0, 0d);
        fitMap.asMap().entrySet().stream().reduce((x, y) -> {
            if (x.getValue() < range && range < y.getValue()) {
                // 更新目标位置
                fitMap.put(x.getKey(), range);
                cur.setCanId(x.getKey());
                log.info(cur.toString());
                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
            }
            return y;
        });
        fitMap.invalidate(0);
    }

    /**
     * todo 滤波
     * @param cur 当前目标
     */
    public static boolean filter(Target cur) {
        return true;
    }

    /**
     * @deprecated 样本不行(不够), 无法区分
     * true: 行车道, false: 超车道
     * todo 每个雷达都要计算出最佳 {x}
     * @param angle 水平角度°
     * @return {@code true} if angle greater than {x} is {@code 0}, otherwise {@code false}
     *
     */
    public static boolean splitByAngle(double angle) {
        return angle > ANGLE_SPLIT;
    }
}
