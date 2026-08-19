package com.game.dream.ui;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.game.dream.GameEngine;
import com.game.dream.utils.LogUtil;
import com.game.dream.map.Minimap;
import com.game.dream.figure.Player;
import com.game.dream.bean.SkillInfo;
import com.game.dream.panel.BuildEquipPanel;
import com.game.dream.panel.CraftingPanel;
import com.game.dream.panel.ItemsPanel;
import com.game.dream.panel.QuestPanel;
import com.game.dream.panel.RoleInfoPanel;
import com.game.dream.panel.ShopPanel;
import com.game.dream.panel.SkillsPanel;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

import com.game.dream.panel.MessagePanel;

public class GameUI {
    private int screenWidth;
    private int screenHeight;

    // Control buttons
    private Rect dpadBounds;

    // Minimap
    private Minimap minimap;

    private CenterNotification currentNotification;
    private CenterToast centerToast;

    private RoleInfoPanel roleInfoPanel;

    private ItemsPanel itemsPanel;

    private SkillsPanel skillsPanel;

    private BuildEquipPanel buildEquipPanel;

    private CraftingPanel craftingPanel;

    private QuestPanel questPanel;

    private ShopPanel shopPanel;

    private DialogBox currentDialog;

    // Message panel
    private MessagePanel messagePanel;

    // Attack buttons
    private Rect meleeAttackButton;

    // Skill buttons (Dynamic based on loadout)
    private List<Rect> skillButtons = new ArrayList<>();
    private Rect switchPageButton;
    private static final int MAX_SKILL_SLOTS = 5;

    private Rect roleInfoButton;
    private Rect equipmentButton;
    private Rect skillsButton;
    private Rect buildEquipButton;
    private Rect craftButton;
    private Rect questButton;

    // For tracking scroll gestures
    private float lastSkillsPanelTouchY = 0;
    private float lastItemsPanelTouchY = 0; // Add this line
    private float lastQuestPanelTouchY = 0;

    private boolean meleeAttackPressed;
    private boolean magicAttackPressed;

    // Track which pointer IDs are controlling the D-pad
    private Integer dpadPointerId = null;

    // Dash double-tap detection
    private int lastDpadTapDirection = -1;
    private long lastDpadTapTime = 0;
    private static final long DOUBLE_TAP_WINDOW = 300; // 300ms window for double-tap

    // FPS tracking
    private long lastFrameTime;
    private int frameCount;
    private float currentFPS;
    private long fpsUpdateTime;

    // Memory tracking (updated every 3 seconds)
    private float cachedMemoryMB = 0;
    private long lastMemoryUpdateTime = 0;
    private static final long MEMORY_UPDATE_INTERVAL = 3000; // 3 seconds

    public void initUI() {
        this.lastFrameTime = System.currentTimeMillis();
        this.frameCount = 0;
        this.currentFPS = 0;
        this.fpsUpdateTime = System.currentTimeMillis();

        // Initialize minimap
        minimap = new Minimap(GameEngine.getInstance().getMap(), GameEngine.MAP_WIDTH,
                GameEngine.MAP_HEIGHT, TILE_SIZE);
        minimap.initialize();

        // Initialize notification
        currentNotification = null;
        centerToast = null;

        // Initialize role info panel
        roleInfoPanel = new RoleInfoPanel(GameEngine.getInstance().getPlayer());

        // Initialize equipment panel
        itemsPanel = new ItemsPanel();

        // Initialize skills panel
        skillsPanel = new SkillsPanel();

        // Initialize Build Equip panel
        buildEquipPanel = new BuildEquipPanel();

        // Initialize Crafting panel
        craftingPanel = new CraftingPanel();

        // Initialize Quest panel
        questPanel = new QuestPanel();

        // Initialize Shop panel
        shopPanel = new ShopPanel();

        // Initialize DialogBox
        currentDialog = new DialogBox();

        // Initialize message panel
        messagePanel = new MessagePanel();
    }

    public void cleanup() {
        // Clean up minimap
        if (minimap != null) {
            minimap.cleanup();
        }
    }

    public void update() {
        updateFPS();

        // Update center notification
        if (currentNotification != null) {
            currentNotification.update();
            if (currentNotification.isExpired()) {
                currentNotification = null;
            }
        }

        // Update center toast
        if (centerToast != null) {
            centerToast.update();
            if (centerToast.isExpired()) {
                centerToast = null;
            }
        }

        // Update message panel
        if (messagePanel != null) {
            messagePanel.update();
        }
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;

        // Initialize control buttons
        initControlButtons();

        // Initialize role info panel (center of screen)
        if (roleInfoPanel != null) {
            int panelWidth = Math.min(600, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            roleInfoPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize equipment panel (center of screen)
        if (itemsPanel != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            itemsPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize skills panel (center of screen)
        if (skillsPanel != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            skillsPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize Build Equip panel (center of screen)
        if (buildEquipPanel != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            buildEquipPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize crafting panel (center of screen)
        if (craftingPanel != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            craftingPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize quest panel (center of screen)
        if (questPanel != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            questPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize shop panel (center of screen)
        if (shopPanel != null) {
            int panelWidth = Math.min(1000, width - 40);
            int panelHeight = Math.min(900, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            shopPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        if (currentDialog != null) {
            int panelWidth = Math.min(1200, width - 40);
            int panelHeight = Math.min(700, height - 100);
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            currentDialog.setBounds(panelX, panelY, panelWidth, panelHeight);
        }

        // Initialize message panel bounds
        if (messagePanel != null) {
            messagePanel.setBounds(width, height);
        }

    }

    private void initControlButtons() {
        // D-pad buttons (bottom-left) - Smaller size
        int buttonSize = screenHeight / 5;
        int padding = 20;

        // Calculate the center of the D-pad cross
        int dpadCenterX = (int) (padding + buttonSize * 1.5);
        int dpadCenterY = (int) (screenHeight - padding - buttonSize * 1.5);

        // D-pad bounds (entire control area)
        dpadBounds = new Rect(
                dpadCenterX - buttonSize,
                dpadCenterY - buttonSize,
                dpadCenterX + buttonSize,
                dpadCenterY + buttonSize
        );

        // Attack buttons cluster (bottom-right)
        int magicButtonSize = (int) (buttonSize * 0.8);
        int physicalButtonSize = (int) (buttonSize * 1.0);
        int attackPaddingX = 30;
        int attackPaddingY = screenHeight / 6;

        // Physical attack button center (bottom-right position)
        int physicalCenterX = screenWidth - attackPaddingX - physicalButtonSize;
        int physicalCenterY = screenHeight - attackPaddingY - physicalButtonSize;

        // Physical attack button (melee) - circular, larger
        meleeAttackButton = new Rect(
                physicalCenterX - physicalButtonSize / 2,
                physicalCenterY - physicalButtonSize / 2,
                physicalCenterX + physicalButtonSize / 2,
                physicalCenterY + physicalButtonSize / 2
        );

        {
            // Initialize 5 skill slots in an arc around the top-left of the attack button
            int attackBtnSize = meleeAttackButton.width(); // Assuming attack button size
            int skillBtnSize = (int) (buttonSize * 0.6);  // Slightly smaller than attack button
            padding = 20;

            // Attack button center position
            float attackCenterX = meleeAttackButton.centerX();
            float attackCenterY = meleeAttackButton.centerY();

            // Radius of the arc (distance from attack center to skill centers)
            float radius = attackBtnSize / 2 + skillBtnSize / 2 + 80;

            skillButtons.clear();
            for (int i = 0; i < MAX_SKILL_SLOTS; i++) {
                // Calculate angle: Distribute 5 buttons between 135 degrees (bottom-left) and 225 degrees (top-left)
                // Or simply from 180 (left) to 270 (top). Let's do 160 to 260 degrees for a nice left-side arc.
                // Angle in radians: startAngle + (step * i)
                double startAngleDeg = 140;
                double endAngleDeg = 280;
                double step = (endAngleDeg - startAngleDeg) / (MAX_SKILL_SLOTS - 1);
                double angleDeg = startAngleDeg + step * i;

                // Convert to radians and adjust for Android coordinate system (0 is right, 90 is down)
                // We want counter-clockwise from left-up.
                // Let's use standard math: x = cx + r * cos(a), y = cy + r * sin(a)
                double angleRad = Math.toRadians(angleDeg);

                int centerX = (int) (attackCenterX + radius * Math.cos(angleRad));
                int centerY = (int) (attackCenterY + radius * Math.sin(angleRad));

                Rect btn = new Rect(
                        centerX - skillBtnSize / 2,
                        centerY - skillBtnSize / 2,
                        centerX + skillBtnSize / 2,
                        centerY + skillBtnSize / 2
                );
                skillButtons.add(btn);
            }

            // Initialize Switch Page Button
            // Place it near the center of the attack button or slightly to the left
            int switchBtnSize = (int) (buttonSize * 0.5);

            // Position: To the left of the attack button, vertically centered with it
            int x = physicalCenterX - 80;
            int y = physicalCenterY + attackBtnSize / 2 + 30;

            switchPageButton = new Rect(x, y, x + switchBtnSize, y + switchBtnSize);
        }

        // role info button (top-right corner)
        int infoButtonSize = screenHeight / 10;
        int infoPadding = 20;

        int startX = screenWidth / 2 + infoPadding;
        roleInfoButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

        // Equipment button (next to role info button)
        startX += infoButtonSize + infoPadding;
        equipmentButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

        // Skills button (next to equipment button)
        startX += infoButtonSize + infoPadding;
        skillsButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

        // Build Equip button (next to Skills button)
        startX += infoButtonSize + infoPadding;
        buildEquipButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

        // Crafting button (next to Build Equip button)
        startX += infoButtonSize + infoPadding;
        craftButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

        // Quest button (next to Crafting button)
        startX += infoButtonSize + infoPadding;
        questButton = new Rect(
                startX,
                screenHeight - infoPadding - infoButtonSize,
                startX + infoButtonSize,
                screenHeight - infoPadding
        );

    }

    public void draw(Canvas canvas) {
        drawSomeInfo(canvas);

        drawControls(canvas);

        // Draw minimap
        if (minimap != null) {
            Player player = GameEngine.getInstance().getPlayer();
            minimap.draw(canvas, player.getX(), player.getY(), screenWidth, screenHeight);
        }

        // Draw role info panel
        if (roleInfoPanel != null) {
            roleInfoPanel.draw(canvas);
        }

        // Draw equipment panel
        if (itemsPanel != null && itemsPanel.isVisible()) {
            itemsPanel.draw(canvas);
        }

        // Draw skills panel
        if (skillsPanel != null && skillsPanel.isVisible()) {
            skillsPanel.draw(canvas);
        }

        // Draw buildEquipPanel panel
        if (buildEquipPanel != null && buildEquipPanel.isVisible()) {
            buildEquipPanel.draw(canvas);
        }

        // Draw craftingPanel panel
        if (craftingPanel != null && craftingPanel.isVisible()) {
            craftingPanel.draw(canvas);
        }

        // Draw questPanel panel
        if (questPanel != null && questPanel.isVisible()) {
            questPanel.draw(canvas);
        }

        // Draw ShopPanel
        if (shopPanel != null && shopPanel.isVisible()) {
            shopPanel.draw(canvas);
        }

        // Draw DialoBox
        if (currentDialog != null && currentDialog.isVisible()) {
            currentDialog.draw(canvas);
        }

        // Draw center notification (on top of everything except UI panels)
        if (currentNotification != null) {
            currentNotification.draw(canvas, GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
        }

        // Draw center toast (on top of everything except UI panels)
        if (centerToast != null) {
            centerToast.draw(canvas, GameEngine.getScreenWidth(), GameEngine.getScreenHeight());
        }

        // Draw message panel (always on top)
        if (messagePanel != null) {
            messagePanel.draw(canvas);
        }
    }

    public boolean handleTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        boolean handled = false;
        // Get the coordinates of the pointer that triggered this event
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        // Handle message panel touch (check first, before other panels)
        if (messagePanel != null && messagePanel.handleTouch(action, x, y)) {
            return true;
        }

        if (currentDialog != null && currentDialog.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN && currentDialog.handleTouch(x, y)) {
                return true; // currentDialog handled the touch (closed itself)
            }
        }

        // Handle Shop Panel
        if (shopPanel != null && shopPanel.isVisible()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return shopPanel.handleTouchDown(x, y);
                case MotionEvent.ACTION_MOVE:
                    return shopPanel.handleTouchMove(x, y);
                case MotionEvent.ACTION_UP:
                    return shopPanel.handleTouchUp(x, y);
            }
            return true;
        }

        // Check menu button clicks BEFORE any panel consumes the event
        // This allows switching panels via bottom menu even when a panel is open
        if (handleMenuButtonTouch(action, x, y)) {
            return true;
        }

        // If role info panel is visible, check if touching it first
        if (roleInfoPanel != null && roleInfoPanel.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN && roleInfoPanel.handleTouch(x, y)) {
                return true; // Panel handled the touch (closed itself)
            }
        }
        // If equipment panel is visible, check if touching it first
        if (itemsPanel != null && itemsPanel.isVisible()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return itemsPanel.handleTouchDown(x, y);
                case MotionEvent.ACTION_MOVE:
                    return itemsPanel.handleTouchMove(x, y);
                case MotionEvent.ACTION_UP:
                    return itemsPanel.handleTouchUp(x, y);
            }
            return true;
        }
        // If skills panel is visible, check if touching it first
        if (skillsPanel != null && skillsPanel.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastSkillsPanelTouchY = y;
                if (skillsPanel.handleTouch(x, y)) {
                    return true;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                float deltaY = y - lastSkillsPanelTouchY;
                if (Math.abs(deltaY) > 5) { // Minimum drag distance
                    skillsPanel.handleScroll(0, deltaY);
                    lastSkillsPanelTouchY = y;
                    return true;
                }
            }
            return true; // Consume all events when skills panel is open
        }

        // Handle Crafting Panel (Priority if visible)
        if (buildEquipPanel != null && buildEquipPanel.isVisible()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return buildEquipPanel.handleTouchDown(x, y);
                case MotionEvent.ACTION_MOVE:
                    return buildEquipPanel.handleTouchMove(x, y);
                case MotionEvent.ACTION_UP:
                    return buildEquipPanel.handleTouchUp(x, y);
            }
            return true;
        }

        // Handle Crafting Panel (Priority if visible)
        if (craftingPanel != null && craftingPanel.isVisible()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return craftingPanel.handleTouchDown(x, y);
                case MotionEvent.ACTION_MOVE:
                    return craftingPanel.handleTouchMove(x, y);
                case MotionEvent.ACTION_UP:
                    return craftingPanel.handleTouchUp(x, y);
            }
            return true;
        }

        // Handle Quest Panel (Priority if visible)
        if (questPanel != null && questPanel.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastQuestPanelTouchY = y;
                if (questPanel.handleTouch(x, y)) {
                    return true;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                float deltaY = y - lastQuestPanelTouchY;
                if (Math.abs(deltaY) > 5) {
                    questPanel.handleScroll(0, deltaY);
                    lastQuestPanelTouchY = y;
                    return true;
                }
            }
            return true; // Consume all events when quest panel is open
        }

        // Handle D-pad with pointer tracking
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Check if this pointer started on D-pad
                if (isInCircle(x, y, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width() / 2)) {
                    dpadPointerId = pointerId; // Lock this pointer to D-pad
                    handled = true;

                    // Calculate initial direction
                    float dx = x - dpadBounds.centerX();
                    float dy = y - dpadBounds.centerY();

                    int direction;
                    GameEngine.getInstance().updatePlayerDirection(false, false, false, false);

                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            direction = 3; // Right
                            GameEngine.getInstance().updatePlayerDirection(false, false, false, true);
                        } else {
                            direction = 2; // Left
                            GameEngine.getInstance().updatePlayerDirection(false, false, true, false);
                        }
                    } else {
                        if (dy > 0) {
                            direction = 0; // Down
                            GameEngine.getInstance().updatePlayerDirection(false, true, false, false);
                        } else {
                            direction = 1; // Up
                            GameEngine.getInstance().updatePlayerDirection(true, false, false, false);
                        }
                    }

                    // Check for double-tap dash trigger
                    long currentTime = System.currentTimeMillis();
                    if (direction == lastDpadTapDirection && (currentTime - lastDpadTapTime) < DOUBLE_TAP_WINDOW) {
                        // Double-tap detected - trigger dash!
                        GameEngine.getInstance().triggerPlayerDash(direction);
                        lastDpadTapDirection = -1;
                        lastDpadTapTime = 0;
                    } else {
                        lastDpadTapDirection = direction;
                        lastDpadTapTime = currentTime;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // If this is the D-pad pointer, update direction regardless of position
                if (dpadPointerId != null && pointerId == dpadPointerId) {
                    handled = true;

                    float dx = x - dpadBounds.centerX();
                    float dy = y - dpadBounds.centerY();

                    GameEngine.getInstance().updatePlayerDirection(false, false, false, false);

                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            GameEngine.getInstance().updatePlayerDirection(false, false, false, true);
                        } else {
                            GameEngine.getInstance().updatePlayerDirection(false, false, true, false);
                        }
                    } else {
                        if (dy > 0) {
                            GameEngine.getInstance().updatePlayerDirection(false, true, false, false);
                        } else {
                            GameEngine.getInstance().updatePlayerDirection(true, false, false, false);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // If this is the D-pad pointer, release it
                if (dpadPointerId != null && pointerId == dpadPointerId) {
                    dpadPointerId = null; // Release the lock
                    GameEngine.getInstance().updatePlayerDirection(false, false, false, false);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                // Cancel all input
                dpadPointerId = null;
                GameEngine.getInstance().updatePlayerDirection(false, false, false, false);
                break;
        }

        // Handle attack buttons based on event type
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Check if this pointer is on any attack button
                if (meleeAttackButton != null &&
                        isPointInCircle(x, y, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                    meleeAttackPressed = true;
                    handled = true;

                    //触发攻击
                    GameEngine.getInstance().doAttackAction();
                }

                for (int index = 0; index < skillButtons.size(); index++) {
                    Rect skillBtn = skillButtons.get(index);
                    if (isPointInCircle(x, y, skillBtn.centerX(), skillBtn.centerY(), skillBtn.width() / 2)) {
                        magicAttackPressed = true;
                        handled = true;

                        int finalIndex = SkillSystem.getInstance().getSkillIndex(index);
                        List<SkillInfo> equipped = SkillSystem.getInstance().getEquippedActiveSkills();
                        if (finalIndex < equipped.size()) {
                            //触发法术
                            GameEngine.getInstance().doCasterAction(equipped.get(finalIndex));
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // Clear the state for the pointer that lifted
                // Check which button this pointer was on
                if (meleeAttackButton != null &&
                        isPointInCircle(x, y, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                    meleeAttackPressed = false;
                    handled = true;
                }

                for (int index = 0; index < skillButtons.size(); index++) {
                    Rect skillBtn = skillButtons.get(index);
                    if (isPointInCircle(x, y, skillBtn.centerX(), skillBtn.centerY(), skillBtn.width() / 2)) {
                        magicAttackPressed = false;
                        handled = true;
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                // For MOVE events, we still need to track if pointers are on buttons
                // to maintain correct visual state when finger moves in/out of button area
                boolean anyOnButton = false;

                for (int i = 0; i < event.getPointerCount(); i++) {
                    float px = event.getX(i);
                    float py = event.getY(i);

                    if (meleeAttackButton != null &&
                            isPointInCircle(px, py, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                        anyOnButton = true;
                        if (!meleeAttackPressed) {
                            meleeAttackPressed = true;
                            handled = true;
                        }
                    }

                    for (int index = 0; index < skillButtons.size(); index++) {
                        Rect skillBtn = skillButtons.get(index);
                        if (isPointInCircle(x, y, skillBtn.centerX(), skillBtn.centerY(), skillBtn.width() / 2)) {
                            anyOnButton = true;
                            if (!magicAttackPressed) {
                                magicAttackPressed = true;
                                handled = true;
                            }
                        }
                    }
                }

                // If no pointers are on any button, clear all states
                if (!anyOnButton) {
                    if (meleeAttackPressed || magicAttackPressed) {
                        meleeAttackPressed = false;
                        magicAttackPressed = false;
                        handled = true;
                    }
                }
                break;
        }

        return handled;
    }

    private void drawSomeInfo(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

        /*// Draw player health bar (top-left, below other info)
        float healthBarWidth = 200;
        float healthBarHeight = 20;
        float healthBarX = 10;
        float healthBarY = 110;

        // Background
        paint.setColor(Color.BLACK);
        canvas.drawRect(healthBarX - 2, healthBarY - 2,
                healthBarX + healthBarWidth + 2,
                healthBarY + healthBarHeight + 2, paint);

        // Health fill
        float healthPercent = player.getHealthPercent();
        int healthColor;
        if (healthPercent > 0.6f) {
            healthColor = Color.GREEN;
        } else if (healthPercent > 0.3f) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.RED;
        }

        paint.setColor(healthColor);
        canvas.drawRect(healthBarX, healthBarY,
                healthBarX + healthBarWidth * healthPercent,
                healthBarY + healthBarHeight, paint);

        // Health text
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(player.getHealth() + " / " + player.getMaxHealth(),
                healthBarX + healthBarWidth / 2,
                healthBarY + 15, paint);

        // Invincibility indicator
        if (player.isCurrentlyInvincible()) {
            paint.setColor(Color.YELLOW);
            paint.setTextSize(16);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("✨ INVINCIBLE", healthBarX, healthBarY + 40, paint);
        }*/

        // Draw player level and experience (top-left)
        int expBarWidth = 200;
        int expBarHeight = 20;
        int expBarX = 10;
        int expBarY = 35;

        // Level text
        paint.setColor(Color.rgb(255, 215, 0)); // Gold color
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Lv." + RoleSystem.getInstance().getRoleInfo().getLevel(), expBarX, expBarY + 7, paint);

        // Experience bar background
        paint.setColor(Color.BLACK);
        canvas.drawRect(expBarX + 70, expBarY - 12,
                expBarX + 70 + expBarWidth + 2,
                expBarY - 12 + expBarHeight + 2, paint);

        // Experience bar fill
        float expProgress = RoleSystem.getInstance().getRoleInfo().getExp() * 1f / RoleSystem.getInstance().getExpForNextLevel();
        paint.setColor(Color.rgb(100, 181, 246)); // Blue
        canvas.drawRect(expBarX + 70, expBarY - 12,
                expBarX + 70 + expBarWidth * expProgress,
                expBarY - 12 + expBarHeight, paint);

        // EXP text
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(RoleSystem.getInstance().getRoleInfo().getExp() + "/" + RoleSystem.getInstance().getExpForNextLevel(),
                expBarX + 70 + expBarWidth / 2,
                expBarY + 5, paint);

        paint.setTextSize(30);
        // Draw FPS (top-right corner)
        paint.setTextAlign(Paint.Align.LEFT);
        if (currentFPS >= 55) {
            paint.setColor(Color.GREEN);
        } else if (currentFPS >= 30) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.RED);
        }
        canvas.drawText("FPS: " + String.format("%.1f", currentFPS), 10, 80, paint);
        paint.setColor(Color.WHITE);
        //canvas.drawText("Memory: " + String.format("%.1f", getUsedMemoryMB()) + " MB", 10, 120, paint);

        // Draw coordinates (top-left)
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        Player player = GameEngine.getInstance().getPlayer();
        canvas.drawText("Position: (" + (int) player.getX() + ", " + (int) player.getY() + ")", 10, 160, paint);

//        // Draw terrain info
//        int playerGridX = (int) (player.getX() / TILE_SIZE);
//        int playerGridY = (int) (player.getY() / TILE_SIZE);
//        String terrainName = MapGenerator.getTerrainName(map[playerGridY][playerGridX]);
//        canvas.drawText("Terrain: " + terrainName, 10, 160, paint);
//
//        // Draw chunk cache info (for debugging)
//        paint.setColor(Color.CYAN);
//        canvas.drawText("Chunks: " + mapRenderer.getCachedChunkCount(), 10, 200, paint);
//        canvas.drawText("Active: " + mapRenderer.getActiveChunkCount(), 10, 240, paint);

        // Draw time info
        if (GameEngine.getInstance().getDayNightCycle() != null) {
            paint.setColor(Color.rgb(255, 255, 200)); // Light yellow
            canvas.drawText(GameEngine.getInstance().getDayNightCycle().getTimePhase(), 10, 200, paint);
        }

        // Draw weather info
        if (GameEngine.getInstance().getWeatherSystem() != null) {
            paint.setColor(Color.rgb(200, 220, 255)); // Light blue
            canvas.drawText("Weather: " + GameEngine.getInstance().getWeatherSystem().getWeatherDescription(), 10, 240, paint);
        }
    }

    private void drawControls(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw D-pad background
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawCircle(dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width() / 2, paint);

        // Draw D-pad buttons
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("▲", dpadBounds.centerX(), dpadBounds.centerY() - dpadBounds.height() / 4 + 15, paint);
        canvas.drawText("▼", dpadBounds.centerX(), dpadBounds.centerY() + dpadBounds.height() / 4 + 15, paint);
        canvas.drawText("◀", dpadBounds.centerX() - dpadBounds.width() / 4, dpadBounds.centerY() + 15, paint);
        canvas.drawText("▶", dpadBounds.centerX() + dpadBounds.width() / 4, dpadBounds.centerY() + 15, paint);

        // Draw dash cooldown indicator (top-left of D-pad)
        drawDashCooldownIndicator(canvas, paint);

        // Draw attack buttons cluster
        if (meleeAttackButton != null) {
            // Draw connection lines from magic buttons to physical button
            paint.setColor(Color.argb(60, 255, 255, 255));
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);

            // Draw physical attack button (circular, larger and more prominent)
            drawCircularPhysicalAttackButton(canvas, meleeAttackButton, meleeAttackPressed);
        }

        List<SkillInfo> equipped = SkillSystem.getInstance().getCurrentPageSkills();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < MAX_SKILL_SLOTS; i++) {
            Rect btn = skillButtons.get(i);
            boolean hasSkill = i < equipped.size();

            // Button Background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(hasSkill ? Color.argb(100, 200, 200, 50) : Color.argb(10, 200, 200, 50));
            canvas.drawCircle(btn.centerX(), btn.centerY(), btn.width() / 2, paint);

            // Border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(btn.centerX(), btn.centerY(), btn.width() / 2, paint);

            // Skill Name or Empty Slot Label
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(22);
            paint.setColor(Color.WHITE);
            if (hasSkill) {
                SkillInfo skillInfo = equipped.get(i);
                String name = skillInfo.getName();
                if (name.length() > 4) name = name.substring(0, 4) + "..";
                canvas.drawText(name, btn.centerX(), btn.centerY() + 10, paint);

                // Draw cooldown overlay
                float cooldownProgress = GameEngine.getInstance().getPlayer().getMagicCooldownProgress(skillInfo.getSkillType());
                if (cooldownProgress < 1.0f) {
                    drawCircularCooldown(canvas, btn, cooldownProgress);
                }
            } else {
                canvas.drawText("空", btn.centerX(), btn.centerY() + 10, paint);
            }
        }

        // Draw Switch Page Button
        boolean hasMorePages = SkillSystem.getInstance().getCurrentPageSkills().size() > 0 &&
                (SkillSystem.getInstance().getCurrentPageIndex() + 1) * 5 < SkillSystem.getInstance().getEquippedActiveSkills().size();

        paint.setColor(Color.argb(100, 255, 140, 0)); // Orange
        canvas.drawCircle(switchPageButton.centerX(), switchPageButton.centerY(), switchPageButton.width() / 2, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(switchPageButton.centerX(), switchPageButton.centerY(), switchPageButton.width() / 2 - 1, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        // Show arrow or page number
        canvas.drawText("⇄", switchPageButton.centerX(), switchPageButton.centerY() + 8, paint);

        // Draw role info button
        if (roleInfoButton != null) {
            drawMenuButton("👤", canvas, roleInfoButton, roleInfoPanel.isVisible());
        }

        // Draw equipment button
        if (equipmentButton != null) {
            drawMenuButton("📦", canvas, equipmentButton, itemsPanel != null && itemsPanel.isVisible());
        }

        // Draw skills button
        if (skillsButton != null) {
            drawMenuButton("⭐", canvas, skillsButton, skillsPanel != null && skillsPanel.isVisible());
        }

        // Draw build equip button
        if (buildEquipButton != null) {
            drawMenuButton("🔨", canvas, buildEquipButton, buildEquipPanel != null && buildEquipPanel.isVisible());
        }

        // Draw craft button
        if (craftButton != null) {
            drawMenuButton("🔥", canvas, craftButton, craftingPanel != null && craftingPanel.isVisible());
        }

        // Draw quest button
        if (questButton != null) {
            drawMenuButton("📜", canvas, questButton, questPanel != null && questPanel.isVisible());
        }
    }

    /**
     * Draw circular physical attack button (larger and more prominent)
     */
    private void drawCircularPhysicalAttackButton(Canvas canvas, Rect button, boolean pressed) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Button background (circular, red gradient effect)
        if (pressed) {
            paint.setColor(Color.argb(120, 255, 80, 80));
        } else {
            paint.setColor(Color.argb(80, 220, 60, 60));
        }
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Outer glow effect
        paint.setColor(Color.argb(80, 255, 100, 100));
        canvas.drawCircle(centerX, centerY, radius + 5, paint);

        // Border (circular, thicker)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Inner circle for depth
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 255, 255, 255));
        canvas.drawCircle(centerX, centerY, radius * 0.7f, paint);

        // Label (larger icon)
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = centerY + 17;
        canvas.drawText("⚔️", centerX, textY, paint);

        // Draw cooldown overlay
        float cooldownProgress = GameEngine.getInstance().getPlayer().getAttackCooldownProgress();
        if (cooldownProgress < 1.0f) {
            drawCircularCooldown(canvas, button, cooldownProgress);
        }
    }

    /**
     * Draw circular cooldown overlay
     */
    private void drawCircularCooldown(Canvas canvas, Rect button, float progress) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Dark overlay based on cooldown progress
        int alpha = (int) (180 * (1 - progress)); // More opaque when cooling down
        paint.setColor(Color.argb(alpha, 0, 0, 0));

        // Draw arc from top, clockwise
        RectF oval = new RectF(button.left, button.top, button.right, button.bottom);
        float sweepAngle = 360 * (1 - progress); // Remaining cooldown

        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(oval, -90, sweepAngle, true, paint);

        // Optional: Draw border for the cooldown arc
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(100, 255, 255, 255));
        canvas.drawArc(oval, -90, sweepAngle, false, paint);
    }

    /**
     * Draw dash charge indicator at top-left of D-pad
     */
    private void drawDashCooldownIndicator(Canvas canvas, Paint paint) {
        Player player = GameEngine.getInstance().getPlayer();
        if (player == null) return;

        float charge = player.getDashCharge();
        float maxCharge = player.getDashChargeMax();
        float cost = player.getDashChargeCost();
        float chargeRatio = charge / maxCharge;
        boolean canDash = charge >= cost;

        // Position: top-left of D-pad
        float dpadRadius = dpadBounds.width() / 2f;
        float indicatorRadius = dpadRadius * 0.2f;
        float indicatorCX = dpadBounds.centerX() - dpadRadius * 1.15f;
        float indicatorCY = dpadBounds.centerY() - dpadRadius * 0.65f;

        // Background circle (dark)
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(100, 0, 0, 0));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(indicatorCX, indicatorCY, indicatorRadius, paint);

        // Charge arc (fills clockwise from top based on charge ratio)
        float sweepAngle = 360 * chargeRatio;
        RectF oval = new RectF(
                indicatorCX - indicatorRadius,
                indicatorCY - indicatorRadius,
                indicatorCX + indicatorRadius,
                indicatorCY + indicatorRadius);

        // Color based on whether can dash
        int arcColor;
        if (canDash) {
            // Cyan/green when enough charge
            arcColor = Color.argb(140, 0, 220, 255);
        } else {
            // Red/orange when low charge
            arcColor = Color.argb(140, 255, 100, 50);
        }
        paint.setColor(arcColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(oval, -90, sweepAngle, true, paint);

        // Outer ring
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(canDash ? Color.argb(180, 0, 200, 255) : Color.argb(180, 255, 80, 30));
        canvas.drawCircle(indicatorCX, indicatorCY, indicatorRadius, paint);

        // Charge number in center
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(230, 255, 255, 255));
        paint.setTextSize(indicatorRadius * 0.65f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(String.valueOf((int) charge), indicatorCX, indicatorCY + indicatorRadius * 0.25f, paint);
    }

    /**
     * Draw menu button
     */
    private void drawMenuButton(String iconTxt, Canvas canvas, Rect button, boolean isActive) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Button background
        if (isActive) {
            paint.setColor(Color.argb(120, 255, 140, 0));
        } else {
            paint.setColor(Color.argb(70, 200, 200, 200));
        }
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 10, 10, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 9, 9, paint);

        // Icon
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textX = button.centerX();
        float textY = button.centerY() + 20;
        canvas.drawText(iconTxt, textX, textY, paint);
    }

    /**
     * Show center toast
     */
    public void showCenterToast(String message, long durationMillis) {
        centerToast = new CenterToast(message, durationMillis);
    }

    /**
     * Show a center screen notification
     */
    public void showNotification(String title, String message, CenterNotification.Type type) {
        currentNotification = new CenterNotification(title, message, type);
        LogUtil.d("GameEngine", "Notification: " + title + " - " + message);
    }

    public void showShopPanel(List<ShopPanel.ShopItem> shopItems) {
        if (shopPanel != null) {
            shopPanel.showShopItems(shopItems);
        }
    }

    public DialogBox showDialog(String title, String msg, List<String> options, DialogBox.DialogListener listener) {
        if (currentDialog != null) {
            currentDialog.show(title, msg, options, listener);
            return currentDialog;
        }
        return null;
    }

    /**
     * Check if a point is within a circle
     */
    private boolean isPointInCircle(float px, float py, float centerX, float centerY, float radius) {
        float dx = px - centerX;
        float dy = py - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    private boolean isInCircle(float x, float y, float centerX, float centerY, float radius) {
        float dx = x - centerX;
        float dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    /**
     * Handle menu button touches. Called before panel checks so that
     * clicking bottom menu buttons works even when a panel is open.
     */
    private boolean handleMenuButtonTouch(int action, float x, float y) {
        // Check Switch Page Button
        if (TouchUtil.checkIsInTouchRectFloat(switchPageButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN) {
                SkillSystem.getInstance().nextPage();
                int page = SkillSystem.getInstance().getCurrentPageIndex() + 1;
                showNotification("提示", "技能第 " + page + " 页", CenterNotification.Type.INFO);
                return true;
            }
        }

        // Check role info button
        if (roleInfoButton != null && TouchUtil.checkIsInTouchRectFloat(roleInfoButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (roleInfoPanel != null) {
                    boolean wasVisible = roleInfoPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) roleInfoPanel.show();
                }
                return true;
            }
        }

        // Check equipment button
        if (equipmentButton != null && TouchUtil.checkIsInTouchRectFloat(equipmentButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (itemsPanel != null) {
                    boolean wasVisible = itemsPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) itemsPanel.show();
                }
                return true;
            }
        }

        // Check skills button
        if (skillsButton != null && TouchUtil.checkIsInTouchRectFloat(skillsButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (skillsPanel != null) {
                    boolean wasVisible = skillsPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) skillsPanel.show();
                }
                return true;
            }
        }

        // Check Build Equip button
        if (buildEquipButton != null && TouchUtil.checkIsInTouchRectFloat(buildEquipButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (buildEquipPanel != null) {
                    boolean wasVisible = buildEquipPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) buildEquipPanel.show();
                }
                return true;
            }
        }

        // Check Crafting button
        if (craftButton != null && TouchUtil.checkIsInTouchRectFloat(craftButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (craftingPanel != null) {
                    boolean wasVisible = craftingPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) craftingPanel.show();
                }
                return true;
            }
        }

        // Check Quest button
        if (questButton != null && TouchUtil.checkIsInTouchRectFloat(questButton, x, y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (questPanel != null) {
                    boolean wasVisible = questPanel.isVisible();
                    closeAllPanels();
                    if (!wasVisible) questPanel.show();
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Close all open panels
     */
    private void closeAllPanels() {
        if (roleInfoPanel != null) roleInfoPanel.hide();
        if (itemsPanel != null) itemsPanel.hide();
        if (skillsPanel != null) skillsPanel.hide();
        if (buildEquipPanel != null) buildEquipPanel.hide();
        if (craftingPanel != null) craftingPanel.hide();
        if (questPanel != null) questPanel.hide();
        if (shopPanel != null) shopPanel.hide();
    }

    /**
     * 添加消息到消息面板
     */
    public void addMessage(String text, MessagePanel.MessageType type) {
        if (messagePanel != null) {
            messagePanel.addMessage(text, type);
        }
    }

    /**
     * 添加消息到消息面板 (自定义颜色)
     */
    public void addMessage(String text, int color) {
        if (messagePanel != null) {
            messagePanel.addMessage(text, color);
        }
    }

    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - fpsUpdateTime;

        // Update FPS every second
        if (elapsed >= 1000) {
            currentFPS = (frameCount * 1000f) / elapsed;
            frameCount = 0;
            fpsUpdateTime = currentTime;
        }
    }

    private float getUsedMemoryMB() {
        long currentTime = System.currentTimeMillis();

        // Only update every 3 seconds
        if (currentTime - lastMemoryUpdateTime >= MEMORY_UPDATE_INTERVAL) {
            android.os.Debug.MemoryInfo memoryInfo = new android.os.Debug.MemoryInfo();
            android.os.Debug.getMemoryInfo(memoryInfo);

            // getTotalPss() returns memory in KB
            long totalPssKB = memoryInfo.getTotalPss();

            // Convert to MB and cache
            cachedMemoryMB = totalPssKB / 1024f;
            lastMemoryUpdateTime = currentTime;
        }

        return cachedMemoryMB;
    }
}
