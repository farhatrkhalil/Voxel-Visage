package com.voxelvisage.modelviewer

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.voxelvisage.modelviewer.databinding.ActivityMainBinding
import com.voxelvisage.modelviewer.gvr.ModelGvrActivity
import com.voxelvisage.modelviewer.obj.ObjModel
import com.voxelvisage.modelviewer.ply.PlyModel
import com.voxelvisage.modelviewer.stl.StlModel
import com.voxelvisage.modelviewer.util.Util.closeSilently
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sampleModels: List<String>
    private var sampleModelIndex = 0
    private var modelView: ModelSurfaceView? = null
    private val disposables = CompositeDisposable()

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.data != null) {
                val uri = it.data?.data
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                beginLoadModel(uri!!)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                beginOpenModel()
            } else {
                Toast.makeText(this, R.string.read_permission_failed, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showWelcomeDialog()
        binding.progressBar.visibility = View.GONE
        binding.actionButton.setOnClickListener { startVrActivity() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerView)) { _, insets ->
            (binding.actionButton.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = insets.systemWindowInsetTop
                bottomMargin = insets.systemWindowInsetBottom
                leftMargin = insets.systemWindowInsetLeft
                rightMargin = insets.systemWindowInsetRight
            }
            (binding.progressBar.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = insets.systemWindowInsetTop
                bottomMargin = insets.systemWindowInsetBottom
                leftMargin = insets.systemWindowInsetLeft
                rightMargin = insets.systemWindowInsetRight
            }
            insets.consumeSystemWindowInsets()
        }

        sampleModels = assets.list("")!!.filter { it.endsWith(".stl") }

        if (intent.data != null && savedInstanceState == null) {
            beginLoadModel(intent.data!!)
        }
    }

    private fun showWelcomeDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome to Voxel Visage")
            .setMessage(
                "Our 3D facial reconstruction app!\n\nTo get started, you can:\n\n" +
                        "- Load a sample model\n" +
                        "- Select a model\n" +
                        "- Provide 3 facial images\n"
            )
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                showOptionsDialog()
            }
            .setCancelable(false)
            .show()
    }

    private fun showOptionsDialog() {
        val options = arrayOf("Load sample model", "Select a model", "Provide 3 facial images")
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> loadSampleModel()
                    1 -> checkReadPermissionThenOpen()
                }
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }


    override fun onStart() {
        super.onStart()
        createNewModelView(ModelViewerApplication.currentModel)
        if (ModelViewerApplication.currentModel != null) {
            title = ModelViewerApplication.currentModel!!.title
        }
    }

    override fun onPause() {
        super.onPause()
        modelView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        modelView?.onResume()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_open_model -> {
                checkReadPermissionThenOpen()
                true
            }

            R.id.menu_load_sample -> {
                loadSampleModel()
                true
            }

            R.id.menu_reset_view -> {
                resetModelView()
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resetModelView() {
        if (modelView != null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Reset View")
                .setMessage("Are you sure you want to reset the view?")
                .setPositiveButton("Yes") { _, _ ->
                    modelView?.removeModel()
                    title = "Voxel Visage"
                    Toast.makeText(this, "Model view reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("No Model")
                .setMessage("There is currently no model to remove.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }


    private fun checkReadPermissionThenOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            showDisclaimerDialog()
        }
    }

    private fun showDisclaimerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disclaimer")
            .setMessage("Please select a model with the supported formats (.obj, .stl, .ply)")
            .setPositiveButton("OK") { _, _ ->
                beginOpenModel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun beginOpenModel() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
        openDocumentLauncher.launch(intent)
    }

    private fun createNewModelView(model: Model?) {
        if (modelView != null) {
            binding.containerView.removeView(modelView)
        }
        modelView = ModelSurfaceView(this, model)
        binding.containerView.addView(modelView, 0)
    }

    private fun beginLoadModel(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE

        disposables.add(Observable.fromCallable {
            var model: Model? = null
            var stream: InputStream? = null
            try {
                val cr = applicationContext.contentResolver
                val fileName = getFileName(cr, uri)
                stream = if ("http" == uri.scheme || "https" == uri.scheme) {
                    val client = OkHttpClient()
                    val request: Request = Request.Builder().url(uri.toString()).build()
                    val response = client.newCall(request).execute()

                    // TODO: figure out how to NOT need to read the whole file at once.
                    ByteArrayInputStream(response.body!!.bytes())
                } else {
                    cr.openInputStream(uri)
                }
                if (stream != null) {
                    if (!fileName.isNullOrEmpty()) {
                        model = when {
                            fileName.lowercase(Locale.ROOT).endsWith(".stl") -> {
                                StlModel(stream)
                            }

                            fileName.lowercase(Locale.ROOT).endsWith(".obj") -> {
                                ObjModel(stream)
                            }

                            fileName.lowercase(Locale.ROOT).endsWith(".ply") -> {
                                PlyModel(stream)
                            }

                            else -> {
                                // assume it's STL.
                                StlModel(stream)
                            }
                        }
                        model.title = fileName
                    } else {
                        // assume it's STL.
                        // TODO: autodetect file type by reading contents?
                        model = StlModel(stream)
                    }
                }
                ModelViewerApplication.currentModel = model
                model!!
            } finally {
                closeSilently(stream)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                binding.progressBar.visibility = View.GONE
            }
            .subscribe({
                setCurrentModel(it)
            }, {
                it.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.open_model_error, it.message),
                    Toast.LENGTH_SHORT
                ).show()
            })
        )
    }

    private fun getFileName(cr: ContentResolver, uri: Uri): String? {
        if ("content" == uri.scheme) {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            ContentResolverCompat.query(cr, uri, projection, null, null, null, null)
                ?.use { metaCursor ->
                    if (metaCursor.moveToFirst()) {
                        return metaCursor.getString(0)
                    }
                }
        }
        return uri.lastPathSegment
    }

    private fun setCurrentModel(model: Model) {
        ModelViewerApplication.currentModel = model
        createNewModelView(model)
        Toast.makeText(applicationContext, R.string.open_model_success, Toast.LENGTH_SHORT).show()
        title = model.title
        binding.progressBar.visibility = View.GONE
    }

    private fun startVrActivity() {
        if (ModelViewerApplication.currentModel == null) {
            Toast.makeText(this, R.string.view_vr_not_loaded, Toast.LENGTH_SHORT).show()
        } else {
            startActivity(Intent(this, ModelGvrActivity::class.java))
        }
    }

    private fun loadSampleModel() {
        val sampleModelNames = sampleModels.map {
            it.substringAfterLast("/").removeSuffix(".stl")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        AlertDialog.Builder(this)
            .setTitle("Select Sample Model")
            .setItems(sampleModelNames.toTypedArray()) { dialog, which ->
                try {
                    val stream = assets.open(sampleModels[which])
                    val fileName = sampleModelNames[which]
                    val fileFormat = sampleModels[which].substringAfterLast(".")
                    val title = "$fileName.$fileFormat"
                    val model = StlModel(stream)
                    model.title = title
                    setCurrentModel(model)
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
