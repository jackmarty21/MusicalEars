package com.example.musicalears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.example.musicalears.Module.modules;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView headerText;
        public TextView descText;

        //constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
            descText = itemView.findViewById(R.id.descriptionText);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onListItemClick(getAdapterPosition());
        }
    }

    private List<Module> mModules;

    public ModuleAdapter(List<Module> modules, ListItemClickListener moduleClickListener) {
        mModules = modules;
        itemClickListener = moduleClickListener;
    }

    @NonNull
    @Override
    public ModuleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View moduleView = inflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(moduleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleAdapter.ViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.headerText.setText(module.getName());
        holder.descText.setText(module.getDescription());
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public interface ListItemClickListener {
        void onListItemClick(int position);
    }

    ListItemClickListener itemClickListener;
}
