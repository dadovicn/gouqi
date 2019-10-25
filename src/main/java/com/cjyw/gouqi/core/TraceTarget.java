package com.cjyw.gouqi.core;

import com.cjyw.gouqi.entity.Target;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 目标跟踪
     * @param canId 消息 id
     * @param traceVal 轨迹状态
     * @param confidenceVal  置信度
     * @param rangeVal 距离
     */
    public static void trace(int canId, Long traceVal,  Long confidenceVal, Long rangeVal) {
        Target cur = new Target(traceVal, confidenceVal, rangeVal);
        if(targetsCache.getIfPresent(canId) == null) {
            targetsCache.put(canId, new ArrayList<Target>() {{
                add(cur);
            }});
        } else {
            List<Target> targetList = targetsCache.getIfPresent(canId);
            if(trueTarget.getIfPresent(canId) != null) {
                if(trueTarget.getIfPresent(canId).get() == 0) {
                    Target t1 = targetList.get(targetList.size() -2);
                    Target t2 = targetList.get(targetList.size() -1);
                    trueTarget.put(canId, new AtomicInteger(trueTarget.getIfPresent(canId).addAndGet(1)));
                    log.info("canId:{}, 置信度:{}, 距离:{}", t1.getTraceVal(), t1.getConfidenceVal(), t1.getRangeVal());
                    log.info("canId:{}, 置信度:{}, 距离:{}", t2.getTraceVal(), t2.getConfidenceVal(), t2.getRangeVal());
                }
                log.info("canId:{}, 置信度:{}, 距离:{}", canId, traceVal, rangeVal);
            } else {
                if (cur.getRangeVal().intValue() == 80 && targetList.get(targetList.size() -1).getConfidenceVal().intValue() == 60) {
                    // 真实目标
                    trueTarget.put(canId, new AtomicInteger(0));
                }
            }
            targetList.add(cur);
        }
    }

}
