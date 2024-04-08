package com.voxelvisage.modelviewer

import android.content.Context
import android.view.View
import android.widget.Toast

class EyeGlassButtonClickListener(private val context: Context) : View.OnClickListener {
    override fun onClick(v: View?) {
        Toast.makeText(context, "Eye glass button clicked", Toast.LENGTH_SHORT).show()
    }
}
