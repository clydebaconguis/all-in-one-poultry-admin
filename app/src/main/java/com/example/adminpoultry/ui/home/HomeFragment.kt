package com.example.adminpoultry.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.adminpoultry.CreateProduct
import com.example.adminpoultry.R
import com.example.adminpoultry.adapter.ProductAdapter
import com.example.adminpoultry.databinding.FragmentHomeBinding
import com.example.adminpoultry.model.ProductModel
import com.example.adminpoultry.config.ApiUrlRoutes
import com.example.adminpoultry.type
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var list = ArrayList<ProductModel>()
    private var typeList = ArrayList<type>()
    private lateinit var recyclerMain : RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var progressBarHome:ProgressBar
    private lateinit var tblType: TableLayout
    private lateinit var inputType:TextInputEditText
    private lateinit var inputUnit:TextInputEditText
    private lateinit var btnSave : MaterialButton
    private lateinit var btnToggle : MaterialButton
    private lateinit var isEmptyTable : TextView
    private lateinit var container: LinearLayoutCompat
    var isToggled = false
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {val dashboardViewModel =
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchData()
        fetchType()
        isEmptyTable = view.findViewById(R.id.isEmptyTable)
        tblType = view.findViewById(R.id.tblType)
        inputType = view.findViewById(R.id.inputType)
        inputUnit = view.findViewById(R.id.inputUnit)
        container = view.findViewById(R.id.contInput)
        btnSave = view.findViewById(R.id.btnSave)
        btnToggle = view.findViewById(R.id.btnToggle)
        floatingActionButton = view.findViewById(R.id.floatingActionButton)
        recyclerMain = view.findViewById(R.id.recyclerMain)
        recyclerMain.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerMain.setHasFixedSize(true)
        progressBarHome = view.findViewById(R.id.progressBarHome)
        container.isVisible = isToggled
        floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateProduct::class.java)
            startActivity(intent)
        }
        btnSave.setOnClickListener {
            if (inputType.text.toString().isNotEmpty() && inputUnit.text.toString().isNotEmpty()){
                save()
            }else{
                Toast.makeText(requireContext(), "Fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
        btnToggle.setOnClickListener {
            isToggled = !isToggled
            container.isVisible = isToggled
        }
        if (typeList.size > 0){
            isEmptyTable.isVisible = false
        }
    }

    private fun save() {
        progressBarHome.isVisible = true
        val url = ApiUrlRoutes().getTypes
        val stringRequest= object : StringRequest(
            Method.POST,url,
            Response.Listener{
                progressBarHome.isVisible = false
                val msg = JSONObject(it).getString("message")
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                fetchType()
                tblType.removeAllViews()
                inputType.setText("")
                inputUnit.setText("")
            },
            Response.ErrorListener {
                progressBarHome.isVisible = false
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }){

            override fun getParams(): MutableMap<String, String> {
                val params= HashMap<String, String>()
                params["name"] = inputType.text.toString()
                params["units"] = inputUnit.text.toString().trim()

                return params
            }
        }

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(stringRequest)
    }
    private fun parseType(it: String) {
        try {
            typeList.clear()
            var index = 0
            val jo = JSONObject(it)
            val ta = jo.getJSONArray("type")
            while(index < ta.length()){
                val tobj = ta.getJSONObject(index)
                val typeId = tobj.getInt("id")
                val typeTitle = tobj.getString("name")
                typeList.add(type(typeId, typeTitle))
                index++
            }
            addRowType()

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun addRowType() {
        var count = 0
        tblType.removeAllViews()
        if (typeList.size > 0){
            isEmptyTable.isVisible = false
            typeList.forEach {
                val typeView = layoutInflater.inflate(R.layout.types_item_row,null,false)
                val typeId =  typeView.findViewById<TextView>(R.id.typeId)
                val typeTitle = typeView.findViewById<TextView>(R.id.typeTitle)
                val btnDelete = typeView.findViewById<ImageView>(R.id.btn_delete)
                val btnEdit = typeView.findViewById<ImageView>(R.id.btn_edit)
                val btnShow = typeView.findViewById<ImageView>(R.id.btn_show)
                val tblRow = typeView.findViewById<TableRow>(R.id.tblRow)
                typeId.text = it.id.toString()
                typeTitle.text = it.name.replaceFirstChar { it.uppercase() }
                if ((count % 2) == 0){
                    tblRow.setBackgroundColor(Color.parseColor("#E8E7E7"))
                }

                btnDelete.setOnClickListener {
                    tblType.removeView(typeView)
                    deleteType(typeId.text.toString().toInt())
                }

                tblType.addView(typeView)
                count++
            }
        }else if (typeList.size <= 0 && tblType.childCount <= 0){
           isEmptyTable.isVisible = true
        }
    }
    private fun deleteType(id: Int) {
        progressBarHome.isVisible = true
        val url = ApiUrlRoutes(id).deleteTypes
        val stringRequest= object : StringRequest(
            Method.DELETE,url,
            Response.Listener{
                progressBarHome.isVisible = false
                Toast.makeText(requireContext(), "Deleted!$it", Toast.LENGTH_SHORT).show()
                if (tblType.childCount <= 0){
                    isEmptyTable.isVisible = true
                }
            },
            Response.ErrorListener {
                progressBarHome.isVisible = false
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(stringRequest)
    }

    private fun fetchType() {
        val url = ApiUrlRoutes().getTypes
        val stringRequest= object : StringRequest(
            Method.GET,url,
            Response.Listener{
                progressBarHome.isVisible = false
                parseType(it)
            },
            Response.ErrorListener {
                progressBarHome.isVisible = false
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(stringRequest)
    }


    private fun fetchData() {
        list.clear()
        val url = ApiUrlRoutes().getProducts
        val stringRequest= object : StringRequest(
            Method.GET,url,
            Response.Listener{
                parseJson(it)
                progressBarHome.isVisible = false
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(stringRequest)
    }

    private fun parseJson(jsonResponse: String){
        try {
            val ja = JSONArray(jsonResponse)
            var index = 0
            while (index < ja.length() ){
                val jo = ja.getJSONObject(index)
                val id = jo.getInt("id")
                val name = jo.getString("name")
                val img = jo.getString("image")
//                Toast.makeText(requireContext(), name, Toast.LENGTH_SHORT).show()
                index++
                list.add(ProductModel(id, name, img))
            }
            recyclerMain.adapter =  ProductAdapter(requireContext(),list)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}