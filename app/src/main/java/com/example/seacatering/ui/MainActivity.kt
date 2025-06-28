package com.example.seacatering.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.seacatering.R
import com.example.seacatering.adapter.LandingPagerAdapter
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.ActivityMainBinding
import com.example.seacatering.ui.auth.login.LoginFragment
import com.example.seacatering.utils.hideSystemBars
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var binding: ActivityMainBinding
    private var hasNavigatedToAuth = false
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        dataStoreManager = DataStoreManager(this)

        lifecycleScope.launch {
            val user = dataStoreManager.userData.first()
            if (user != null) {
                startActivity(Intent(this@MainActivity, BaseActivity::class.java))
                finish()
            } else {
                setContentView(binding.root)
            }
        }

        hideSystemBars()
        viewPager = binding.viewPager2
        dotsIndicator = binding.dotsIndicator
        viewPager.adapter = LandingPagerAdapter(this)
        dotsIndicator.setViewPager2(viewPager)


    }
}