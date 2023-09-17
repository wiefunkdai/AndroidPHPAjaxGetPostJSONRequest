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

package id.web.stephanusdai.ajaxgetpostjson.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import id.web.stephanusdai.ajaxgetpostjson.AjaxRequestUtil
import id.web.stephanusdai.ajaxgetpostjson.AuthorModel
import id.web.stephanusdai.ajaxgetpostjson.EmployeeModel
import id.web.stephanusdai.ajaxgetpostjson.MediaHelper
import id.web.stephanusdai.ajaxgetpostjson.R
import id.web.stephanusdai.ajaxgetpostjson.RVAuthorAdapter
import id.web.stephanusdai.ajaxgetpostjson.RVEmployeeAdapter
import id.web.stephanusdai.ajaxgetpostjson.RVResultAdapter
import id.web.stephanusdai.ajaxgetpostjson.ResultModel
import id.web.stephanusdai.ajaxgetpostjson.databinding.ActivityRequestFormBinding
import id.web.stephanusdai.ajaxgetpostjson.databinding.ActivityRequestResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.UUID


class RequestSendActivity : AppCompatActivity(), AjaxRequestUtil.OnResponseListener  {
    private var filePhotoUpload: Uri? = null
    private var itemsArray: ArrayList<ResultModel> = ArrayList()
    private lateinit var itemAdapter: RVResultAdapter
    private lateinit var ajaxRequestUtil: AjaxRequestUtil
    private lateinit var formBinding: ActivityRequestFormBinding
    private lateinit var resultBinding: ActivityRequestResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityRequestResultBinding.inflate(layoutInflater)
        formBinding = ActivityRequestFormBinding.inflate(layoutInflater)
        setContentView(formBinding.root)
        formBinding.progLoader.visibility = View.INVISIBLE
        formBinding.previewPhoto.setOnClickListener({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else{
                    chooseFromImageGallery();
                }
            }else{
                chooseFromImageGallery();
            }
        })
        formBinding.btnSend.setOnClickListener({
            try {
                formBinding.btnSend.isEnabled = false
                formBinding.progLoader.visibility = View.VISIBLE
                ajaxRequestUtil = AjaxRequestUtil(this,"https://raw.githubusercontent.com/")
                ajaxRequestUtil.setRequestMethod(AjaxRequestUtil.RequestMethodType.DATA)
                ajaxRequestUtil.sendPost("name", formBinding.nameText.text.toString())
                ajaxRequestUtil.sendPost("email", formBinding.emailText.text.toString())
                ajaxRequestUtil.sendPost("comment", formBinding.commentText.text.toString())
                if (this.filePhotoUpload != null) {
                    val fileName = MediaHelper.getPath(this, this.filePhotoUpload!!)
                    ajaxRequestUtil.sendPost("avatar", File(fileName))
                }
                ajaxRequestUtil.open("/wiefunkdai/AndroidAjaxGetPostRequest/master/app/src/main/assets/ajaxrequest.php")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseFromImageGallery()
                }else{
                    Toast.makeText(this,"You must to grant for permission device!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_CHOOSE_CODE && resultCode == Activity.RESULT_OK){
            val filePhoto = data?.data
            if (filePhoto != null) {
                val imageFile = MediaHelper.getBitmapFromUri(this, filePhoto)
                with(formBinding.previewPhoto) {
                    try {
                        setBackgroundColor(Color.TRANSPARENT)
                        setImageBitmap(imageFile)
                        setScaleType(ImageView.ScaleType.FIT_XY)
                        with(getLayoutParams() as ViewGroup.LayoutParams) {
                            width  = 300
                            height = 300
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        setVisibility(View.INVISIBLE)
                    }
                }
                this.filePhotoUpload = filePhoto
            }
        }
    }

    @SuppressLint("LongLogTag")
    override fun onResponse(response: JSONObject?, error: Exception?) {
        if (error != null) {
            formBinding.btnSend.isEnabled = true
            formBinding.progLoader.visibility = View.INVISIBLE
            Toast.makeText(this,error?.message.toString(), Toast.LENGTH_SHORT).show()
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                if (error == null && response != null) {
                    withContext(Dispatchers.Main) {
                        setContentView(resultBinding.root)
                        setupRecyclerView()
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(JsonParser.parseString(response.toString()))
                        // Alternative within: response.toString(2)
                        resultBinding.resultOverview.text = prettyJson

                        val data = response.getJSONObject("content")

                        with(resultBinding.resultsPhoto) {
                            try {
                                val imagePhoto = ajaxRequestUtil.getBitmapFromUrl(data.getString("photo"))
                                setBackgroundColor(Color.TRANSPARENT)
                                setImageBitmap(imagePhoto)
                                setScaleType(ImageView.ScaleType.FIT_XY)
                                with(getLayoutParams() as ViewGroup.LayoutParams) {
                                    width  = 300
                                    height = 300
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                setVisibility(View.INVISIBLE)
                            }
                        }
                        try {
                            val resultName = data.getString("name")
                            val resultEmail = data.getString("email")
                            val resultComment = data.getString("comment")

                            val resultModel = ResultModel(
                                resultName,
                                resultEmail,
                                resultComment
                            )
                            itemsArray.add(resultModel)

                            itemAdapter = RVResultAdapter(itemsArray)
                            itemAdapter.notifyDataSetChanged()
                        } finally {
                            resultBinding.resultSummary.adapter = itemAdapter
                        }
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun chooseFromImageGallery() {
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, IMAGE_CHOOSE_CODE)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        resultBinding.resultSummary.layoutManager = layoutManager
        resultBinding.resultSummary.setHasFixedSize(true)
        val dividerItemDecoration =
            DividerItemDecoration(
                resultBinding.resultSummary.context,
                layoutManager.orientation
            )
        ContextCompat.getDrawable(this, R.drawable.ic_line_divider)
            ?.let { drawable ->
                dividerItemDecoration.setDrawable(drawable)
            }
        resultBinding.resultSummary.addItemDecoration(dividerItemDecoration)
    }

   companion object {
       private const val PERMISSION_CODE = 111
       private const val IMAGE_CHOOSE_CODE = 112
   }
}