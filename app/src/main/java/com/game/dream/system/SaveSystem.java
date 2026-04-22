package com.game.dream.system;

import android.content.Context;

import com.game.dream.LogUtil;
import com.game.dream.bean.SaveInfo;
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

    private final static String SAVE_FILE_NAME = "save.json";
    private SaveInfo saveInfo;

    private SaveSystem() {

    }

    public void save(Context context) {
        try {
            SaveInfo saveInfo = new SaveInfo();

            saveInfo.setRoleInfo(RoleSystem.getInstance().getRoleInfo());

            // Convert to JSON and save
            Gson gson = new Gson();
            String jsonData = gson.toJson(saveInfo);

            // Save to internal storage with level number
            FileOutputStream fos = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read(Context context) {
        try {
            FileInputStream fis = context.openFileInput(SAVE_FILE_NAME);
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
    }

    private SaveInfo getInitData() {
        SaveInfo saveInfo = new SaveInfo();
        saveInfo.setRoleInfo(RoleSystem.getInstance().getInitRoleInfo());
        return saveInfo;
    }

    public SaveInfo getSaveInfo() {
        return saveInfo;
    }
}
