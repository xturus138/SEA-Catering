package com.example.seacatering.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.seacatering.ui.walkthrough.PageOneFragment
import com.example.seacatering.ui.walkthrough.PageThirdFragment
import com.example.seacatering.ui.walkthrough.PageTwoFragment

class LandingPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PageOneFragment()
            1 -> PageTwoFragment()
            2 -> PageThirdFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
