package com.game.dream;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.game.dream.bean.SaveSlotInfo;
import com.game.dream.system.SaveSystem;

import java.io.File;
import java.util.List;

public class SaveListActivity extends AppCompatActivity implements SaveSlotAdapter.Listener {

    private SaveSlotAdapter adapter;
    private TextView emptyView;
    private RecyclerView recyclerView;

    private final ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri uri = result.getData().getData();
                String error = SaveSystem.getInstance().importSave(getApplicationContext(), uri);
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "存档已导入", Toast.LENGTH_SHORT).show();
                    refreshList();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_list);

        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.saveRecycler);
        adapter = new SaveSlotAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button importSaveButton = findViewById(R.id.importSaveButton);
        importSaveButton.setOnClickListener(v -> openImportPicker());

        Button newSaveButton = findViewById(R.id.newSaveButton);
        newSaveButton.setOnClickListener(v -> {
            SaveSystem.getInstance().startNewGame(getApplicationContext());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onShare(SaveSlotInfo slot) {
        File shareFile = SaveSystem.getInstance().createShareCopy(getApplicationContext(), slot);
        if (shareFile == null) {
            Toast.makeText(this, "无法分享该存档", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", shareFile);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("application/octet-stream");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.putExtra(Intent.EXTRA_SUBJECT, "梦游存档 " + slot.getRoleName());
        send.putExtra(Intent.EXTRA_TEXT, "梦游存档：" + slot.getRoleName() + " Lv." + slot.getLevel());
        send.setClipData(ClipData.newRawUri("", uri));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(send, "分享存档");
        List<ResolveInfo> targets = getPackageManager().queryIntentActivities(send, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo target : targets) {
            grantUriPermission(target.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(chooser);
    }

    @Override
    public void onLoad(SaveSlotInfo slot) {
        SaveSystem.getInstance().readSlot(getApplicationContext(), slot.getId());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onDelete(SaveSlotInfo slot) {
        new AlertDialog.Builder(this)
                .setTitle("删除存档")
                .setMessage("确定删除「" + slot.getRoleName() + " Lv." + slot.getLevel() + "」的存档？删除后无法恢复。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    SaveSystem.getInstance().deleteSlot(getApplicationContext(), slot.getId());
                    refreshList();
                })
                .show();
    }

    private void openImportPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importLauncher.launch(intent);
    }

    private void refreshList() {
        List<SaveSlotInfo> slots = SaveSystem.getInstance().listSlots(getApplicationContext());
        adapter.setSlots(slots);
        boolean empty = slots.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
