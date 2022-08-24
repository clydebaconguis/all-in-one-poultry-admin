package com.example.adminpoultry.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.adminpoultry.R
import com.example.adminpoultry.adapter.OrdersAdapter
import com.example.adminpoultry.config.ApiUrlRoutes
import com.example.adminpoultry.model.OrdersModel
import org.json.JSONArray


class DashboardFragment: Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var loading : ProgressBar
    private lateinit var recyclerView: RecyclerView
    private var list = ArrayList<OrdersModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading =  view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerOrders)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        fetchData()
    }

    private fun fetchData() {
        loading.isVisible =  true
        val url = ApiUrlRoutes().transactions
        val stringRequest= object : StringRequest(
            Method.GET,url,
            Response.Listener{
                loading.isVisible =  false
                parseJson(it)
            },
            Response.ErrorListener {
                loading.isVisible =  false
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
                val code = jo.getString("trans_code")
                val status = jo.getString("status")

                list.add(OrdersModel(id, code, status))
                index++
            }

            recyclerView.adapter = OrdersAdapter(requireContext(),list)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}