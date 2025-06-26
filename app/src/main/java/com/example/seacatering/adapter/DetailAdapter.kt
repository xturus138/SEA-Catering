package com.example.seacatering.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seacatering.databinding.ItemDetailMealBinding

class DetailAdapter(private val details: List<String>) :
    RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(private val binding: ItemDetailMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvDetail = binding.tvDetail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemDetailMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.tvDetail.text = "• ${details[position]}"
    }

    override fun getItemCount(): Int = details.size
}

