package com.game.dream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.game.dream.bean.SaveSlotInfo;
import com.game.dream.system.SaveSystem;

import java.util.List;

public class SaveListActivity extends AppCompatActivity implements SaveSlotAdapter.Listener {

    private SaveSlotAdapter adapter;
    private TextView emptyView;
    private RecyclerView recyclerView;

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

    private void refreshList() {
        List<SaveSlotInfo> slots = SaveSystem.getInstance().listSlots(getApplicationContext());
        adapter.setSlots(slots);
        boolean empty = slots.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
