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
    private static final double  TARGET_SPACE = 5d;
    private static final double  ANGLE_SPACE = 5d;
    private static final Logger log = LoggerFactory.getLogger(TargetFitting.class);
    private static AtomicInteger cartId = new AtomicInteger(0);

    /** <k, range> */
    private static Cache<Integer, Target> fitMap = CacheBuilder.newBuilder()
            .maximumSize(32).expireAfterAccess(2000, TimeUnit.MILLISECONDS).build();

    /**
     * 目标跟踪
     * @param cur 当前目标
     */
    public static void trace(Target cur) {
        log.info(fitMap.asMap().keySet().toString());
        if(cur.getTraceVal().intValue() == 1 && cur.getRangeVal() < 60) {
            // 目标出现
            if(fitMap.size() == 0) {
                cartId.set(0);
                fitMap.put(cartId.addAndGet(1), cur);
                // 第一个目标
            } else {
                if(checkTargetCart(cur)) {
                    // 新目标出现
                    fitMap.put(cartId.addAndGet(1), cur);
                    log.info("新目标出现: {}", cur.getRangeVal());
                    if(fitMap.asMap().size() >= 3) {
                        launderTarget();
                    }
                } else {
                    correctAngle(cur);
                    // 已有目标, 更新目标
                    updatePosition(cur.getRangeVal(), cur);
                }
            }
        }
        if(cur.getTraceVal().intValue() == 3 && filter(cur) && cur.getConfidenceVal().intValue() > 40) {
            // 更新距离
            // 已经被跟踪的目标
            updatePosition(cur.getRangeVal(), cur);
        }
    }

    /**
     * 目标检测
     * @param cur
     * @return
     */
    public static boolean checkTargetCart(Target cur) {
        // 返回当前目标中的最小距离
        Optional<Entry<Integer, Target>> lastCur = fitMap.asMap().entrySet().parallelStream().reduce((x, y) -> x.getValue().getRangeVal() < y.getValue().getRangeVal() ? x: y);
        if(lastCur.isPresent()) {
            // 新目标间距 大于 20
            if((lastCur.get().getValue().getRangeVal()) - cur.getRangeVal() > 30) {
                return true;
            } else {
//                // 调整参数
//                if(Math.abs(lastCur.get().getValue().getAngleVal() - cur.getAngleVal()) > ANGLE_SPACE) {
//                    return true;
//                }
            }
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
        // 先找是属于哪边的车

        // 规约开始
        if(range > 150.0) {
            // 直接丢掉
            return;
        }
        Optional<Entry<Integer, Target>> max = fitMap.asMap().entrySet().parallelStream().reduce((x, y) -> x.getValue().getRangeVal() < y.getValue().getRangeVal() ? y: x);
        if(max.isPresent()) {
            double maxRange = max.get().getValue().getRangeVal();
            int pos = max.get().getKey();
            if(range > maxRange) {
                cur.setCanId(pos);
                fitMap.put(pos, cur);
                if(cur.getConfidenceVal().intValue() == 0 && cur.getRangeVal() < 50) {
                    TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                }
                if(cur.getConfidenceVal().intValue() >= 60) {
                    TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                }
            } else {
                fitMap.put(0, new Target(0, 1l, 0d, 0d, 0d, 0d, 0d, 0d));
                fitMap.asMap().entrySet().parallelStream().reduce((x, y) -> {
                    if (x.getValue().getRangeVal() < range && range < y.getValue().getRangeVal()) {
                        // 更新目标位置
                        if(x.getKey() != 0) {
                            fitMap.put(x.getKey(), cur);
                            cur.setCanId(x.getKey());
                            if(cur.getConfidenceVal().intValue() == 0 && cur.getRangeVal() < 50) {
                                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                            }
                            if(cur.getConfidenceVal().intValue() >= 60) {
                                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                            }
                        }
                    }
                    return y;
                });
                fitMap.invalidate(0);
            }
        }
    }

    /**
     * 目标清洗: 移除假目标
     */
    public static void launderTarget() {
        List<Integer> mqp = new ArrayList<>(10);
        List<Integer> total = new ArrayList<>(2);
        if(fitMap.asMap().size() == 2) {
            fitMap.asMap().entrySet().forEach(w -> {
               total.add(w.getValue().getRangeVal().intValue());
            });
            if(Math.abs(total.get(0) - total.get(1)) < 5) {
                Integer a = (Integer) fitMap.asMap().keySet().toArray()[0];
                fitMap.invalidate(a);
            }
        } else if (fitMap.asMap().size() > 2){
            fitMap.asMap().entrySet().parallelStream().reduce((x, y) -> {
                if(y.getValue().getRangeVal() - x.getValue().getRangeVal() < 3) {
                    log.info("移除目标: {}", x.getKey());
                    mqp.add(x.getKey());
                }
                return y;
            });
            mqp.forEach(i -> {
                fitMap.invalidate(i);
            });
        }


    }

    /**
     * 纠正角度
     * @param cur
     */
    public static void correctAngle(Target cur) {
        if(Math.abs(cur.getAngleVal()) > 5) {
            if(cur.getAngleVal() < 0) {
                cur.setAngleVal(-1.5);
            } else {
                cur.setAngleVal(1.5);
            }
        }
    }

    /**
     * todo 滤波
     * @param cur 当前目标
     */
    public static boolean filter(Target cur) {
        if(Math.abs(cur.getAngleVal()) < 5) {
           return true;
        }
        return false;
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
