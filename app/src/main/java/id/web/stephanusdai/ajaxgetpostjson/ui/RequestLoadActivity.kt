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

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import id.web.stephanusdai.ajaxgetpostjson.AjaxRequestUtil
import id.web.stephanusdai.ajaxgetpostjson.AuthorModel
import id.web.stephanusdai.ajaxgetpostjson.EmployeeModel
import id.web.stephanusdai.ajaxgetpostjson.R
import id.web.stephanusdai.ajaxgetpostjson.RVAuthorAdapter
import id.web.stephanusdai.ajaxgetpostjson.RVEmployeeAdapter
import id.web.stephanusdai.ajaxgetpostjson.databinding.ActivityRequestGetBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.ArrayList

class RequestLoadActivity : AppCompatActivity(), AjaxRequestUtil.OnResponseListener  {
    private var authorsArray: ArrayList<AuthorModel> = ArrayList()
    private var employeesArray: ArrayList<EmployeeModel> = ArrayList()
    private lateinit var authorAdapter: RVAuthorAdapter
    private lateinit var employeeAdapter: RVEmployeeAdapter
    private lateinit var ajaxRequestUtil: AjaxRequestUtil
    private lateinit var binding: ActivityRequestGetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestGetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setupRecyclerView()
            ajaxRequestUtil = AjaxRequestUtil(this,"sampledata.json")
            ajaxRequestUtil.setRequestMethod(AjaxRequestUtil.RequestMethodType.GET)
            ajaxRequestUtil.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("LongLogTag")
    override fun onResponse(response: JSONObject?, error: Exception?) {
        if (error != null) {
            Toast.makeText(this,error?.message.toString(), Toast.LENGTH_SHORT).show()
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                if (error == null && response != null) {
                    withContext(Dispatchers.Main) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(JsonParser.parseString(response.toString()))
                        // Alternatif within: response.toString(2)
                        binding.resultOverview.text = prettyJson
                        with(binding.resultsPhoto) {
                            try {
                                val imagePhoto = ajaxRequestUtil.getBitmapFromUrl(response.getString("photo"))
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
                            val authorName = response.getString("author")
                            val authorLink = response.getString("link")

                            val childCell = response.getJSONObject("childdata")
                            val linkGithub = childCell.getString("github")

                            val sosmedCell = childCell.getJSONObject("sosmed")
                            val linkYoutube = sosmedCell.getString("youtube")

                            val authorModel = AuthorModel(
                                authorName,
                                authorLink,
                                linkYoutube,
                                linkGithub
                            )
                            authorsArray.add(authorModel)

                            authorAdapter = RVAuthorAdapter(authorsArray)
                            authorAdapter.notifyDataSetChanged()
                        } finally {
                            binding.resultSummary.adapter = authorAdapter
                        }

                        binding.buttonList.setOnClickListener({
                            try {
                                binding.resultsPhoto.visibility = View.GONE

                                val jsonArray = response.getJSONArray("multidata")

                                for (i in 0 until jsonArray.length()) {
                                    val employeeId = jsonArray.getJSONObject(i).getString("id")

                                    val employee = jsonArray.getJSONObject(i).getJSONObject("employee")
                                    val employeeName = employee.getString("name")
                                    val employeeSalary = employee.getJSONObject("salary")
                                    val employeeSalaryUSD = employeeSalary.getInt("usd")
                                    val employeeSalaryEUR = employeeSalary.getInt("eur")
                                    val employeeAge = employee.getString("age")

                                    val employeesModel = EmployeeModel(
                                        employeeId,
                                        employeeName,
                                        "$ $employeeSalaryUSD / € $employeeSalaryEUR",
                                        employeeAge
                                    )
                                    employeesArray.add(employeesModel)

                                    employeeAdapter = RVEmployeeAdapter(employeesArray)
                                    employeeAdapter.notifyDataSetChanged()
                                }
                            } finally {
                                binding.resultSummary.adapter = employeeAdapter
                                binding.buttonList.visibility = View.GONE
                            }
                        })
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.resultSummary.layoutManager = layoutManager
        binding.resultSummary.setHasFixedSize(true)
        val dividerItemDecoration =
            DividerItemDecoration(
                binding.resultSummary.context,
                layoutManager.orientation
            )
        ContextCompat.getDrawable(this, R.drawable.ic_line_divider)
            ?.let { drawable ->
                dividerItemDecoration.setDrawable(drawable)
            }
        binding.resultSummary.addItemDecoration(dividerItemDecoration)
    }
}