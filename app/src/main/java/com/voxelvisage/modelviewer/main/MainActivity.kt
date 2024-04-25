package com.voxelvisage.modelviewer.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.voxelvisage.modelviewer.EyeGlassButtonClickListener
import com.voxelvisage.modelviewer.LoadingDialog
import com.voxelvisage.modelviewer.Model
import com.voxelvisage.modelviewer.ModelRenderer
import com.voxelvisage.modelviewer.ModelSurfaceView
import com.voxelvisage.modelviewer.ModelViewerApplication
import com.voxelvisage.modelviewer.ModelViewerApplication.Companion.currentModel
import com.voxelvisage.modelviewer.R
import com.voxelvisage.modelviewer.connection.NoInternetActivity
import com.voxelvisage.modelviewer.databinding.ActivityMainBinding
import com.voxelvisage.modelviewer.gallery.GalleryViewerActivity
import com.voxelvisage.modelviewer.gvr.ModelGvrActivity
import com.voxelvisage.modelviewer.obj.ObjModel
import com.voxelvisage.modelviewer.ply.PlyModel
import com.voxelvisage.modelviewer.settings.SettingsActivity
import com.voxelvisage.modelviewer.stl.StlModel
import com.voxelvisage.modelviewer.util.Util.closeSilently
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sampleModels: List<String>
    private var modelView: ModelSurfaceView? = null
    private val disposables = CompositeDisposable()
    private var loadingDialog: LoadingDialog? = null
    private var uri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())


    private var floorColor = floatArrayOf(0.2f, 0.2f, 0.2f, 1.0f)
    private var lineColor = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)

    private var modelRenderer = ModelRenderer(this, null, floorColor, lineColor)


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

        val intent = intent

        if (intent.hasExtra("modelPath")) {
            val modelPath = intent.getStringExtra("modelPath")!!
            val uri = Uri.parse(modelPath)
            beginLoadModel(uri)
        }

        var floorColor = floatArrayOf(0.8f, 0.4f, 0.8f, 1.0f)
        var lineColor = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)

        modelView = ModelSurfaceView(this, null, floorColor, lineColor)
        modelRenderer = ModelRenderer(this, null, floorColor, lineColor)

        setContentView(binding.root)
        val eyeGlassButtonClickListener = EyeGlassButtonClickListener(this)


        showWelcomeDialog()
        binding.addButton.setOnClickListener {
            showImageSourceDialog()
        }

        binding.shareDownload.setOnClickListener {
            showShareDownloadOptions()
        }

        binding.actionButton.setOnClickListener {
            startVrActivity()
        }


        binding.filterButton.setOnClickListener {
            val eyeGlassButtonClickListener = EyeGlassButtonClickListener(this@MainActivity)
            eyeGlassButtonClickListener.onClick(it)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerView)) { _, insets ->
            (binding.actionButton.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = insets.systemWindowInsetTop
                bottomMargin = insets.systemWindowInsetBottom
                leftMargin = insets.systemWindowInsetLeft
                rightMargin = insets.systemWindowInsetRight
            }
            (binding.progressBar.layoutParams as ConstraintLayout.LayoutParams).apply {
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

    @SuppressLint("SetTextI18n")
    fun updateFpsCounter(fps: Int) {
        runOnUiThread {
            binding.fpsCounter.text = "FPS: $fps"
        }
    }

    private fun loadModelFromFile(filePath: String) {
        try {
            val stream = FileInputStream(filePath)
            val fileName = File(filePath).nameWithoutExtension
            val fileFormat = filePath.substringAfterLast(".")
            val title = "$fileName.$fileFormat"
            val model = when (fileFormat) {
                "obj" -> ObjModel(stream)
                "stl" -> StlModel(stream)
                "ply" -> PlyModel(stream)
                else -> throw IllegalArgumentException("Unsupported file format: $fileFormat")
            }
            model.title = title
            setCurrentModel(model)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun checkConnectivity() {
        if (isConnected()) {
            Toast.makeText(this, "Connected to the internet", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this@MainActivity, NoInternetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Select Images from Gallery")
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setTitle("Choose Source Of Images:")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> selectFromGallery()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun selectFromGallery() {
        val intent = Intent(this, GalleryViewerActivity::class.java)
        startActivity(intent)
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to exit the app?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun showWelcomeDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialog = builder.setTitle("Welcome to Voxel Visage")
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
            .create()

        dialog.show()

    }


    private fun showOptionsDialog() {
        val options = arrayOf("Load sample model", "Select a model", "Provide 3 facial images")
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setTitle("Choose an option:")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> loadSampleModel()
                    1 -> checkReadPermissionThenOpen()
                    2 -> showImageSourceDialog()
                }
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        dialog.show()

    }


    private fun showShareDownloadOptions() {
        if (isSampleModel()) {
            AlertDialog.Builder(this)
                .setTitle("Sample Models Restriction")
                .setMessage("Sample models are restricted from downloading and sharing due to copyright protection.\n\nThis measure ensures compliance with copyright laws, safeguarding the rights of content creators. Allowing downloads without proper authorization could lead to legal ramifications, including copyright infringement.\n\nBy enforcing this restriction, the application maintains ethical usage practices and respects the intellectual property rights of model creators.")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        } else if (ModelViewerApplication.currentModel == null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Model Available")
                .setMessage("There is currently no model available to share or download.")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        } else {
            val options = arrayOf("Download/Share model")
            AlertDialog.Builder(this)
                .setTitle("Select an option")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> showDownloadDisclaimerDialog()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }


    private fun showDownloadDisclaimerDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Disclaimer")
            .setMessage("Please enter the desired model name, and note that the format of the model will remain unchanged (.obj, .stl, .ply).")
            .setPositiveButton("OK") { dialog, which ->
                downloadModel()
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }


    private fun isSampleModel(): Boolean {
        val currentModelTitle = ModelViewerApplication.currentModel?.title ?: return false
        return sampleModels.contains(currentModelTitle) && currentModelTitle.endsWith(".stl")
    }

    private fun downloadModel() {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        AlertDialog.Builder(this)
            .setTitle("Enter Model Name")
            .setView(editText)
            .setPositiveButton("Next") { dialog, _ ->
                val modelName = editText.text.toString().trim()
                if (modelName.isEmpty()) {
                    Toast.makeText(this, "Model name cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (modelName.contains(Regex("[.](obj|stl|ply)$"))) {
                    Toast.makeText(
                        this,
                        "Model name should not contain file formats like .obj, .stl, .ply",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveModelToFile(modelName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveModelToFile(modelName: String) {

        val uri = uri ?: run {
            Toast.makeText(this, "URI is null", Toast.LENGTH_SHORT).show()
            return
        }

        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Toast.makeText(this, "Failed to open model file", Toast.LENGTH_SHORT).show()
            return
        }

        val extension = when (currentModel) {
            is ObjModel -> "obj"
            is StlModel -> "stl"
            is PlyModel -> "ply"
            else -> {
                Toast.makeText(this, "Invalid model type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val destinationFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "$modelName.$extension"
        )

        try {
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            Toast.makeText(
                this,
                "$modelName.$extension saved to the download directory",
                Toast.LENGTH_SHORT
            ).show()

            val mimeType = when (extension) {
                "obj" -> "text/plain"
                "stl" -> "application/netfabb"
                "ply" -> "application/sla"
                else -> "application/octet-stream"
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, Uri.fromFile(destinationFile))
                type = mimeType
            }
            startActivity(Intent.createChooser(shareIntent, "Share model"))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save model", Toast.LENGTH_SHORT).show()
        } finally {
            inputStream?.close()
        }
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
        handler.removeCallbacksAndMessages(null)
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

            R.id.menu_share_app -> {
                showShareDialog()
                true
            }

            R.id.menu_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                settingsIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(settingsIntent)
                true
            }

            R.id.background -> {
                changeBackground()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showShareDialog() {
        val options = arrayOf("Share the app", "Copy the repository link", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Thanks for sharing the app")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> shareApp()
                    1 -> copyRepoLink()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun shareApp() {
        val appInfo = """
        Check out Voxel Visage, a powerful 3D Face Reconstruction app that leverages a locally hosted API for face and landmarks recognition. 
        The API generates the necessary .obj files, which are then displayed in the application. 
        Internet connection is required for this functionality.
        
        This repository contains the source code and resources for an efficient 3D face reconstruction algorithm, offering users a seamless experience through an Android application developed using both Java and Kotlin in Android Studio.
        
        Features:
        - View 3D models on Android devices.
        - Support for STL (ASCII and binary) files.
        - Limited support for OBJ (Wavefront) and PLY (Stanford) files.
        - Download and share 3D models.
        - Requires internet connection for face and landmarks recognition.
        
        License: Copyright 2024+ Khalil Farhat, Abed El Fattah Amouneh, Sammy Medawar, Wally El Sayed
        Licensed under the MIT License
        
        Learn more at: https://github.com/farhatrkhalil/Voxel-Visage
    """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, appInfo)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share"))
    }

    private fun copyRepoLink() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Repository Link",
            "https://github.com/farhatrkhalil/Voxel-Visage"
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Repository link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun resetModelView() {
        if (modelView != null && ModelViewerApplication.currentModel != null) {
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
        modelView = ModelSurfaceView(this, model, floorColor, lineColor)
        binding.containerView.addView(modelView, 0)
    }

    fun beginLoadModel(uri: Uri) {
        this.uri = uri
        loadingDialog = LoadingDialog(this)
        loadingDialog!!.show()

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
                loadingDialog!!.dismiss()
            }
            .subscribe({ result: Model ->
                setCurrentModel(result)
            }, { error: Throwable ->
                error.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.open_model_error, error.message),
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

    fun setCurrentModel(model: Model) {
        ModelViewerApplication.currentModel = model
        createNewModelView(model)
        Toast.makeText(applicationContext, R.string.open_model_success, Toast.LENGTH_SHORT).show()
        title = model.title
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

    private val combinations = listOf(
        Pair(
            "Pink Floor",
            floatArrayOf(1.0f, 0.5f, 0.5f, 1.0f) to floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        ),
        Pair(
            "Light Blue Floor",
            floatArrayOf(0.5f, 0.5f, 1.0f, 1.0f) to floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        ),
        Pair(
            "Green Floor",
            floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f) to floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        ),
        Pair(
            "Yellow Floor",
            floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f) to floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        ),
        Pair(
            "Purple Floor",
            floatArrayOf(0.5f, 0.0f, 0.5f, 1.0f) to floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        ),
        Pair(
            "Default Floor",
            floatArrayOf(0.2f, 0.2f, 0.2f, 1.0f) to floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)
        )
    )

    private var currentCombinationIndex = 0

    private fun changeBackground() {
        floorColor = combinations[currentCombinationIndex].second.first
        lineColor = combinations[currentCombinationIndex].second.second
        modelView?.refreshBackground(floorColor, lineColor)
        Toast.makeText(this, combinations[currentCombinationIndex].first, Toast.LENGTH_SHORT).show()
        currentCombinationIndex = (currentCombinationIndex + 1) % combinations.size
    }
}
