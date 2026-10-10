package com.game.dream;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.game.dream.bean.SaveSlotInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaveSlotAdapter extends RecyclerView.Adapter<SaveSlotAdapter.Holder> {

    public interface Listener {
        void onLoad(SaveSlotInfo slot);

        void onDelete(SaveSlotInfo slot);
    }

    private final List<SaveSlotInfo> slots = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public SaveSlotAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setSlots(List<SaveSlotInfo> data) {
        slots.clear();
        if (data != null) {
            slots.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_save_slot, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SaveSlotInfo slot = slots.get(position);
        holder.roleText.setText(slot.getRoleName() + "    Lv." + slot.getLevel());
        String time = slot.getUpdatedAt() > 0 ? dateFormat.format(new Date(slot.getUpdatedAt())) : "--";
        String mapName = slot.getMapName() == null ? "未知地图" : slot.getMapName();
        holder.detailText.setText(mapName + "    " + time);
        holder.loadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLoad(slot);
            }
        });
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView roleText;
        final TextView detailText;
        final Button loadButton;
        final Button deleteButton;

        Holder(View itemView) {
            super(itemView);
            roleText = itemView.findViewById(R.id.roleText);
            detailText = itemView.findViewById(R.id.detailText);
            loadButton = itemView.findViewById(R.id.loadButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
