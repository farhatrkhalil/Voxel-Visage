package com.voxelvisage.modelviewer

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat.startActivity
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


object RetrofitClient {

    private var retrofit: Retrofit? = null
    private val okHttpClient = OkHttpClient()
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
                        val file = File(context.filesDir, "model.obj")
                        val sink = file.sink().buffer()
                        sink.writeAll(responseBody.source())
                        sink.close()
                        modelName = file.absolutePath;
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("modelPath", modelName)
                        startActivity(context, intent, null)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }


    private val instance: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.0.107:3003") // Replace with your actual API base URL
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
