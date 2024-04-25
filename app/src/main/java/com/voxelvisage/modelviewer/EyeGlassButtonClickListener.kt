package com.voxelvisage.modelviewer


import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.voxelvisage.modelviewer.main.MainActivity
import com.voxelvisage.modelviewer.obj.ObjModel
import java.io.IOException
import java.util.Locale

@Suppress("NAME_SHADOWING")
class EyeGlassButtonClickListener(private val mainActivity: MainActivity) : View.OnClickListener {

    override fun onClick(v: View?) {
        showEyeGlassSelectionDialog()
    }

    private fun showEyeGlassSelectionDialog() {
        val hasModel = ModelViewerApplication.currentModel != null

        mainActivity.assets.list("")?.filter { it.endsWith(".obj") } ?: emptyList()

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
                        "Voxel Visage will add the selected filter to the scene.\n\n" +
                        "For the best experience, a face is required to accurately test the visual appearance of the glasses.\n"
            )
            .setPositiveButton("OK") { dialog, _ ->
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
                "glasses.obj",
                "file:///android_asset/glasses.png"
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

        option1TextView.text = "Glasses"
        option2TextView.text = "Thin Eyeglasses"

        Glide.with(mainActivity).load(options[0].second).into(option1Button)
        Glide.with(mainActivity).load(options[1].second).into(option2Button)


        alertDialogBuilder
            .setTitle("Select a filter:")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = alertDialogBuilder.create()
        dialog.setCancelable(true)

        option1Button.setOnClickListener {
            handleOptionSelection(options[0].first)
            dialog.dismiss()
        }
        option2Button.setOnClickListener {
            handleOptionSelection(options[1].first)
            dialog.dismiss()
        }

        alertDialogBuilder
            .setTitle("Select a filter:")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialog.show()
    }

    private fun handleOptionSelection(modelName: String) {
        try {
            val stream = mainActivity.assets.open(modelName)
            val fileName = modelName.substringAfterLast("/").removeSuffix(".obj")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val fileFormat = modelName.substringAfterLast(".")
            val title = "$fileName.$fileFormat"
            val model = ObjModel(stream)
            model.title = title
            mainActivity.setCurrentModel(model)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}