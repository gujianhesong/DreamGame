package com.game.dream.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TitleProgressInfo {

    private String equippedTitleId;
    private List<String> unlockedTitleIds;
    private Map<String, Long> counters;
    private List<Integer> visitedMapIds;
    private Set<String> flags;

    public TitleProgressInfo() {
        unlockedTitleIds = new ArrayList<>();
        counters = new HashMap<>();
        visitedMapIds = new ArrayList<>();
        flags = new HashSet<>();
    }

    public String getEquippedTitleId() {
        return equippedTitleId;
    }

    public void setEquippedTitleId(String equippedTitleId) {
        this.equippedTitleId = equippedTitleId;
    }

    public List<String> getUnlockedTitleIds() {
        if (unlockedTitleIds == null) {
            unlockedTitleIds = new ArrayList<>();
        }
        return unlockedTitleIds;
    }

    public void setUnlockedTitleIds(List<String> unlockedTitleIds) {
        this.unlockedTitleIds = unlockedTitleIds;
    }

    public Map<String, Long> getCounters() {
        if (counters == null) {
            counters = new HashMap<>();
        }
        return counters;
    }

    public void setCounters(Map<String, Long> counters) {
        this.counters = counters;
    }

    public List<Integer> getVisitedMapIds() {
        if (visitedMapIds == null) {
            visitedMapIds = new ArrayList<>();
        }
        return visitedMapIds;
    }

    public void setVisitedMapIds(List<Integer> visitedMapIds) {
        this.visitedMapIds = visitedMapIds;
    }

    public Set<String> getFlags() {
        if (flags == null) {
            flags = new HashSet<>();
        }
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public long getCounter(String key) {
        if (key == null) {
            return 0;
        }
        Long v = getCounters().get(key);
        return v != null ? v : 0;
    }

    public void addCounter(String key, long delta) {
        if (key == null || delta == 0) {
            return;
        }
        long next = getCounter(key) + delta;
        getCounters().put(key, next);
    }

    public void setCounter(String key, long value) {
        if (key == null) {
            return;
        }
        getCounters().put(key, Math.max(0, value));
    }

    public boolean hasVisitedMap(int mapId) {
        return getVisitedMapIds().contains(mapId);
    }

    public void visitMap(int mapId) {
        if (!getVisitedMapIds().contains(mapId)) {
            getVisitedMapIds().add(mapId);
        }
    }

    public boolean hasFlag(String flag) {
        return flag != null && getFlags().contains(flag);
    }

    public void setFlag(String flag) {
        if (flag != null) {
            getFlags().add(flag);
        }
    }

    public boolean isUnlocked(String titleId) {
        return titleId != null && getUnlockedTitleIds().contains(titleId);
    }
}