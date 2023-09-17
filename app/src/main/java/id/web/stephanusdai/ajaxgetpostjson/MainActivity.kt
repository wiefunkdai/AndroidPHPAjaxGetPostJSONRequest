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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.web.stephanusdai.ajaxgetpostjson.databinding.ActivityMainBinding
import id.web.stephanusdai.ajaxgetpostjson.ui.RequestGetActivity
import id.web.stephanusdai.ajaxgetpostjson.ui.RequestLoadActivity
import id.web.stephanusdai.ajaxgetpostjson.ui.RequestSendActivity

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ajaxButtonGet.setOnClickListener {
            val context = this@MainActivity
            val intent = Intent(context, RequestGetActivity::class.java)
            context.startActivity(intent)
        }
        binding.ajaxButtonSend.setOnClickListener {
            val context = this@MainActivity
            val intent = Intent(context, RequestSendActivity::class.java)
            context.startActivity(intent)
        }
        binding.ajaxButtonLoad.setOnClickListener {
            val context = this@MainActivity
            val intent = Intent(context, RequestLoadActivity::class.java)
            context.startActivity(intent)
        }
    }
}