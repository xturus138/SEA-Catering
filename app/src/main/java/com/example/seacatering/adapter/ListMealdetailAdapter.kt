package com.example.seacatering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seacatering.R
import com.example.seacatering.model.MenuItem

class ListMealdetailAdapter(private var menuList: List<MenuItem>) :
    RecyclerView.Adapter<ListMealdetailAdapter.MealDetailViewHolder>() {

    class MealDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoodImage: ImageView = itemView.findViewById(R.id.ivFoodImage)
        val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        val tvFreshTag: TextView = itemView.findViewById(R.id.tvFreshTag)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val tvNutritionInfo: TextView = itemView.findViewById(R.id.tvNutritionInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mealdetail_row, parent, false)
        return MealDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealDetailViewHolder, position: Int) {
        val menuItem = menuList[position]

        holder.tvFoodName.text = menuItem.foodName
        holder.tvFreshTag.text = menuItem.freshTag
        holder.tvRestaurantName.text = menuItem.restaurant
        holder.tvNutritionInfo.text = menuItem.nutrition

        Glide.with(holder.itemView.context)
            .load(menuItem.foodImage)
            .into(holder.ivFoodImage)
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newMenuList: List<MenuItem>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }
}