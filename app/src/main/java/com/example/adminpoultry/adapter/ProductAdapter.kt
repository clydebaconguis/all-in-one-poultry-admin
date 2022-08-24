package com.example.adminpoultry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.adminpoultry.R
import com.example.adminpoultry.config.ApiUrlRoutes
import com.example.adminpoultry.model.ProductModel
import com.squareup.picasso.Picasso

class ProductAdapter(private val context: Context, private val itemList:ArrayList<ProductModel>): RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val productName : TextView = itemView.findViewById(R.id.product_title)
        val productImg:  ImageView = itemView.findViewById(R.id.product_img)
        val btnDelete:  ImageView = itemView.findViewById(R.id.btn_delete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_item_row, parent, false)
        return MyViewHolder(view)
    }

    fun deleteProduct(id: Int) {
        val url = ApiUrlRoutes(id).deleteProduct
        val stringRequest= object : StringRequest(
            Method.DELETE,url,
            Response.Listener{
                Toast.makeText(context, "Deleted!$it", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(context)
        queue.add(stringRequest)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.productName.text = currentItem.title.replaceFirstChar { it.uppercase() }
        if (currentItem.img.isNotEmpty()){
            val hostImg = ApiUrlRoutes().hostImg + currentItem.img
            Picasso.get().load(hostImg).into(holder.productImg)
        }

        holder.btnDelete.setOnClickListener {
           deleteProduct(currentItem.id)
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }

        holder.itemView.setOnClickListener {
//            val id = currentItem.id
//            val name =  currentItem.name
//            val intent = Intent(context, ProductItem::class.java)
//            intent.putExtra("prodId", id)
//            intent.putExtra("prodName", name)
//            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}