package com.voxelvisage.modelviewer.connection

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.voxelvisage.modelviewer.main.MainActivity
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private var retrofit: Retrofit? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES) // connect timeout
        .readTimeout(40, TimeUnit.SECONDS) // socket timeout
        .build()

    private var modelName: String? = null

    @JvmStatic
    fun uploadImages(imageFiles: List<File>, context: Context) {
        val apiService = instance!!.create(ApiService::class.java)
        val imageParts: MutableList<MultipartBody.Part> = ArrayList()
        for (imageFile in imageFiles) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(imageFile.path)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val mediaType: MediaType? = mimeType?.toMediaTypeOrNull()
            if (mediaType != null) {
                val requestBody = RequestBody.create(mediaType, imageFile)
                val imagePart = MultipartBody.Part.createFormData("files", imageFile.name, requestBody)
                imageParts.add(imagePart)
            }
        }
        val call = apiService.uploadImages(imageParts)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("API_CALL", "Response received")

                        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        val file = File(downloadDir, "model982731.obj")

                        val sink = file.sink().buffer()

                        sink.writeAll(responseBody.source())

                        sink.close()

                        Log.d("API_CALL", "File created at ${file.absolutePath}")
                        modelName = file.absolutePath

                        val fileUri: Uri = FileProvider.getUriForFile(
                            context,
                            "com.voxelvisage.modelviewer.fileprovider",
                            file
                        )

                        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        scanIntent.data = fileUri
                        context.sendBroadcast(scanIntent)

                        val mainIntent = Intent(context, MainActivity::class.java)
                        mainIntent.putExtra("fromGallery", true)
                        mainIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(context, mainIntent, null)
                    } else {
                        Log.d("API_CALL", "Response body is null")
                    }
                } else {
                    Log.d("API_CALL", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_CALL", "Call failed", t)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Call failed: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }

        })
    }


    private val instance: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.0.107:3003") // Replace with your actual API base URL
                    //You can get the base url from ipconfig in cmd and make sure its the wireless lan
                    // ipv4 address (WIFI Section)
                    //In this case we are speaking about the server running on a Laptop and the android
                    // app running on a mobile phone under the same network.
                    .addConverterFactory(ScalarsConverterFactory.create()) // Adjust if needed (e.g., for JSON use GsonConverterFactory)
                    .client(okHttpClient)
                    .build()
            }
            return retrofit
        }


    interface ApiService {
        @Multipart
        @POST("/process_images")
        fun uploadImages(@Part files: List<MultipartBody.Part>?): Call<ResponseBody>
    }
}
