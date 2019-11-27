package com.cjyw.gouqi.entity;

public class Target {
    private int canId;
    private Long traceVal;
    private Double confidenceVal;
    private Double rangeVal;
    private Double angleVal;
    private Double rateVal;
    private Double powerVal;
    private Double frameVal;

    public int getCanId() {
        return canId;
    }

    public void setCanId(int canId) {
        this.canId = canId;
    }

    public Double getPowerVal() {
        return powerVal;
    }

    public void setPowerVal(Double powerVal) {
        this.powerVal = powerVal;
    }

    public Double getRateVal() {
        return rateVal;
    }

    public void setRateVal(Double rateVal) {
        this.rateVal = rateVal;
    }

    public Double getAngleVal() {
        return angleVal;
    }

    public void setAngleVal(Double angleVal) {
        this.angleVal = angleVal;
    }

    public Long getTraceVal() {
        return traceVal;
    }

    public void setTraceVal(Long traceVal) {
        this.traceVal = traceVal;
    }

    public Double getConfidenceVal() {
        return confidenceVal;
    }

    public void setConfidenceVal(Double confidenceVal) {
        this.confidenceVal = confidenceVal;
    }

    public Double getRangeVal() {
        return rangeVal;
    }

    public void setRangeVal(Double rangeVal) {
        this.rangeVal = rangeVal;
    }

    public Double getFrameVal() {
        return frameVal;
    }

    public void setFrameVal(Double frameVal) {
        this.frameVal = frameVal;
    }

    public Target(int canId, Long traceVal, Double confidenceVal, Double rangeVal, Double angleVal, Double rateVal, Double powerVal, Double frameVal) {
        this.canId = canId;
        this.traceVal = traceVal;
        this.confidenceVal = confidenceVal;
        this.rangeVal = rangeVal;
        this.angleVal = angleVal;
        this.rateVal = rateVal;
        this.powerVal = powerVal;
        this.frameVal = frameVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        if (canId != target.canId) return false;
        if (traceVal != null ? !traceVal.equals(target.traceVal) : target.traceVal != null) return false;
        if (confidenceVal != null ? !confidenceVal.equals(target.confidenceVal) : target.confidenceVal != null)
            return false;
        if (rangeVal != null ? !rangeVal.equals(target.rangeVal) : target.rangeVal != null) return false;
        if (angleVal != null ? !angleVal.equals(target.angleVal) : target.angleVal != null) return false;
        if (rateVal != null ? !rateVal.equals(target.rateVal) : target.rateVal != null) return false;
        if (powerVal != null ? !powerVal.equals(target.powerVal) : target.powerVal != null) return false;
        return frameVal != null ? frameVal.equals(target.frameVal) : target.frameVal == null;
    }

    @Override
    public int hashCode() {
        int result = canId;
        result = 31 * result + (traceVal != null ? traceVal.hashCode() : 0);
        result = 31 * result + (confidenceVal != null ? confidenceVal.hashCode() : 0);
        result = 31 * result + (rangeVal != null ? rangeVal.hashCode() : 0);
        result = 31 * result + (angleVal != null ? angleVal.hashCode() : 0);
        result = 31 * result + (rateVal != null ? rateVal.hashCode() : 0);
        result = 31 * result + (powerVal != null ? powerVal.hashCode() : 0);
        result = 31 * result + (frameVal != null ? frameVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Target{" +
                "canId=" + canId +
                ", traceVal=" + traceVal +
                ", confidenceVal=" + confidenceVal +
                ", rangeVal=" + rangeVal +
                ", angleVal=" + angleVal +
                ", rateVal=" + rateVal +
                ", powerVal=" + powerVal +
                ", frameVal=" + frameVal +
                '}';
    }
}
