package com.example.adminpoultry

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.adminpoultry.config.ApiUrlRoutes
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.RuntimeException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min

class CreateProduct : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageButton: Button
    private lateinit var sendButton: Button
    private lateinit var productName: EditText
    private lateinit var autoCompleteTv : AutoCompleteTextView
    private lateinit var autoCompleteTv2: AutoCompleteTextView
    private lateinit var pPrice : TextInputEditText
    private lateinit var btnAddPrice : Button
    private lateinit var container : LinearLayoutCompat
    private lateinit var loading: ProgressBar
    val type = ArrayList<type>()
    val _dType = ArrayList<String>()
    val unit = ArrayList<type>()
    val _dUnit = ArrayList<String>()
    private var imageData: ByteArray? = null

        private val loadImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageView.setImageURI(it)
        if (it != null) {
            createImageData(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Product"
        loading = findViewById(R.id.progressBar)
        productName = findViewById(R.id.productName)
        container =  findViewById(R.id.container)
        btnAddPrice = findViewById(R.id.btnAddPrice)
        btnAddPrice.setOnClickListener {
            addViewPrice()
        }
        imageView = findViewById(R.id.imageView)
        imageButton = findViewById(R.id.imageButton)
        imageButton.setOnClickListener {
            loadImage.launch("image/*")
        }
        sendButton = findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            if (valid()){
                uploadProduct()
            }else{
                Toast.makeText(this,"Fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        addViewPrice()
        dropdown()

    }

    private fun valid(): Boolean {
        var valid = true
        val childCount = container.getChildCount()
        var index = 0
        while (index < childCount && valid) {
            val element = container.getChildAt(index)
            if (element is ViewGroup){
                val vValue = element.findViewById<TextInputEditText>(R.id.pPrice)
                val vType = element.findViewById<AutoCompleteTextView>(R.id.autoCompleteTv)
                val vUnit = element.findViewById<AutoCompleteTextView>(R.id.autoCompleteTv2)
                if (vValue.text.toString().isEmpty() || vType.text.toString().isEmpty() || vUnit.text.toString().isEmpty()){
                    valid = false
                    break
                }
            }
            index++
        }
        return valid
    }

    private fun addViewPrice() {
        val priceView = layoutInflater.inflate(R.layout.row_add_price,null,false)
        pPrice =  priceView.findViewById(R.id.pPrice)
        autoCompleteTv = priceView.findViewById(R.id.autoCompleteTv)
        autoCompleteTv2 = priceView.findViewById(R.id.autoCompleteTv2)
        val btnRemoveView = priceView.findViewById<ImageView>(R.id.btnRemoveView)

        container.addView(priceView)
        val adapter = ArrayAdapter(this, R.layout.dropdown_type_list_item, _dType)
        autoCompleteTv.setAdapter(adapter)

        val adapter2 = ArrayAdapter(this, R.layout.dropdown_type_list_item, _dUnit)
        autoCompleteTv2.setAdapter(adapter2)

        autoCompleteTv.setOnItemClickListener { parent, view, position, id ->
            getUnitDropdown(position)
        }
        btnRemoveView.setOnClickListener {
            container.removeView(priceView)
        }
    }

    private fun getUnitDropdown(position: Int){
        _dUnit.clear()
        val tarray = type[position]
        val tId = tarray.id
        unit.forEach {
            val id = it.id
            if (id == tId){
                _dUnit.add(it.name)
            }
        }
        val adapter = ArrayAdapter(this, R.layout.dropdown_type_list_item, _dUnit)
        autoCompleteTv2.setAdapter(adapter)

    }

    private fun dropdown(){
        type.clear()
        unit.clear()
        val url = ApiUrlRoutes().getTypes
        val stringRequest= object : StringRequest(
            Method.GET,url,
            Response.Listener{
                loading.isVisible = false
                parseJson(it)
            },
            Response.ErrorListener {
                loading.isVisible = false
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)

    }

    private fun parseJson(jsonResponse: String){
        try {
            val jo = JSONObject(jsonResponse)
            val ta = jo.getJSONArray("type")
            val ua = jo.getJSONArray("unit")
            parseType(ta)
            parseUnit(ua)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun parseUnit(ja: JSONArray) {
        var index = 0
        while (index < ja.length() ){
            val jo = ja.getJSONObject(index)
            val id = jo.getInt("type_id")
            val name = jo.getString("unit")
            unit.add(type(id,name))
            _dUnit.add(name)
            index++
        }
        val adapter = ArrayAdapter(this, R.layout.dropdown_type_list_item, _dUnit)
        autoCompleteTv2.setAdapter(adapter)
    }

    private fun parseType(ja: JSONArray) {
        var index = 0
        while (index < ja.length() ){
            val jo = ja.getJSONObject(index)
            val id = jo.getInt("id")
            val name = jo.getString("name")
            type.add(type(id,name))
            _dType.add(name)
            index++
        }
        val adapter = ArrayAdapter(this, R.layout.dropdown_type_list_item, _dType)
        autoCompleteTv.setAdapter(adapter)
    }

    private fun uploadProduct(){
        imageData?: return
        loading.isVisible = true
        val jsonPrices = JSONArray()
        val childCount = container.getChildCount()
        var index = 0
        while (index < childCount) {
            val element = container.getChildAt(index)
            if (element is ViewGroup){
                val vValue = element.findViewById<TextInputEditText>(R.id.pPrice)
                val vType = element.findViewById<AutoCompleteTextView>(R.id.autoCompleteTv)
                val vUnit = element.findViewById<AutoCompleteTextView>(R.id.autoCompleteTv2)

                val jsonPrice = JSONObject()

                jsonPrice.put("value", vValue.text)
                jsonPrice.put("type", vType.text)
                jsonPrice.put("unit", vUnit.text)

                jsonPrices.put(jsonPrice)
            }

            index++

        }

        val url = ApiUrlRoutes().getProducts
        val request = object:VolleyFileUploadRequest(
            Method.POST,url,
            Response.Listener{
                loading.isVisible = false
                Toast.makeText(this,"Product Added!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            },
            Response.ErrorListener {
                loading.isVisible = false
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }){

            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params= HashMap<String, FileDataPart>()
                params["image"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }

            override fun getParams(): MutableMap<String, String> {
                val params= HashMap<String, String>()
                params["name"] = productName.text.toString()
                params["prices"] = jsonPrices.toString()

                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun createImageData(uri: Uri){
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use{
            imageData = it.readBytes()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

data class PriceData(val price:Double, val type:String, val unit:String)

data class type(val id:Int, val name:String)

open class VolleyFileUploadRequest(
    method:Int,
    url:String,
    listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener) : Request<NetworkResponse>(method, url, errorListener){
        private var responseListener: Response.Listener<NetworkResponse>? = null
        init {
            this.responseListener = listener
        }

        private var headers: Map<String, String>? = null
        private val divider: String = "--"
        private val ending = "\r\n"
        private val boundary = "imageRequest${System.currentTimeMillis()}"


        override fun getHeaders(): MutableMap<String, String> {
            return when (headers) {
                null -> super.getHeaders()
                else -> headers!!.toMutableMap()
            }
        }

        override fun getBodyContentType() = "multipart/form-data;boundary=$boundary"

        @Throws(AuthFailureError::class)
        override fun getBody(): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            try {
                if (params!=null && params!!.isNotEmpty()){
                    processParams(dataOutputStream, params!!, paramsEncoding)
                }
                val data = getByteData() as Map<String, FileDataPart>?
                if(data!= null && data.isNotEmpty()){
                    processData(dataOutputStream, data)
                }
                dataOutputStream.writeBytes(divider + boundary + divider + ending)
                return byteArrayOutputStream.toByteArray()

            }catch (e: IOException){
                e.printStackTrace()
            }
            return super.getBody()
        }

        @Throws(AuthFailureError::class)
        open fun getByteData(): Map<String, Any>?{
            return null
        }

        override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {
            return try {
                Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
            }catch (e: Exception){
                Response.error(ParseError(e))
            }
        }

        override fun deliverResponse(response: NetworkResponse?) {
            responseListener?.onResponse(response)
        }

        override fun deliverError(error: VolleyError?) {
            errorListener?.onErrorResponse(error)
        }

        @Throws(IOException::class)
        private fun processParams(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding:String){
            try {
                params.forEach {
                    dataOutputStream.writeBytes(divider + boundary + ending)
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"$ending")
                    dataOutputStream.writeBytes(ending)
                    dataOutputStream.writeBytes(it.value + ending)
                }
            }catch (e: UnsupportedEncodingException){
                throw RuntimeException("Unsupported encoding not supported: $encoding with error: ${e.message}", e)
            }
        }

        @Throws(IOException::class)
        private fun processData(dataOutputStream: DataOutputStream, data: Map<String, FileDataPart>){
            data.forEach {
                val dataFile = it.value
                dataOutputStream.writeBytes("$divider$boundary$ending")
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"; filename=\"${dataFile.filename}\"$ending")
                if (dataFile.type.trim().isNotEmpty()){
                    dataOutputStream.writeBytes("Content-Type: ${dataFile.type}$ending")
                }
                dataOutputStream.writeBytes(ending)
                val fileInputStream = ByteArrayInputStream(dataFile.data)
                var bytesAvailable = fileInputStream.available()
                val maxBufferSize = 1024 * 1024
                var bufferSize = min(bytesAvailable, maxBufferSize)
                val buffer = ByteArray(bufferSize)
                var bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0){
                    dataOutputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize =  min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }
                dataOutputStream.writeBytes(ending)
            }
        }

    }

class FileDataPart(var filename:String?, var data:ByteArray, var type:String)
