package com.voxelvisage.modelviewer

import android.util.TypedValue
import android.view.View
import android.widget.TextView
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
                .apply {
                    val messageView = findViewById<TextView>(android.R.id.message)
                    messageView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
                }
            return
        }

        val filtermodels: List<String> = mainActivity.assets.list("")?.filter { it.endsWith(".obj") } ?: emptyList()

        alertDialogBuilder
            .setTitle("Filters Overlaying")
            .setMessage(
                "The user can select one of the 3D eyeglasses filters.\n\n" +
                        "Voxel Visage will apply the selected filter to a 3D model face displayed in the scene by fitting the glasses onto the face seamlessly.\n\n" +
                        "If the selected model does not accurately represent a face or is unclear, an error will be displayed.\n"
            )
            .setPositiveButton("OK") { dialog, _ ->
                // After clicking OK, show another dialog with the options
                showSelectOptionsDialog()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSelectOptionsDialog() {
        val options = arrayOf("metal_frame_reading_glasses_filter2.obj", "thin_eyeglasses_filter1.obj")

        val alertDialogBuilder = AlertDialog.Builder(mainActivity)
        alertDialogBuilder
            .setTitle("Select a filter:")
            .setItems(options) { dialog, which ->
                // Handle option selection here
                val selectedOption = options[which]
                // You can perform further actions based on the selected option
            }
            .show()
    }
}
