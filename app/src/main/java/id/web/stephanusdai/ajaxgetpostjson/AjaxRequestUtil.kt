/*
 * AndroidAjaxGetPostJSON by Stephanus Dai
 * @fullname : Stephanus Bagus Saputra
 *             ( 戴 Dai 偉 Wie 峯 Funk )
 * @email    : wiefunk@stephanusdai.web.id
 * @contact  : http://t.me/wiefunkdai
 * @support  : http://opencollective.com/wiefunkdai
 * @weblink  : http://www.stephanusdai.web.id
 * Copyright (c) ID 2023 Stephanus Bagus Saputra. All rights reserved.
 * Terms of the following https://stephanusdai.web.id/p/license.html
 */

package id.web.stephanusdai.ajaxgetpostjson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.util.Base64
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class AjaxRequestUtil {
    public enum class RequestMethodType {
        RAW, POST, DATA, GET, PUT, DELETE
    }

    public interface OnResponseListener {
        fun onResponse(response: JSONObject?, error: Exception?)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @set:JvmName("setRequestMethod")
    @get:JvmName("getRequestMethod")
    public var requestMethod: RequestMethodType = RequestMethodType.GET

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getBaseUrl")
    @set:JvmName("setBaseUrl")
    public var baseUrl: String? = null

    private var postData = HashMap<String, Any>()
    private var onResponseListener: OnResponseListener? = null
    private var boundary: String? = null
    private var callbackResponse: ((response: JSONObject?, error: Exception?) -> Unit)? = null
    private var urlConnection: URLConnection? = null
    private lateinit var context: Context

    constructor(context: Context) {
        this.context = context
        try {
            this.setOnResponseListener(context as OnResponseListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    constructor(context: Context, baseUrl: String) {
        this.context = context
        this.setBaseUrl(baseUrl)
        try {
            this.setOnResponseListener(context as OnResponseListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun setOnResponseListener(listener: OnResponseListener) {
        this.onResponseListener = listener
    }

    public fun setOnNetworkChanged(callback: (response: JSONObject?, error: Exception?) -> Unit) {
        this.callbackResponse = callback
    }

    private fun attachResponse(response: JSONObject?, error: Exception?) {
        if (this.callbackResponse != null) {
            this.callbackResponse?.invoke(response, error)
        }
        if (this.onResponseListener != null) {
            this.onResponseListener?.onResponse(response, error)
        }
    }

    @JvmName("requestMethod")
    public fun getRequestMethod(): RequestMethodType? {
        return this.requestMethod
    }

    @JvmName("requestMethod")
    public fun setRequestMethod(method: RequestMethodType) {
        this.requestMethod = method
    }

    @JvmName("baseUrl")
    public fun getBaseUrl(): String? {
        return this.baseUrl
    }

    @JvmName("baseUrl")
    public fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    public fun open() {
        this.open(null, null)
    }

    public fun open(pathUrl: String) {
        this.open(pathUrl, null)
    }

    public fun open(callbacks: ((response: JSONObject?, error: Exception?) -> Unit)?) {
        this.open(null, callbacks)
    }

    @Throws(NullPointerException::class)
    public fun open(pathUrl: String?, callbacks: ((response: JSONObject?, error: Exception?) -> Unit)?) {
        var linkUrl = this.baseUrl
        if (this.baseUrl == null) {
            throw NullPointerException("You have to set baseUrl for first!")
        }
        if (pathUrl != null) {
            val posLastSeprator: Int = this.baseUrl!!.lastIndexOf("/")
            val lengthBaseUrl: Int = this.baseUrl!!.length - 1
            if (posLastSeprator <= 0) {
                throw InterruptedException("Put a \"/\" in baseUrl as the main directory!")
            } else if (posLastSeprator != lengthBaseUrl) {
                throw InterruptedException("You must put a \"/\" at the end field of the \"baseUrl\" variable!")
            }

            if (pathUrl.indexOf("/") == 0) {
                linkUrl += pathUrl.substring(1, pathUrl.length)
            } else {
                linkUrl += pathUrl
            }
        }
        this.loadFromAssets(linkUrl!!, callbacks)
    }

    public fun sendPost(key: String, value: Int) {
        this.postData.put(key, value)
    }

    public fun sendPost(key: String, value: Boolean) {
        this.postData.put(key, value)
    }

    public fun sendPost(key: String, value: String) {
        this.postData.put(key, value)
    }

    public fun sendPost(key: String, value: File) {
        this.postData.put(key, value)
    }

    public fun sendPost(key: String, value: CharArray) {
        this.postData.put(key, value)
    }

    @Suppress("DEPRECATION")
    @DelicateCoroutinesApi
    @Throws(IOException::class, NullPointerException::class)
    public fun loadFromAssets(fileName: String, callbacks: ((response: JSONObject?, error: Exception?) -> Unit)?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileAsset = getFileFromAssets(fileName)
                val fileJsonString = convertFileStreamToString(FileInputStream(fileAsset))
                withContext(Dispatchers.Main) {
                    try {
                        val jsonObject = JSONTokener(fileJsonString).nextValue() as JSONObject
                        callbacks?.invoke(jsonObject, null)
                        this@AjaxRequestUtil.attachResponse(jsonObject, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        this@AjaxRequestUtil.loadFromWeb(fileName, callbacks)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                this@AjaxRequestUtil.loadFromWeb(fileName, callbacks)
            }
        }
    }

    @Suppress("DEPRECATION")
    @DelicateCoroutinesApi
    @Throws(IOException::class, NullPointerException::class)
    public fun loadFromWeb(linkUrl: String, callbacks: ((response: JSONObject?, error: Exception?) -> Unit)?) {
        var hostLink = linkUrl
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (this@AjaxRequestUtil.requestMethod == RequestMethodType.GET ||
                    this@AjaxRequestUtil.requestMethod == RequestMethodType.DELETE) {
                    hostLink = this@AjaxRequestUtil.setupParamGetRequest(hostLink)
                }
                if (hostLink.indexOf("http://") == 0) {
                    this@AjaxRequestUtil.urlConnection = URL(hostLink).openConnection()
                    val urlConnectionClass = this@AjaxRequestUtil.urlConnection as HttpURLConnection
                    with(urlConnectionClass) {
                        this@AjaxRequestUtil.setupConnectionOption()
                        this@AjaxRequestUtil.setupDataPostEntries()
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = urlConnectionClass.inputStream.bufferedReader().use { it.readText() }
                            withContext(Dispatchers.Main) {
                                try {
                                    val jsonObject = JSONTokener(response).nextValue() as JSONObject
                                    callbacks?.invoke(jsonObject, null)
                                    this@AjaxRequestUtil.attachResponse(jsonObject, null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    callbacks?.invoke(null, e)
                                    this@AjaxRequestUtil.attachResponse(null, e)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                val ex = Exception("ERROR_CONNECTION_" + responseCode)
                                callbacks?.invoke(null, ex)
                                this@AjaxRequestUtil.attachResponse(null, ex)
                            }
                        }
                    }
                } else if (hostLink.indexOf("https://") == 0) {
                    this@AjaxRequestUtil.urlConnection = URL(hostLink).openConnection()
                    val urlConnectionClass = this@AjaxRequestUtil.urlConnection as HttpsURLConnection
                    with(urlConnectionClass) {
                        this@AjaxRequestUtil.setupConnectionOption()
                        this@AjaxRequestUtil.setupDataPostEntries()

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = urlConnectionClass.inputStream.bufferedReader().use { it.readText() }
                            withContext(Dispatchers.Main) {
                                try {
                                    val jsonObject = JSONTokener(response).nextValue() as JSONObject
                                    callbacks?.invoke(jsonObject, null)
                                    this@AjaxRequestUtil.attachResponse(jsonObject, null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    callbacks?.invoke(null, e)
                                    this@AjaxRequestUtil.attachResponse(null, e)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                val ex = Exception("ERROR_CONNECTION_" + responseCode)
                                callbacks?.invoke(null, ex)
                                this@AjaxRequestUtil.attachResponse(null, ex)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    callbacks?.invoke(null, e)
                    this@AjaxRequestUtil.attachResponse(null, e)
                }
            }

        }
    }

    public fun getBitmapFromUrl(urlLink: String): Bitmap {
        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        return BitmapFactory.decodeStream(URL(urlLink).openConnection().getInputStream())
    }

    private fun setupParamGetRequest(linkUrl: String): String {
        var urlAddress = linkUrl
        val uriBuilder = Uri.Builder()
        with (uriBuilder) {
            for ((key, value) in this@AjaxRequestUtil.postData) {
                when(value) {
                    String -> {
                        appendQueryParameter(key, value.toString())
                    }
                    Int -> {
                        appendQueryParameter(key, value.toString())
                    }
                }
                if (value is File) {
                    appendQueryParameter(key, convertFileToBase64(value as File))
                }
            }
        }
        uriBuilder.build()
        val params = uriBuilder.toString().replace("?", "")
        if (urlAddress.indexOf("?") > 0) {
            urlAddress += "&" + params
        } else {
            urlAddress += "?" + params
        }
        return urlAddress
    }

    private fun setupDataPostEntries() {
        if (this.urlConnection == null) {
            throw NullPointerException("URL Connection Class is not set!")
        }
        if (this@AjaxRequestUtil.boundary == null) {
            this@AjaxRequestUtil.boundary = "Boundary-${System.currentTimeMillis()}"
        }
        val boundary = this@AjaxRequestUtil.boundary
        when(this.requestMethod) {
            RequestMethodType.RAW -> {
                val jsonObject = JSONObject()
                with(jsonObject) {
                    for ((key, value) in this@AjaxRequestUtil.postData) {
                        when(value) {
                            is File -> {
                                //put(key, convertFileToBase64(value))
                            }
                            else -> {
                                put(key, value.toString())
                            }
                        }
                    }
                }
                val jsonObjectString = jsonObject.toString()
                val outputStreamWriter = OutputStreamWriter(this.urlConnection!!.outputStream)
                outputStreamWriter.write(jsonObjectString)
                outputStreamWriter.flush()
            }
            RequestMethodType.POST -> {
                val uriBuilder = Uri.Builder()
                val outputStreamToRequestBody = this.urlConnection!!.outputStream
                with (uriBuilder) {
                    for ((key, value) in this@AjaxRequestUtil.postData) {
                        when(value) {
                            is File -> {
                                //appendQueryParameter(key, convertFileToBase64(value))
                            }
                            else -> {
                                appendQueryParameter(key, value.toString())
                            }
                        }
                    }
                }
                uriBuilder.build()

                val params = uriBuilder.toString().replace("?", "")  // Remove the "?" from the beginning of the parameters ?name=Jack&salary=8054&age=45
                val postData = params.toByteArray(StandardCharsets.UTF_8)
                val dataOutputStream = DataOutputStream(this.urlConnection!!.outputStream)
                dataOutputStream.write(postData)
            }
            RequestMethodType.DATA -> {
                val outputStreamToRequestBody = this.urlConnection!!.outputStream
                val httpRequestBodyWriter = BufferedWriter(OutputStreamWriter(outputStreamToRequestBody))
                with (httpRequestBodyWriter) {
                    for ((key, value) in this@AjaxRequestUtil.postData) {
                        when(value) {
                            is String -> {
                                write("\n\n--$boundary\n")
                                write("Content-Disposition: form-data; name=\"$key\"")
                                write("\n\n")
                                write(value as String)
                            }
                            is Boolean -> {
                                write("\n\n--$boundary\n")
                                write("Content-Disposition: form-data; name=\"$key\"")
                                write("\n\n")
                                write(value.toString())
                            }
                            is Int -> {
                                write("\n\n--$boundary\n")
                                write("Content-Disposition: form-data; name=\"$key\"")
                                write("\n\n")
                                write(value as Int)
                            }
                            is CharArray -> {
                                write("\n\n--$boundary\n")
                                write("Content-Disposition: form-data; name=\"$key\"")
                                write("\n\n")
                                write(value as CharArray)
                            }
                            is File -> {
                                val fileUpload = value as File
                                val mime = getMimeType(fileUpload)
                                write("\n\n--$boundary\n")
                                write("Content-Disposition: form-data;"
                                        + "name=\"$key\";"
                                        + "filename=\"" + fileUpload.name + "\""
                                        + "\nContent-Type: $mime\n\n")
                                flush()

                                val inputStreamToFile = FileInputStream(fileUpload)
                                var bytesRead: Int
                                val dataBuffer = ByteArray(1024)
                                while (inputStreamToFile.read(dataBuffer).also { bytesRead = it } != -1) {
                                    outputStreamToRequestBody.write(dataBuffer, 0, bytesRead)
                                }
                                outputStreamToRequestBody.flush()
                            }
                        }
                    }
                    write("\n--$boundary--\n")
                    flush()
                    outputStreamToRequestBody.close()
                    close()
                }
            }
            RequestMethodType.PUT -> {
                val jsonObject = JSONObject()
                with(jsonObject) {
                    for ((key, value) in this@AjaxRequestUtil.postData) {
                        when(value) {
                            is File -> {
                                //put(key, convertFileToBase64(value))
                            }
                            else -> {
                                put(key, value.toString())
                            }
                        }
                    }
                }
                val jsonObjectString = jsonObject.toString()
                val outputStreamWriter = OutputStreamWriter(this.urlConnection!!.outputStream)
                outputStreamWriter.write(jsonObjectString)
                outputStreamWriter.flush()
            }
            else -> {}
        }
    }

    private fun setupConnectionOption() {
        if (this.urlConnection == null) {
            throw NullPointerException("URL Connection Class is not set!")
        }
        if (urlConnection is HttpsURLConnection) {
            with(this.urlConnection as HttpsURLConnection) {
                setRequestProperty("Accept", "application/json")
                when(this@AjaxRequestUtil.requestMethod) {
                    RequestMethodType.RAW -> {
                        setRequestProperty("Content-Type", "application/json")
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.POST -> {
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.DATA -> {
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                        this@AjaxRequestUtil.boundary = "Boundary-${System.currentTimeMillis()}"
                        addRequestProperty(
                            "Content-Type",
                            "multipart/form-data; boundary=" + this@AjaxRequestUtil.boundary
                        )
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.PUT -> {
                        setRequestProperty("Content-Type", "application/json")
                        requestMethod = "PUT"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.DELETE -> {
                        requestMethod = "DELETE"
                        doInput = true
                        doOutput = false
                    }
                    else -> {
                        requestMethod = "GET"
                        doInput = true
                        doOutput = false
                    }
                }
            }
        } else if (urlConnection is HttpURLConnection) {
            with(this.urlConnection as HttpURLConnection) {
                setRequestProperty("Accept", "application/json")
                when(this@AjaxRequestUtil.requestMethod) {
                    RequestMethodType.RAW -> {
                        setRequestProperty("Content-Type", "application/json")
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.POST -> {
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.DATA -> {
                        this@AjaxRequestUtil.boundary = "Boundary-${System.currentTimeMillis()}"
                        addRequestProperty(
                            "Content-Type",
                            "multipart/form-data; boundary=" + this@AjaxRequestUtil.boundary
                        )
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.PUT -> {
                        setRequestProperty("Content-Type", "application/json")
                        requestMethod = "PUT"
                        doInput = true
                        doOutput = true
                    }
                    RequestMethodType.DELETE -> {
                        requestMethod = "DELETE"
                        doInput = true
                        doOutput = false
                    }
                    else -> {
                        requestMethod = "GET"
                        doInput = true
                        doOutput = false
                    }
                }
            }
        }
    }

    private fun getMimeType(file: File): String? {
        val uri = Uri.fromFile(file)
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun getJsonDataFromAssets(fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun convertFileStreamToString(inputStream: FileInputStream): String? {
        val reader = BufferedReader(inputStream.reader())
        var content: String
        try {
            content = reader.readText()
        } finally {
            reader.close()
        }
        return content
    }

    private fun getFileFromAssets(fileName: String): File = File(context.cacheDir, fileName).also {
        if (!it.exists()) {
            it.outputStream().use { cache ->
                context.assets.open(fileName).use { inputStream ->
                    inputStream.copyTo(cache)
                }
            }
        }
    }

    private fun convertFileToBase64(attachment: File): String {
        return Base64.encodeToString(attachment.readBytes(), Base64.NO_WRAP)
    }
}
