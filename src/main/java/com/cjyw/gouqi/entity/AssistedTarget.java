package com.cjyw.gouqi.entity;

/**
 * 辅助目标
 */
public class AssistedTarget {
    private int canId;
    private Double updateMode; // 目标更新模式
    private Double rangeAccelVal; // 目标俯仰角度
    private Double pitchAngleVal; // 径向加速度 m/s2
    private Double lateralVal; // 横向速度 m/s
    private Double frameNo; // 目标帧号

    public Double getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(Double frameNo) {
        this.frameNo = frameNo;
    }

    public int getCanId() {
        return canId;
    }

    public void setCanId(int canId) {
        this.canId = canId;
    }

    public Double getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(Double updateMode) {
        this.updateMode = updateMode;
    }

    public Double getRangeAccelVal() {
        return rangeAccelVal;
    }

    public void setRangeAccelVal(Double rangeAccelVal) {
        this.rangeAccelVal = rangeAccelVal;
    }

    public Double getPitchAngleVal() {
        return pitchAngleVal;
    }

    public void setPitchAngleVal(Double pitchAngleVal) {
        this.pitchAngleVal = pitchAngleVal;
    }

    public Double getLateralVal() {
        return lateralVal;
    }

    public void setLateralVal(Double lateralVal) {
        this.lateralVal = lateralVal;
    }

    public AssistedTarget(int canId, Double updateMode, Double rangeAccelVal, Double pitchAngleVal, Double lateralVal, Double frameNo) {
        this.canId = canId;
        this.updateMode = updateMode;
        this.rangeAccelVal = rangeAccelVal;
        this.pitchAngleVal = pitchAngleVal;
        this.lateralVal = lateralVal;
        this.frameNo = frameNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssistedTarget that = (AssistedTarget) o;

        if (canId != that.canId) return false;
        if (updateMode != null ? !updateMode.equals(that.updateMode) : that.updateMode != null) return false;
        if (rangeAccelVal != null ? !rangeAccelVal.equals(that.rangeAccelVal) : that.rangeAccelVal != null)
            return false;
        if (pitchAngleVal != null ? !pitchAngleVal.equals(that.pitchAngleVal) : that.pitchAngleVal != null)
            return false;
        if (lateralVal != null ? !lateralVal.equals(that.lateralVal) : that.lateralVal != null) return false;
        return frameNo != null ? frameNo.equals(that.frameNo) : that.frameNo == null;
    }

    @Override
    public int hashCode() {
        int result = canId;
        result = 31 * result + (updateMode != null ? updateMode.hashCode() : 0);
        result = 31 * result + (rangeAccelVal != null ? rangeAccelVal.hashCode() : 0);
        result = 31 * result + (pitchAngleVal != null ? pitchAngleVal.hashCode() : 0);
        result = 31 * result + (lateralVal != null ? lateralVal.hashCode() : 0);
        result = 31 * result + (frameNo != null ? frameNo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AssistedTarget{" +
                "canId=" + canId +
                ", updateMode=" + updateMode +
                ", rangeAccelVal=" + rangeAccelVal +
                ", pitchAngleVal=" + pitchAngleVal +
                ", lateralVal=" + lateralVal +
                ", frameNo=" + frameNo +
                '}';
    }
}
