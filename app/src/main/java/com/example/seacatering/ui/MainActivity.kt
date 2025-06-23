package com.example.seacatering.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.seacatering.R
import com.example.seacatering.adapter.LandingPagerAdapter
import com.example.seacatering.databinding.ActivityMainBinding
import com.example.seacatering.utils.hideSystemBars
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var binding: ActivityMainBinding
    private var hasNavigatedToAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars()
        viewPager = binding.viewPager2
        dotsIndicator = binding.dotsIndicator
        viewPager.adapter = LandingPagerAdapter(this)
        dotsIndicator.setViewPager2(viewPager)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2 && !hasNavigatedToAuth) {
                    hasNavigatedToAuth = true
                    viewPager.postDelayed({
                        val intent = Intent(this@MainActivity, AuthActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)
                }
            }
        })
    }
}