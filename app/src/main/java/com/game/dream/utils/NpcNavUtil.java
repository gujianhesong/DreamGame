package com.game.dream.utils;

import com.game.dream.GameEngine;
import com.game.dream.figure.Player;
import com.game.dream.npc.Npc;

/**
 * 导航到 NPC 时计算玩家站立点（不与 NPC 重叠）。
 */
public final class NpcNavUtil {

    /** 人物与 NPC 轮廓之间额外留白（像素） */
    private static final float EXTRA_GAP = 56f;

    private NpcNavUtil() {
    }

    public static float[] npcCenter(Npc npc) {
        return new float[]{
                npc.getX() + npc.getSize() * 0.5f,
                npc.getY() + npc.getSize() * 0.5f
        };
    }

    public static float standOffDistance(Npc npc) {
        Player player = GameEngine.getInstance().getPlayer();
        int playerSize = player != null ? player.getSize() : 40;
        return npc.getSize() * 0.5f + playerSize * 0.5f + EXTRA_GAP;
    }

    /**
     * 在 NPC 外侧、沿「从 NPC 指向接近方向」放置站立点。
     *
     * @param approachFromX 通常用玩家当前 X，表示从哪一侧靠近
     * @param approachFromY 通常用玩家当前 Y
     */
    public static float[] standNearNpc(Npc npc, float approachFromX, float approachFromY) {
        if (npc == null) {
            return new float[]{approachFromX, approachFromY};
        }
        float[] center = npcCenter(npc);
        float cx = center[0];
        float cy = center[1];
        float standOff = standOffDistance(npc);

        float dx = approachFromX - cx;
        float dy = approachFromY - cy;
        float len = (float) Math.hypot(dx, dy);
        if (len < 1f) {
            dx = 0f;
            dy = 1f;
        } else {
            dx /= len;
            dy /= len;
        }

        float bestX = cx + dx * standOff;
        float bestY = cy + dy * standOff;
        if (MapWalkUtil.isWorldPositionReachable(bestX, bestY)) {
            return new float[]{bestX, bestY};
        }

        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4;
            float tx = cx + (float) Math.cos(ang) * standOff;
            float ty = cy + (float) Math.sin(ang) * standOff;
            if (MapWalkUtil.isWorldPositionReachable(tx, ty)) {
                return new float[]{tx, ty};
            }
        }

        for (float scale = 0.85f; scale >= 0.45f; scale -= 0.1f) {
            float tx = cx + dx * standOff * scale;
            float ty = cy + dy * standOff * scale;
            if (MapWalkUtil.isWorldPositionReachable(tx, ty)) {
                return new float[]{tx, ty};
            }
        }
        return new float[]{bestX, bestY};
    }
}