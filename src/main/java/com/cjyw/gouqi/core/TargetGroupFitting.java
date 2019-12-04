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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Map.Entry;

/**
 * 目标拟合算法
 */
public class TargetGroupFitting {
    private static final double  ANGLE_SPLIT = 0d;
    private static final Logger log = LoggerFactory.getLogger(TargetGroupFitting.class);
    private static AtomicInteger cartId = new AtomicInteger(0);
    private static AtomicInteger carId = new AtomicInteger(0);


    /** <k, range> */
    private static Cache<Integer, Double> fitCartMap = CacheBuilder.newBuilder()
            .maximumSize(32).expireAfterAccess(1000, TimeUnit.MILLISECONDS).build();

    private static Cache<Integer, Double> fitCarMap = CacheBuilder.newBuilder()
            .maximumSize(32).expireAfterAccess(1000, TimeUnit.MILLISECONDS).build();

    /**
     * 目标跟踪
     * @param cur 当前目标
     */
    public static void trace(Target cur) {
        if(Math.abs(cur.getAngleVal()) > 5) {
            if(cur.getAngleVal() < 0) {
                cur.setAngleVal(-1.5);
            } else {
                cur.setAngleVal(1.5);
            }
        }
        if(cur.getTraceVal().intValue() == 1 && cur.getRangeVal() < 60) {
            // 目标出现
            if(cur.getRangeVal().intValue() < 25) {
                if(fitCartMap.size() == 0) {
                    cartId.set(0);
                    fitCartMap.put(cartId.addAndGet(1), cur.getRangeVal());
                    // 第一个目标
                } else {
                    if(checkTargetCart(cur.getRangeVal())) {
                        // 新目标出现
                        log.info("新目标出现: ", cartId.toString() );
                        fitCartMap.put(cartId.addAndGet(1), cur.getRangeVal());
                        if(fitCartMap.asMap().size() >= 2) {
                            launderCartTarget();
                        }
                    } else {
                        // 已有目标, 更新目标
                        log.info("先找目标, 然后更新, 更新模式");
                        updatePosition(cur.getRangeVal(), cur, fitCartMap);
                    }
                }
            } else {
                if(fitCarMap.size() == 0) {
                    carId.set(50);
                    fitCarMap.put(carId.addAndGet(1), cur.getRangeVal());
                    // 第一个目标
                } else {
                    if(checkTarget(cur.getRangeVal(), fitCarMap)) {
                        // 新目标出现
                        log.info("新目标出现: ", carId.toString() );
                        fitCarMap.put(carId.addAndGet(1), cur.getRangeVal());
                        if(fitCarMap.asMap().size() >= 2) {
                            launderCarTarget();
                        }
                    } else {
                        // 已有目标, 更新目标
                        log.info("先找目标, 然后更新, 更新模式");
                        updatePosition(cur.getRangeVal(), cur, fitCarMap);
                    }
                }
            }

        }
        if(cur.getTraceVal().intValue() == 3) {
            // 更新距离
            // 已经被跟踪的目标
            updatePosition(cur.getRangeVal(), cur, fitCarMap);
            updatePosition(cur.getRangeVal(), cur, fitCartMap);
        }
    }



    /**
     * 检查大车目标
     * @param range
     * @return
     */
    public static boolean checkTargetCart(Double range) {
        Optional<Entry<Integer, Double>> lastCur = fitCartMap.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? x: y);
        if(lastCur.isPresent()) {
            if((range -lastCur.get().getValue()) > 20) {
                return range < lastCur.get().getValue();
            }
        }
        return false;
    }

    /**
     * 检查是否为新目标
     * @return true: 新目标, false 已有目标
     */
    public static boolean checkTarget(Double range, Cache<Integer, Double> cache) {
        Optional<Entry<Integer, Double>> lastCur = cache.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? x: y);
        if(lastCur.isPresent()) {
            return range < lastCur.get().getValue();
        }
        return false;
    }

    /**
     * 找到 k , 并更新 range
     * @param range
     * @param cur
     * @return
     */
    public static void updatePosition(Double range, Target cur, Cache<Integer, Double> cache) {
        // 先找是属于哪边的车

        // 规约开始
        if(range > 150.0) {
            // 直接丢掉
            return;
        }
        Optional<Entry<Integer, Double>> max = cache.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? y: x);
        if(max.isPresent()) {
            double maxRange = max.get().getValue();
            int pos = max.get().getKey();
            if(range > maxRange) {
                cur.setCanId(pos);
                log.info(cur.toString());
                cache.put(pos, range);
                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
            }
        }
        cache.put(0, 0d);
        cache.asMap().entrySet().stream().reduce((x, y) -> {
            if (x.getValue() < range && range < y.getValue()) {
                // 更新目标位置
                if(x.getKey() != 0) {
                    cache.put(x.getKey(), range);
                    cur.setCanId(x.getKey());
                    log.info(cur.toString());
                    TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                }
            }
            return y;
        });
        cache.invalidate(0);
    }

    /**
     * 目标清洗: 移除假目标
     */
    public static void launderCartTarget() {
        List<Integer> mqp = new ArrayList<>(10);
        if(fitCartMap.asMap().size() > 2) {
            fitCartMap.asMap().entrySet().stream().reduce((x, y) -> {
                if(y.getValue() - x.getValue() < 10) {
                    mqp.add(x.getKey());
                }
                return y;
            });
        }
        mqp.forEach(i -> {
            fitCartMap.invalidate(i);
        });
    }

    public static void launderCarTarget() {
        List<Integer> mqp = new ArrayList<>(10);
        if(fitCarMap.asMap().size() > 2) {
            fitCarMap.asMap().entrySet().stream().reduce((x, y) -> {
                if(y.getValue() - x.getValue() < 20) {
                    mqp.add(x.getKey());
                }
                return y;
            });
        }
        mqp.forEach(i -> {
            fitCarMap.invalidate(i);
        });
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
