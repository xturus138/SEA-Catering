package com.example.seacatering.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentHomeBinding
import com.example.seacatering.ui.profile.ProfileFragment

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)


        binding.cardView.setOnClickListener(){
            val intent = Intent(requireContext(), ProfileFragment::class.java)
            startActivity(intent)
        }

        return binding.root
    }


}