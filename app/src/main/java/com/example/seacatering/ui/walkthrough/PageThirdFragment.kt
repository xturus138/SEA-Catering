package com.example.seacatering.ui.walkthrough

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentPageThirdBinding
import com.example.seacatering.ui.AuthActivity


class PageThirdFragment : Fragment() {

    private lateinit var binding: FragmentPageThirdBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageThirdBinding.inflate(layoutInflater, container, false)

        binding.txtNext.setOnClickListener(){
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        return binding.root
    }


}