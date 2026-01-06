package com.example.forgetmenot.ui.expandable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.forgetmenot.R;

import java.util.List;

public final class ExpandableSectionAdapter extends RecyclerView.Adapter<ExpandableSectionAdapter.VH> {

    private final List<ExpandableSection> items;

    // Track expanded state per item
    private final boolean[] expanded;

    public ExpandableSectionAdapter(@NonNull List<ExpandableSection> items) {
        this.items = items;
        this.expanded = new boolean[items.size()];
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_expandable_section, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ExpandableSection section = items.get(position);

        holder.title.setText(section.getTitle());
        holder.body.setText(section.getBody());

        boolean isExpanded = expanded[position];
        holder.body.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.icon.setRotation(isExpanded ? 180f : 0f);

        holder.header.setOnClickListener(v -> {
            expanded[position] = !expanded[position];
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final View header;
        final TextView title;
        final TextView body;
        final ImageView icon;

        VH(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            title = itemView.findViewById(R.id.section_title);
            body = itemView.findViewById(R.id.section_body);
            icon = itemView.findViewById(R.id.expand_icon);
        }
    }
}
