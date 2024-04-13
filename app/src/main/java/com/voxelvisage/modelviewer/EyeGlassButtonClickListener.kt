package com.voxelvisage.modelviewer


import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.voxelvisage.modelviewer.MainActivity
import com.voxelvisage.modelviewer.ModelViewerApplication
import com.voxelvisage.modelviewer.R

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

        val filterModels: List<String> =
            mainActivity.assets.list("")?.filter { it.endsWith(".obj") } ?: emptyList()

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

    @SuppressLint("SetTextI18n")
    private fun showSelectOptionsDialog() {
        val options = arrayOf(
            Pair(
                "metal_frame_reading_glasses_filter2.obj",
                "file:///android_asset/metal_frame_reading_glasses_filter2.png"
            ),
            Pair("thin_eyeglasses_filter1.obj", "file:///android_asset/thin_eyeglasses_filter1.png")
        )

        val alertDialogBuilder = AlertDialog.Builder(mainActivity)
        val dialogView =
            LayoutInflater.from(mainActivity).inflate(R.layout.dialog_select_options, null)
        alertDialogBuilder.setView(dialogView)

        val option1Button = dialogView.findViewById<ImageButton>(R.id.option1Button)
        val option2Button = dialogView.findViewById<ImageButton>(R.id.option2Button)

        val option1TextView = dialogView.findViewById<TextView>(R.id.option1TextView)
        val option2TextView = dialogView.findViewById<TextView>(R.id.option2TextView)

        option1TextView.text = "Metal Frame Reading Glasses"
        option2TextView.text = "Thin Eyeglasses"

        // Load thumbnails using Glide or any other image loading library
        Glide.with(mainActivity).load(options[0].second).into(option1Button)
        Glide.with(mainActivity).load(options[1].second).into(option2Button)

        // Handle button clicks
        option1Button.setOnClickListener {
            // Handle option 1 selection here
        }
        option2Button.setOnClickListener {
            // Handle option 2 selection here
        }

        alertDialogBuilder
            .setTitle("Select a filter:")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}