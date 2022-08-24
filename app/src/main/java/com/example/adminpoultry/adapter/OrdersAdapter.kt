package com.example.adminpoultry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminpoultry.R
import com.example.adminpoultry.model.OrdersModel
import com.squareup.picasso.Picasso

class OrdersAdapter(private val context: Context, private val itemList:ArrayList<OrdersModel>): RecyclerView.Adapter<OrdersAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val code : TextView = itemView.findViewById(R.id.tvTransac)
        val status:  TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.orders_item_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]
        val code = "Transacion No." + " " + currentItem.transaction
        holder.code.text = code
        holder.status.text=currentItem.status

//        holder.itemView.setOnClickListener {
//            val id = currentItem.id
//            val name =  currentItem.name
//            val intent = Intent(context, ProductItem::class.java)
//            intent.putExtra("prodId", id)
//            intent.putExtra("prodName", name)
//            intent.putExtra("img", img)
//            context.startActivity(intent)
//
//        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}