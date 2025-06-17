package com.example.seacatering.ui.subscription

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentSubscriptionBinding

class SubscriptionFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscriptionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}