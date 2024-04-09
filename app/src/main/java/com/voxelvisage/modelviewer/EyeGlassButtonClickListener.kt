package com.voxelvisage.modelviewer

import android.content.Context
import android.net.Uri
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

        alertDialogBuilder
            .setTitle("Filters Overlaying")
            .setMessage(
                "The user can select one of the 3D eyeglasses filters.\n\n" +
                        "Voxel Visage will apply the selected filter to a 3D model face displayed in the scene by fitting the glasses onto the face seamlessly.\n\n" +
                        "If the selected model does not accurately represent a face or is unclear, an error will be displayed."
            )
            .setPositiveButton("OK") { _, _ ->
            }
            .show()
    }

}
