package com.example.seacatering.ui.walkthrough

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentPageThirdBinding


class PageThirdFragment : Fragment() {

    private lateinit var binding: FragmentPageThirdBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageThirdBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


}