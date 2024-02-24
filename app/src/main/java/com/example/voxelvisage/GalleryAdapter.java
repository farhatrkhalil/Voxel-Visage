package com.example.voxelvisage;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Uri> images;
    private boolean[] selectedStates;

    public GalleryAdapter(Context context, ArrayList<Uri> images, boolean[] selectedStates) {
        this.context = context;
        this.images = images;
        this.selectedStates = selectedStates;
    }

    @Override
    public int getCount() {
        return images.size();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        Picasso.get().load(images.get(position)).resize(300, 300).centerCrop().into(imageView);

        if (selectedStates[position]) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_background));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        return convertView;
    }
}
