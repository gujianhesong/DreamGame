package com.game.dream.system;

import android.content.Context;

import com.game.dream.bean.SaveInfo;
import com.game.dream.utils.StorageHelper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class SaveSystem {

    private static SaveSystem instance = new SaveSystem();

    public static SaveSystem getInstance() {
        return instance;
    }

    private final static String SAVE_FILE_NAME = "save.txt";
    private SaveInfo saveInfo;

    private SaveSystem() {

    }

    public void save(Context context) {
        try {
            SaveInfo saveInfo = new SaveInfo();
            saveInfo.setRoleInfo(RoleSystem.getInstance().getRoleInfo());
            saveInfo.setItemInfos(ItemSystem.getInstance().getItemInfos());

            // Convert to JSON and save
            Gson gson = new Gson();
            String jsonData = gson.toJson(saveInfo);

            // Save to external storage with level number
            FileOutputStream fos = new FileOutputStream(getSavePath(context));
            fos.write(jsonData.getBytes());
            fos.close();
        } catch (Exception e) {
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
            Gson gson = new Gson();
            saveInfo = gson.fromJson(content, SaveInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (saveInfo == null) {
            saveInfo = getInitData();
        }

        RoleSystem.getInstance().setRoleInfo(saveInfo.getRoleInfo());
        ItemSystem.getInstance().setItemInfos(saveInfo.getItemInfos());
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
}
