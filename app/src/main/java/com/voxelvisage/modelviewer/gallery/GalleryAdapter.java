package com.voxelvisage.modelviewer.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;
import com.voxelvisage.modelviewer.R;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Uri> images;
    private final boolean[] selectedStates;
    private int visibleItemCount = 20;
    private int loadedItemCount = 0;


    public GalleryAdapter(Context context, ArrayList<Uri> images, boolean[] selectedStates) {
        this.context = context;
        this.images = images;
        this.selectedStates = selectedStates;
    }

    @Override
    public int getCount() {
        return Math.min(images.size(), loadedItemCount + visibleItemCount);
    }

    @Override
    public Uri getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.tickImageView = convertView.findViewById(R.id.tickImageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Uri imageUri = images.get(position);
        Picasso.get().load(imageUri).resize(600, 400).centerCrop().into(holder.imageView);

        if (selectedStates[position]) {
            holder.tickImageView.setVisibility(View.VISIBLE);
        } else {
            holder.tickImageView.setVisibility(View.GONE);
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        if (position >= getCount() - 1 && loadedItemCount < images.size()) {
            loadedItemCount += visibleItemCount;
            notifyDataSetChanged();
        }

        return convertView;
    }

    static class ViewHolder {
        CurvedImageView imageView;
        ImageView tickImageView;
    }

    public void updateView(int position, View convertView) {
        ViewHolder holder = (ViewHolder) convertView.getTag();

        if (selectedStates[position]) {
            holder.tickImageView.setVisibility(View.VISIBLE);
        } else {
            holder.tickImageView.setVisibility(View.GONE);
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
    }
    public void setVisibleItemCount(int itemCount) {
        visibleItemCount = itemCount;
        notifyDataSetChanged();
    }
}
