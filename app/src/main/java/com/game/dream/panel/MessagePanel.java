package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.game.dream.utils.TouchUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 消息面板 - 显示经验、金钱、物品、升级等消息
 * 默认收起，点击左侧按钮展开
 */
public class MessagePanel {

    /** 消息类型枚举 */
    public enum MessageType {
        EXPERIENCE,    // 经验
        MONEY,         // 金钱
        ITEM,          // 物品
        LEVEL_UP,      // 升级
        SKILL_UP,      // 技能升级
        QUEST,         // 任务
        INFO           // 普通信息
    }

    /** 单条消息 */
    private static class MessageEntry {
        String text;
        int color;
        long timestamp;
        MessageType type;

        MessageEntry(String text, int color, MessageType type) {
            this.text = text;
            this.color = color;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // 消息列表
    private List<MessageEntry> messages = new ArrayList<>();

    // 最大消息数量
    private static final int MAX_MESSAGES = 1000;

    // 消息显示时长 (毫秒)
    private static final long MESSAGE_LIFETIME = 30000; // 30秒

    // 面板状态
    private boolean isExpanded = false;

    // 面板布局
    private Rect buttonBounds;       // 收起状态下的按钮
    private Rect panelBounds;        // 展开状态下的面板
    private Rect closeButtonBounds;  // 收起按钮
    private int panelWidth;
    private int panelHeight;
    private static final int CLOSE_BUTTON_SIZE = 50;

    // 滚动相关
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isScrolling = false;
    private boolean isCloseButtonPressed = false; // 关闭按钮按下状态

    // 新消息提示
    private boolean hasNewMessage = false;
    private long newMessageTime = 0;

    // 视觉参数
    private static final int BUTTON_SIZE = 80;
    private static final int BUTTON_MARGIN = 20;
    private static final int PADDING = 15;
    private static final int MESSAGE_HEIGHT = 45;
    private static final int TITLE_HEIGHT = 60;
    private static final float TEXT_SIZE = 30;
    private static final float TITLE_TEXT_SIZE = 35;

    // 动画
    private float expandAnimation = 0; // 0 = 收起, 1 = 展开
    private static final float ANIMATION_SPEED = 0.15f;

    // 时间格式化
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private Date timeDate = new Date();

    public MessagePanel() {
        buttonBounds = new Rect();
        panelBounds = new Rect();
        closeButtonBounds = new Rect();
    }

    /**
     * 设置位置 (屏幕左侧，占满全高，宽度为屏幕1/3)
     */
    public void setBounds(int screenWidth, int screenHeight) {
        // 按钮位置: 左侧中间偏上
        int buttonX = BUTTON_MARGIN;
        int buttonY = screenHeight / 3;
        buttonBounds.set(buttonX, buttonY, buttonX + BUTTON_SIZE, buttonY + BUTTON_SIZE);

        // 面板尺寸: 宽度1/3屏幕，高度占满
        panelWidth = screenWidth / 3;
        panelHeight = screenHeight;

        // 面板位置: 左侧占满全高
        int panelX = 0;
        int panelY = 0;
        panelBounds.set(panelX, panelY, panelX + panelWidth, panelY + panelHeight);

        // 收起按钮位置: 标题栏右侧
        int closeX = panelX + panelWidth - CLOSE_BUTTON_SIZE - 10;
        int closeY = panelY + (TITLE_HEIGHT - CLOSE_BUTTON_SIZE) / 2;
        closeButtonBounds.set(closeX, closeY, closeX + CLOSE_BUTTON_SIZE, closeY + CLOSE_BUTTON_SIZE);
    }

    /**
     * 添加消息
     */
    public void addMessage(String text, MessageType type) {
        int color = getColorForType(type);
        messages.add(new MessageEntry(text, color, type));

        // 限制消息数量
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }

        // 新消息提示
        hasNewMessage = true;
        newMessageTime = System.currentTimeMillis();

        // 如果展开状态，自动滚动到底部
        if (isExpanded) {
            scrollToBottom();
        }
    }

    /**
     * 添加消息 (自定义颜色)
     */
    public void addMessage(String text, int color) {
        messages.add(new MessageEntry(text, color, MessageType.INFO));

        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }

        hasNewMessage = true;
        newMessageTime = System.currentTimeMillis();

        if (isExpanded) {
            scrollToBottom();
        }
    }

    /**
     * 更新 (动画)
     */
    public void update() {
        // 更新展开/收起动画
        if (isExpanded && expandAnimation < 1) {
            expandAnimation = Math.min(1, expandAnimation + ANIMATION_SPEED);
        } else if (!isExpanded && expandAnimation > 0) {
            expandAnimation = Math.max(0, expandAnimation - ANIMATION_SPEED);
        }

        // 新消息提示动画 (3秒后消失)
        long now = System.currentTimeMillis();
        if (hasNewMessage && (now - newMessageTime) > 3000) {
            hasNewMessage = false;
        }
    }

    /**
     * 绘制
     */
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 绘制按钮 (始终显示)
        drawButton(canvas, paint);

        // 绘制面板 (展开时)
        if (expandAnimation > 0) {
            drawPanel(canvas, paint);
        }
    }

    /**
     * 绘制按钮
     */
    private void drawButton(Canvas canvas, Paint paint) {
        float centerX = buttonBounds.centerX();
        float centerY = buttonBounds.centerY();
        float radius = BUTTON_SIZE / 2f;

        // 按钮背景
        int bgColor = isExpanded ? Color.argb(200, 255, 140, 0) : Color.argb(150, 80, 80, 80);
        paint.setColor(bgColor);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // 按钮边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius - 2, paint);
        paint.setStyle(Paint.Style.FILL);

        // 按钮图标 (消息符号)
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("💬", centerX, centerY + 14, paint);

        // 新消息红点
        if (hasNewMessage && !isExpanded) {
            float dotX = centerX + radius * 0.6f;
            float dotY = centerY - radius * 0.6f;
            paint.setColor(Color.RED);
            canvas.drawCircle(dotX, dotY, 12, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(16);
            canvas.drawText("!", dotX, dotY + 5, paint);
        }
    }

    /**
     * 绘制面板
     */
    private void drawPanel(Canvas canvas, Paint paint) {
        // 计算动画后的实际尺寸 (从左侧滑入)
        float animatedWidth = panelWidth * expandAnimation;

        float left = panelBounds.left;
        float top = panelBounds.top;
        float right = left + animatedWidth;
        float bottom = panelBounds.bottom;

        // 面板背景
        paint.setColor(Color.argb((int)(200 * expandAnimation), 30, 30, 40));
        canvas.drawRoundRect(left, top, right, bottom, 15, 15, paint);

        // 面板边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb((int)(180 * expandAnimation), 100, 150, 255));
        canvas.drawRoundRect(left, top, right, bottom, 15, 15, paint);
        paint.setStyle(Paint.Style.FILL);

        // 只在动画完成后绘制内容
        if (expandAnimation > 0.9f) {
            // 裁剪区域
            canvas.save();
            canvas.clipRect(left, top, right, bottom);

            // 绘制标题
            drawTitle(canvas, paint, left, top, animatedWidth);

            // 绘制消息列表
            drawMessages(canvas, paint, left, top + TITLE_HEIGHT, animatedWidth, panelHeight - TITLE_HEIGHT);

            canvas.restore();
        }
    }

    /**
     * 绘制标题
     */
    private void drawTitle(Canvas canvas, Paint paint, float left, float top, float width) {
        // 标题背景
        paint.setColor(Color.argb(100, 50, 50, 70));
        canvas.drawRect(left, top, left + width, top + TITLE_HEIGHT, paint);

        // 标题文字 (居中偏左，给收起按钮留空间)
        paint.setColor(Color.WHITE);
        paint.setTextSize(TITLE_TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("消息记录", left + (width - CLOSE_BUTTON_SIZE) / 2, top + TITLE_HEIGHT / 2 + 10, paint);
        paint.setFakeBoldText(false);

        // 绘制收起按钮 (右侧 X 图标)
        drawCloseButton(canvas, paint);

        // 分隔线
        paint.setColor(Color.argb(100, 100, 150, 255));
        paint.setStrokeWidth(1);
        canvas.drawLine(left, top + TITLE_HEIGHT, left + width, top + TITLE_HEIGHT, paint);
    }

    /**
     * 绘制收起按钮
     */
    private void drawCloseButton(Canvas canvas, Paint paint) {
        float cx = closeButtonBounds.centerX();
        float cy = closeButtonBounds.centerY();
        float r = CLOSE_BUTTON_SIZE / 2f;

        // 按钮背景
        paint.setColor(Color.argb(80, 200, 80, 80));
        canvas.drawCircle(cx, cy, r, paint);

        // 按钮边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(180, 255, 100, 100));
        canvas.drawCircle(cx, cy, r - 2, paint);
        paint.setStyle(Paint.Style.FILL);

        // X 图标
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("✕", cx, cy + 12, paint);
    }

    /**
     * 绘制消息列表
     */
    private void drawMessages(Canvas canvas, Paint paint, float left, float top, float width, float height) {
        if (messages.isEmpty()) {
            // 空消息提示
            paint.setColor(Color.argb(150, 200, 200, 200));
            paint.setTextSize(TEXT_SIZE);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("暂无消息", left + width / 2, top + height / 2, paint);
            return;
        }

        // 计算可见区域
        float contentTop = top - scrollOffset;
        int startIndex = (int) (scrollOffset / MESSAGE_HEIGHT);
        int endIndex = Math.min(messages.size(), startIndex + (int)(height / MESSAGE_HEIGHT) + 2);

        for (int i = startIndex; i < endIndex; i++) {
            MessageEntry entry = messages.get(i);
            float y = contentTop + i * MESSAGE_HEIGHT + MESSAGE_HEIGHT / 2 + 8;

            // 检查是否在可见区域内
            if (y < top - MESSAGE_HEIGHT || y > top + height) continue;

            // 时间戳
            timeDate.setTime(entry.timestamp);
            String timeStr = timeFormat.format(timeDate);
            paint.setTextSize(TEXT_SIZE);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.WHITE);
            canvas.drawText(timeStr, left + PADDING, y, paint);

            // 消息类型图标
            String icon = getIconForType(entry.type);
            paint.setTextSize(24);
            paint.setColor(entry.color);
            canvas.drawText(icon, left + PADDING + 130, y, paint);

            // 消息文字
            paint.setTextSize(TEXT_SIZE);
            String displayText = entry.text;
            // 截断过长的文字
            float maxWidth = width - PADDING * 2 - 140;
            while (paint.measureText(displayText) > maxWidth && displayText.length() > 5) {
                displayText = displayText.substring(0, displayText.length() - 4) + "...";
            }
            paint.setColor(entry.color);
            canvas.drawText(displayText, left + PADDING + 165, y, paint);
        }

        // 更新最大滚动偏移
        float totalHeight = messages.size() * MESSAGE_HEIGHT;
        float visibleHeight = height;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);

        // 绘制滚动条 (如果内容超出)
        if (maxScrollOffset > 0) {
            float scrollbarHeight = height * (visibleHeight / totalHeight);
            float scrollbarY = top + (height - scrollbarHeight) * (scrollOffset / maxScrollOffset);
            paint.setColor(Color.argb(100, 150, 150, 200));
            canvas.drawRoundRect(left + width - 8, scrollbarY, left + width - 4, scrollbarY + scrollbarHeight, 2, 2, paint);
        }
    }

    /**
     * 处理触摸事件
     * @return true 如果事件被处理
     */
    public boolean handleTouch(int action, float x, float y) {
        // 检查是否点击了左侧消息按钮
        if (isPointInCircle(x, y, buttonBounds.centerX(), buttonBounds.centerY(), BUTTON_SIZE / 2f)) {
            if (action == android.view.MotionEvent.ACTION_DOWN) {
                return true;
            }
            if (action == android.view.MotionEvent.ACTION_UP) {
                toggleExpanded();
                return true;
            }
        }

        // 面板展开时处理面板触摸
        if (isExpanded && expandAnimation > 0.9f) {
            // 检查是否在关闭按钮区域内
            boolean inCloseButton = (x >= closeButtonBounds.left && x <= closeButtonBounds.right &&
                                     y >= closeButtonBounds.top && y <= closeButtonBounds.bottom);

            // 关闭按钮: ACTION_DOWN 记录状态，ACTION_UP 触发关闭
            if (inCloseButton) {
                if (action == android.view.MotionEvent.ACTION_DOWN) {
                    isCloseButtonPressed = true;
                    return true;
                }
            }

            // 检查是否在面板区域内
            if (x >= panelBounds.left && x <= panelBounds.right &&
                y >= panelBounds.top && y <= panelBounds.bottom) {

                switch (action) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        lastTouchY = y;
                        isScrolling = false;
                        return true;

                    case android.view.MotionEvent.ACTION_MOVE:
                        if (isCloseButtonPressed) return true; // 关闭按钮按下时不处理滚动
                        float deltaY = y - lastTouchY;
                        if (Math.abs(deltaY) > 5) {
                            isScrolling = true;
                            scrollOffset -= deltaY;
                            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
                            lastTouchY = y;
                        }
                        return true;

                    case android.view.MotionEvent.ACTION_UP:
                        if (isCloseButtonPressed) {
                            // 关闭按钮释放，收起面板
                            isCloseButtonPressed = false;
                            isExpanded = false;
                            return true;
                        }
                        isScrolling = false;
                        return true;
                }
            } else {
                // 手指移出面板区域，取消关闭按钮状态
                isCloseButtonPressed = false;
            }
        }

        return false;
    }

    /**
     * 切换展开/收起状态
     */
    private void toggleExpanded() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            scrollToBottom();
            hasNewMessage = false;
        }
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        float totalHeight = messages.size() * MESSAGE_HEIGHT;
        float visibleHeight = panelHeight - TITLE_HEIGHT;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = maxScrollOffset;
    }

    /**
     * 是否展开
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * 获取颜色
     */
    private int getColorForType(MessageType type) {
        switch (type) {
            case EXPERIENCE: return Color.rgb(100, 181, 246);  // 浅蓝
            case MONEY:      return Color.rgb(255, 215, 0);    // 金色
            case ITEM:       return Color.rgb(150, 255, 150);  // 绿色
            case LEVEL_UP:   return Color.rgb(255, 255, 100);  // 黄色
            case SKILL_UP:   return Color.rgb(200, 150, 255);  // 紫色
            case QUEST:      return Color.rgb(255, 180, 100);  // 橙色
            case INFO:       return Color.rgb(220, 220, 220);  // 白色
            default:         return Color.WHITE;
        }
    }

    /**
     * 获取图标
     */
    private String getIconForType(MessageType type) {
        switch (type) {
            case EXPERIENCE: return "✨";
            case MONEY:      return "💰";
            case ITEM:       return "🎁";
            case LEVEL_UP:   return "⬆️";
            case SKILL_UP:   return "📖";
            case QUEST:      return "📜";
            case INFO:       return "•";
            default:         return "•";
        }
    }

    /**
     * 判断点是否在圆内
     */
    private boolean isPointInCircle(float px, float py, float cx, float cy, float radius) {
        float dx = px - cx;
        float dy = py - cy;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
}
