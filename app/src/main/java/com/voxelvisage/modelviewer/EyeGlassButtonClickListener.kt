package com.voxelvisage.modelviewer

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog

class EyeGlassButtonClickListener(private val context: Context) : View.OnClickListener {

    override fun onClick(v: View?) {
        showEyeGlassSelectionDialog()
    }

    private fun showEyeGlassSelectionDialog() {
        AlertDialog.Builder(context)
            .setTitle("Filters Overlaying")
            .setMessage(
                "The user can select one of the 3D eyeglasses filters.\n\n" + "Voxel Visage will apply the selected filter to a 3D model face displayed in the scene by fitting the glasses onto the face seamlessly.\n\n" + "If the selected model does not accurately represent a face or is unclear, an error will be displayed.\n"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}
