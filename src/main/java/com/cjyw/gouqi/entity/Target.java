package com.cjyw.gouqi.entity;

import java.util.Objects;


public class Target {
    private Long traceVal;
    private Long confidenceVal;
    private Long rangeVal;

    public Long getTraceVal() {
        return traceVal;
    }

    public void setTraceVal(Long traceVal) {
        this.traceVal = traceVal;
    }

    public Long getConfidenceVal() {
        return confidenceVal;
    }

    public void setConfidenceVal(Long confidenceVal) {
        this.confidenceVal = confidenceVal;
    }

    public Long getRangeVal() {
        return rangeVal;
    }

    public void setRangeVal(Long rangeVal) {
        this.rangeVal = rangeVal;
    }

    public Target(Long traceVal, Long confidenceVal, Long rangeVal) {
        this.traceVal = traceVal;
        this.confidenceVal = confidenceVal;
        this.rangeVal = rangeVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(traceVal, target.traceVal) &&
                Objects.equals(confidenceVal, target.confidenceVal) &&
                Objects.equals(rangeVal, target.rangeVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceVal, confidenceVal, rangeVal);
    }

    @Override
    public String toString() {
        return "Target{" +
                "traceVal=" + traceVal +
                ", confidenceVal=" + confidenceVal +
                ", rangeVal=" + rangeVal +
                '}';
    }
}
