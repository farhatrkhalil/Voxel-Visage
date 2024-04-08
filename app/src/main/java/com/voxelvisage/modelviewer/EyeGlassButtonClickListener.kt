package com.voxelvisage.modelviewer

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog

class EyeGlassButtonClickListener(private val mainActivity: MainActivity) : View.OnClickListener {

    override fun onClick(v: View?) {
        showEyeGlassSelectionDialog()
    }

    private fun showEyeGlassSelectionDialog() {
        val hasModel = ModelViewerApplication.currentModel != null

        val alertDialogBuilder = AlertDialog.Builder(mainActivity)

        if (!hasModel) {
            alertDialogBuilder
                .setTitle("No Model Available")
                .setMessage("There is currently no model available to apply filters on.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        alertDialogBuilder
            .setTitle("Filters Overlaying")
            .setMessage(
                "The user can select one of the 3D eyeglasses filters.\n\n" +
                        "Voxel Visage will apply the selected filter to a 3D model face displayed in the scene by fitting the glasses onto the face seamlessly.\n\n" +
                        "If the selected model does not accurately represent a face or is unclear, an error will be displayed."
            )
            .setPositiveButton("OK") { _, _ ->
                showSelectOptionDialog()
            }
            .show()
    }

    private fun showSelectOptionDialog() {
        val options = arrayOf("1-Thin Eyeglasses Filter", "2-Metal Frame Glasses Filter")

        AlertDialog.Builder(mainActivity)
            .setTitle("Select Option:")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> applyFilter("thin_eyeglasses_filter1.obj")
                    1 -> applyFilter("metal_frame_reading_glasses_filter2.obj")
                }
            }
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun applyFilter(filterName: String) {
        val modelUri = Uri.parse("android.resource://${mainActivity.packageName}/raw/$filterName")
        mainActivity.beginLoadModel(modelUri)
    }
}
