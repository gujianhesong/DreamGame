package com.game.dream.system;

import android.content.Context;
import android.content.SharedPreferences;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.bean.TreasureMapItemInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SaveInfo;
import com.game.dream.bean.SaveSlotInfo;
import com.game.dream.utils.LogUtil;
import com.game.dream.utils.StorageHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SaveSystem {

    private static SaveSystem instance = new SaveSystem();

    public static SaveSystem getInstance() {
        return instance;
    }

    private static final String LEGACY_SAVE_FILE = "save.txt";
    private static final String SAVES_DIR = "saves";
    private static final String PREF_NAME = "dream_saves";
    private static final String PREF_CURRENT_ID = "current_save_id";

    private SaveInfo saveInfo;
    private String currentSaveId;
    private Gson gson;

    private SaveSystem() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ItemInfo.class, new ItemDeserializer())
                .create();
    }

    public String getCurrentSaveId() {
        return currentSaveId;
    }

    /**
     * 写入当前档。没有当前档时会新建一档。
     */
    public void save(Context context) {
        try {
            migrateLegacySave(context);
            ensureCurrentSaveId(context);

            SaveInfo saveInfo = new SaveInfo();
            saveInfo.setSaveId(currentSaveId);
            saveInfo.setUpdatedAt(System.currentTimeMillis());
            saveInfo.setRoleInfo(RoleSystem.getInstance().getRoleInfo());
            saveInfo.setItemInfos(ItemSystem.getInstance().getItemInfos());
            saveInfo.setEquipInfos(ItemSystem.getInstance().getEquipInfos());
            saveInfo.setMainSkillInfos(SkillSystem.getInstance().getMainSkillInfos());
            saveInfo.setAssistSkillInfos(SkillSystem.getInstance().getAssistSkillInfos());
            saveInfo.setPractiseSkillInfos(SkillSystem.getInstance().getPractiseSkillInfos());
            saveInfo.setEquipedSkillInfos(SkillSystem.getInstance().getEquippedActiveSkills());
            saveInfo.setQuestInfos(QuestSystem.getInstance().getAcceptedQuests());
            saveInfo.setDefaultHpPotionIds(ItemSystem.getInstance().getDefaultHpPotionIds());
            saveInfo.setDefaultMpPotionIds(ItemSystem.getInstance().getDefaultMpPotionIds());
            saveInfo.setPetInfos(PetSystem.getInstance().toSaveInfos());
            saveInfo.setBagMaxSize(ItemSystem.getInstance().getMaxSize());
            saveInfo.setSkillBookShopBought(ShopSystem.getInstance().getSkillBookShopBought());
            if (this.saveInfo != null) {
                saveInfo.setIllusionRealmInfo(this.saveInfo.getIllusionRealmInfo());
            }

            String jsonData = gson.toJson(saveInfo);
            LogUtil.i("saveInfo: " + jsonData);

            FileOutputStream fos = new FileOutputStream(getSlotFile(context, currentSaveId));
            fos.write(jsonData.getBytes());
            fos.close();
            this.saveInfo = saveInfo;
            persistCurrentSaveId(context, currentSaveId);
        } catch (Exception e) {
            LogUtil.i("saveInfo error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** 开始游戏：读取最近更新的存档；没有则开新档。 */
    public void readLatest(Context context) {
        migrateLegacySave(context);
        List<SaveSlotInfo> slots = listSlots(context);
        if (slots.isEmpty()) {
            currentSaveId = null;
            persistCurrentSaveId(context, null);
            applySaveInfo(getInitData());
            return;
        }
        readSlot(context, slots.get(0).getId());
    }

    /** 兼容旧调用，等同于读取最近存档。 */
    public void read(Context context) {
        readLatest(context);
    }

    /** 开一档新存档：进入游戏后首次自动保存会生成新文件，不覆盖旧档。 */
    public void startNewGame(Context context) {
        currentSaveId = null;
        persistCurrentSaveId(context, null);
        applySaveInfo(getInitData());
    }

    public void readSlot(Context context, String saveId) {
        migrateLegacySave(context);
        SaveInfo loaded = loadSaveFile(getSlotFile(context, saveId));
        if (loaded == null) {
            applySaveInfo(getInitData());
            currentSaveId = saveId;
            persistCurrentSaveId(context, saveId);
            return;
        }
        if (loaded.getSaveId() == null || loaded.getSaveId().isEmpty()) {
            loaded.setSaveId(saveId);
        }
        currentSaveId = saveId;
        persistCurrentSaveId(context, saveId);
        applySaveInfo(loaded);
    }

    public boolean deleteSlot(Context context, String saveId) {
        if (saveId == null || saveId.isEmpty()) {
            return false;
        }
        File file = getSlotFile(context, saveId);
        boolean deleted = !file.exists() || file.delete();
        if (deleted && saveId.equals(currentSaveId)) {
            currentSaveId = null;
            persistCurrentSaveId(context, null);
            saveInfo = null;
        }
        return deleted;
    }

    public List<SaveSlotInfo> listSlots(Context context) {
        migrateLegacySave(context);
        List<SaveSlotInfo> slots = new ArrayList<>();
        File dir = getSavesDir(context);
        File[] files = dir.listFiles((d, name) -> name.startsWith("save_") && name.endsWith(".json"));
        if (files == null) {
            return slots;
        }
        for (File file : files) {
            SaveSlotInfo slot = toSlotInfo(file);
            if (slot != null) {
                slots.add(slot);
            }
        }
        Collections.sort(slots, new Comparator<SaveSlotInfo>() {
            @Override
            public int compare(SaveSlotInfo a, SaveSlotInfo b) {
                return Long.compare(b.getUpdatedAt(), a.getUpdatedAt());
            }
        });
        return slots;
    }

    public SaveInfo getSaveInfo() {
        return saveInfo;
    }

    private void applySaveInfo(SaveInfo saveInfo) {
        this.saveInfo = saveInfo;
        RoleSystem.getInstance().setRoleInfo(saveInfo.getRoleInfo());
        ItemSystem.getInstance().setItemInfos(saveInfo.getItemInfos());
        ItemSystem.getInstance().setEquipInfos(saveInfo.getEquipInfos());
        SkillSystem.getInstance().setMainSkillInfos(saveInfo.getMainSkillInfos());
        SkillSystem.getInstance().setAssistSkillInfos(saveInfo.getAssistSkillInfos());
        SkillSystem.getInstance().setPractiseSkillInfos(saveInfo.getPractiseSkillInfos());
        SkillSystem.getInstance().setEquippedActiveSkills(saveInfo.getEquipedSkillInfos());
        QuestSystem.getInstance().setAcceptedQuests(saveInfo.getQuestInfos());
        ItemSystem.getInstance().setDefaultHpPotionIds(saveInfo.getDefaultHpPotionIds());
        ItemSystem.getInstance().setDefaultMpPotionIds(saveInfo.getDefaultMpPotionIds());
        PetSystem.getInstance().loadFrom(saveInfo.getPetInfos());
        if (saveInfo.getBagMaxSize() > 0) {
            ItemSystem.getInstance().setMaxSize(saveInfo.getBagMaxSize());
        }
        ShopSystem.getInstance().setSkillBookShopBought(saveInfo.getSkillBookShopBought());
        com.game.dream.quest.SideQuest005.syncWorld();
    }

    private SaveInfo getInitData() {
        SaveInfo saveInfo = new SaveInfo();
        saveInfo.setRoleInfo(RoleSystem.getInstance().getInitRoleInfo());
        return saveInfo;
    }

    private SaveSlotInfo toSlotInfo(File file) {
        SaveInfo info = loadSaveFile(file);
        if (info == null) {
            return null;
        }
        String id = info.getSaveId();
        if (id == null || id.isEmpty()) {
            id = idFromFileName(file.getName());
        }
        if (id == null) {
            return null;
        }
        SaveSlotInfo slot = new SaveSlotInfo();
        slot.setId(id);
        RoleInfo role = info.getRoleInfo();
        if (role != null) {
            slot.setRoleName(role.getName() == null || role.getName().isEmpty() ? "无名侠客" : role.getName());
            slot.setLevel(role.getLevel());
            slot.setMapId(role.getMapId());
            slot.setMapName(MapSystem.getInstance().getMapName(role.getMapId()));
        } else {
            slot.setRoleName("无名侠客");
            slot.setMapName("未知地图");
        }
        long updated = info.getUpdatedAt();
        if (updated <= 0) {
            updated = file.lastModified();
        }
        slot.setUpdatedAt(updated);
        return slot;
    }

    private SaveInfo loadSaveFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            fis.close();
            return gson.fromJson(sb.toString(), SaveInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void ensureCurrentSaveId(Context context) {
        if (currentSaveId != null && !currentSaveId.isEmpty()) {
            return;
        }
        String stored = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_CURRENT_ID, null);
        if (stored != null && !stored.isEmpty() && getSlotFile(context, stored).exists()) {
            currentSaveId = stored;
            return;
        }
        currentSaveId = String.valueOf(System.currentTimeMillis());
    }

    private void persistCurrentSaveId(Context context, String saveId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        if (saveId == null) {
            editor.remove(PREF_CURRENT_ID);
        } else {
            editor.putString(PREF_CURRENT_ID, saveId);
        }
        editor.apply();
    }

    private void migrateLegacySave(Context context) {
        File legacy = new File(new StorageHelper(context).getAppExternalDir(), LEGACY_SAVE_FILE);
        if (!legacy.exists()) {
            return;
        }
        File dest = getSlotFile(context, "legacy");
        if (dest.exists()) {
            return;
        }
        File dir = dest.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        if (!legacy.renameTo(dest)) {
            copyFile(legacy, dest);
        }
    }

    private void copyFile(File src, File dest) {
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getSavesDir(Context context) {
        File dir = new File(new StorageHelper(context).getAppExternalDir(), SAVES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private File getSlotFile(Context context, String saveId) {
        return new File(getSavesDir(context), "save_" + saveId + ".json");
    }

    private String idFromFileName(String name) {
        if (name == null || !name.startsWith("save_") || !name.endsWith(".json")) {
            return null;
        }
        return name.substring("save_".length(), name.length() - ".json".length());
    }

    class ItemDeserializer implements JsonDeserializer<ItemInfo> {
        @Override
        public ItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            if ("EQUIPMENT".equals(type)) {
                return context.deserialize(jsonObject, EquipItemInfo.class);
            } else if ("SPECIAL".equals(type) && jsonObject.has("targetMapId")) {
                return context.deserialize(jsonObject, TreasureMapItemInfo.class);
            } else {
                int id = jsonObject.get("id").getAsInt();
                String name = jsonObject.get("name").getAsString();
                int amount = jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1;
                return new ItemInfo(id, name, type, amount);
            }
        }
    }
}
