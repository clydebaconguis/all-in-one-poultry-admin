package com.example.adminpoultry.config

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.text.format.Formatter.formatIpAddress
import java.util.*

class ApiUrlRoutes(val id:Int=0) {

    private var host = "https://larapoultry.herokuapp.com"
    private val api = "api/"
    private var hostApi = "$host/$api"

    var hostImg = "https://drive.google.com/uc?export=view&id="

    //Products
    val getProducts = hostApi + "product_categories"
    val getProduct = hostApi +  "product_categories/$id"
    val deleteProduct =  hostApi + "product_categories/$id"

    //Types
    val getTypes = hostApi + "types"
    val deleteTypes = hostApi + "types/$id"
    val postPrice = "pricings"

    val transactions = hostApi + "transactions"
//
//    //Carts
//    val addCart = host + "carts"
//    val getCart = host + "carts/$id"
//
//    //login
//    val login = host + "login"
//    val register = host + "register"
}