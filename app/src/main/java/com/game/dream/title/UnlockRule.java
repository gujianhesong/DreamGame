package com.game.dream.title;

import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.TitleProgressInfo;

/**
 * 自动解锁条件（满足全部非空字段）。
 */
public class UnlockRule {

    public final Integer visitMapId;
    public final String counterKey;
    public final long counterMin;
    public final Integer minLevel;
    public final String requireFlag;

    private UnlockRule(Builder b) {
        this.visitMapId = b.visitMapId;
        this.counterKey = b.counterKey;
        this.counterMin = b.counterMin;
        this.minLevel = b.minLevel;
        this.requireFlag = b.requireFlag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSatisfied(TitleProgressInfo progress, RoleInfo role) {
        if (progress == null || role == null) {
            return false;
        }
        if (visitMapId != null && !progress.hasVisitedMap(visitMapId)) {
            return false;
        }
        if (counterKey != null && progress.getCounter(counterKey) < counterMin) {
            return false;
        }
        if (minLevel != null && role.getLevel() < minLevel) {
            return false;
        }
        if (requireFlag != null && !progress.hasFlag(requireFlag)) {
            return false;
        }
        return true;
    }

    public static final class Builder {
        private Integer visitMapId;
        private String counterKey;
        private long counterMin;
        private Integer minLevel;
        private String requireFlag;

        public Builder map(int mapId) {
            this.visitMapId = mapId;
            return this;
        }

        public Builder counter(String key, long min) {
            this.counterKey = key;
            this.counterMin = min;
            return this;
        }

        public Builder level(int min) {
            this.minLevel = min;
            return this;
        }

        public Builder flag(String key) {
            this.requireFlag = key;
            return this;
        }

        public UnlockRule build() {
            return new UnlockRule(this);
        }
    }
}