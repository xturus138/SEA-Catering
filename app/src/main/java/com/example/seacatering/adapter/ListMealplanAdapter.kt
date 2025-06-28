package com.example.seacatering.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seacatering.R
import com.example.seacatering.model.Meals


class ListMealplanAdapter(private var meals: List<Meals>, private val listener: onMealClickListener) :
    RecyclerView.Adapter<ListMealplanAdapter.MealViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDescriptionTitle: TextView = itemView.findViewById(R.id.tvDescriptionTitle)
        val rvDetails: RecyclerView = itemView.findViewById(R.id.rvDetails)
        val ivMealImage: ImageView = itemView.findViewById(R.id.ivMealImage)
        val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
    }

    interface onMealClickListener {
        fun onMealClick(meal: Meals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mealplan_row, parent, false)
        return MealViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        holder.tvName.text = meal.name
        holder.tvPrice.text = meal.price
        holder.tvDescriptionTitle.text = meal.descriptionTitle

        holder.rvDetails.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvDetails.adapter = DetailAdapter(meal.details)

        Glide.with(holder.itemView.context)
            .load(meal.imageResId)
            .into(holder.ivMealImage)

        holder.ivSelected.visibility =
            if (position == selectedPosition) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.bindingAdapterPosition
            if (clickedPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previous = selectedPosition
            selectedPosition = clickedPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)

            listener.onMealClick(meals[clickedPosition])
        }

        holder.rvDetails.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                holder.itemView.performClick()
            }
            true
        }
    }

    override fun getItemCount(): Int = meals.size

    fun updateData(newMeals: List<Meals>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}