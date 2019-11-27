package com.cjyw.gouqi.core;

import com.alibaba.fastjson.JSON;
import com.cjyw.gouqi.core.report.mq.TargetReport;
import com.cjyw.gouqi.entity.Target;
import com.cjyw.gouqi.util.config.PropertiesSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 目标跟踪
 */
public class TraceTarget {
    private static final Logger log = LoggerFactory.getLogger(TraceTarget.class);

    private static Cache<Integer, AtomicInteger> trueTarget = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(3, TimeUnit.SECONDS).build();

    private static Cache<Integer, List<Target>> targetsCache = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(3, TimeUnit.SECONDS).build();

    /**
     * 影响因子: 目标初次出现的位置
     */
    private static Cache<Integer, Integer> factorIndex = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(3, TimeUnit.SECONDS).build();

    /**
     * 目标跟踪
     * @param canId 消息 id
     * @param traceVal 轨迹状态
     * @param confidenceVal  置信度
     * @param rangeVal 距离
     */
    public static void trace(int canId, Long traceVal,  double confidenceVal, double rangeVal, double angleVal, double rateValue, double powerValue, double frameValue) {
        Target cur = new Target(canId, traceVal, confidenceVal, rangeVal, angleVal, rateValue, powerValue, frameValue);
        if(targetsCache.getIfPresent(canId) == null) {
            targetsCache.put(canId, new ArrayList<Target>() {{
                add(cur);
            }});
        } else {
            List<Target> targetList = targetsCache.getIfPresent(canId);
            if(trueTarget.getIfPresent(canId) != null) {
                if(trueTarget.getIfPresent(canId).get() == 0) {
                    factorIndex.put(canId, targetList.size());
                    trueTarget.put(canId, new AtomicInteger(trueTarget.getIfPresent(canId).addAndGet(1)));
                }
                if(factorIndex.getIfPresent(canId) >= 100 ) {
                    // ok
                    if(factorIndex.getIfPresent(canId) == 100) {
                        targetList.stream().forEach(i -> {
                            TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(i));
                            log.info(i.toString());
                        });
                        factorIndex.put(canId, 101);
                    } else {
                        if(cur.getTraceVal() == 3) {
                            TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                            log.info(cur.toString());
                        }
                    }

                } else {
                    Integer factorSize = factorIndex.getIfPresent(canId) + 8;
                    if(targetList.size() > factorSize) {
                        log.debug("targetList.size = {}, {} ", targetList.size(), factorSize);
                        checkConfidence(canId, targetList.subList(factorIndex.getIfPresent(canId), factorIndex.getIfPresent(canId) + 6));
                    }
                }

            } else {
                if (cur.getConfidenceVal().intValue() == 80 && targetList.get(targetList.size() -1).getConfidenceVal().intValue() == 60
                    && rangeVal < 7000l  // 对于大车和小车都一样
                ) {
                    // 真实目标
                    trueTarget.put(canId, new AtomicInteger(0));
                }
            }
            targetList.add(cur);
        }
    }

    /**
     * 算出连续六个目标的总体评分, 达到标准, 放行.
     * @param canId
     * @param targets
     */
    public static void checkConfidence(int canId, List<Target> targets) {
        double score = targets.stream().map(x -> x.getConfidenceVal()).collect(Collectors.toList()).stream().reduce((x, y) -> x + y).get();
        log.debug("分数:{} ", score);
        if((Double.valueOf(score) / 6) > 94.0) {
            log.info("结果: {}", Double.valueOf(score) / 6);
            factorIndex.put(canId, 100);
        }
    }


}
