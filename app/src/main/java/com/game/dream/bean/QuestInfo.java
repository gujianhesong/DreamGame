package com.game.dream.bean;

import java.util.List;

/**
 * 任务数据类
 * 用 stage 表示当前进度节点，stageDescs 描述各阶段内容
 */
public class QuestInfo {
    private int id;
    private String name;
    private String desc;
    private int stage;            // 当前阶段 (0 = 刚开始接取, 1 = 第一阶段完成, ...)
    private int totalStages;      // 总阶段数
    private List<String> stageDescs; // 各阶段描述
    private QuestStatus status;   // 任务状态
    private long acceptTime;      // 接取时间
    private long completeTime;    // 完成时间

    public enum QuestStatus {
        NOT_STARTED,   // 未接取
        ACCEPTED,      // 已接取(进行中)
        COMPLETED,     // 已完成
        FAILED         // 已失败
    }

    public QuestInfo() {
    }

    public QuestInfo(int id, String name, String desc, int totalStages, List<String> stageDescs) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.totalStages = totalStages;
        this.stageDescs = stageDescs;
        this.stage = 0;
        this.status = QuestStatus.NOT_STARTED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getTotalStages() {
        return totalStages;
    }

    public void setTotalStages(int totalStages) {
        this.totalStages = totalStages;
    }

    public List<String> getStageDescs() {
        return stageDescs;
    }

    public void setStageDescs(List<String> stageDescs) {
        this.stageDescs = stageDescs;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public long getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(long acceptTime) {
        this.acceptTime = acceptTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    /**
     * 获取当前阶段的描述
     */
    public String getCurrentStageDesc() {
        if (stageDescs == null || stage < 0 || stage >= stageDescs.size()) {
            return "";
        }
        return stageDescs.get(stage);
    }

    /**
     * 推进到下一阶段
     * @return true 如果任务完成
     */
    public boolean advanceStage() {
        stage++;
        if (stage >= totalStages) {
            status = QuestStatus.COMPLETED;
            completeTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * 接取任务
     */
    public void accept() {
        status = QuestStatus.ACCEPTED;
        stage = 0;
        acceptTime = System.currentTimeMillis();
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return status == QuestStatus.COMPLETED;
    }

    /**
     * 是否进行中
     */
    public boolean isActive() {
        return status == QuestStatus.ACCEPTED;
    }

    /**
     * 获取进度百分比 (0-1)
     */
    public float getProgress() {
        if (totalStages <= 0) return 0;
        return (float) stage / totalStages;
    }
}
