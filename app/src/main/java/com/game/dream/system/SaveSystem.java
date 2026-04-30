package com.game.dream.system;

import android.content.Context;

import com.game.dream.LogUtil;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.bean.SaveInfo;
import com.game.dream.utils.StorageHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class SaveSystem {

    private static SaveSystem instance = new SaveSystem();

    public static SaveSystem getInstance() {
        return instance;
    }

    private final static String SAVE_FILE_NAME = "save.txt";
    private SaveInfo saveInfo;

    private Gson gson;

    private SaveSystem() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ItemInfo.class, new ItemDeserializer()) // 自定义反序列化器
                .create();
    }

    public void save(Context context) {
        try {
            SaveInfo saveInfo = new SaveInfo();
            saveInfo.setRoleInfo(RoleSystem.getInstance().getRoleInfo());
            saveInfo.setItemInfos(ItemSystem.getInstance().getItemInfos());
            saveInfo.setEquipInfos(ItemSystem.getInstance().getEquipInfos());

            // Convert to JSON and save
            String jsonData = gson.toJson(saveInfo);

            LogUtil.i("saveInfo: " + jsonData);

            // Save to external storage with level number
            FileOutputStream fos = new FileOutputStream(getSavePath(context));
            fos.write(jsonData.getBytes());
            fos.close();
        } catch (Exception e) {
            LogUtil.i("saveInfo error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void read(Context context) {
        try {
            FileInputStream fis = new FileInputStream(getSavePath(context));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            fis.close();

            String content = sb.toString();
            LogUtil.i("read saveInfo: " + content);
            saveInfo = gson.fromJson(content, SaveInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (saveInfo == null) {
            saveInfo = getInitData();
        }

        RoleSystem.getInstance().setRoleInfo(saveInfo.getRoleInfo());
        ItemSystem.getInstance().setItemInfos(saveInfo.getItemInfos());
        ItemSystem.getInstance().setEquipInfos(saveInfo.getEquipInfos());
    }

    private SaveInfo getInitData() {
        SaveInfo saveInfo = new SaveInfo();
        saveInfo.setRoleInfo(RoleSystem.getInstance().getInitRoleInfo());
        return saveInfo;
    }

    public SaveInfo getSaveInfo() {
        return saveInfo;
    }

    private String getSavePath(Context context) {
        String savePath = new StorageHelper(context).getAppExternalDir().getAbsolutePath() + "/" + SAVE_FILE_NAME;
        return savePath;
    }

    // 自定义反序列化器示例
    class ItemDeserializer implements JsonDeserializer<ItemInfo> {
        @Override
        public ItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            if ("EQUIPMENT".equals(type)) {
                return context.deserialize(jsonObject, EquipItemInfo.class);
            } else {
                // For other items (CONSUMABLE, MATERIAL, etc.), parse directly to avoid infinite recursion
                int id = jsonObject.get("id").getAsInt();
                String name = jsonObject.get("name").getAsString();
                int amount = jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1;

                ItemInfo itemInfo = new ItemInfo(id, name, type, amount);
                return itemInfo;
            }
        }
    }
}
