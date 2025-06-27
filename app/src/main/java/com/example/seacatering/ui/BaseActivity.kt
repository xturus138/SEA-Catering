package com.example.seacatering.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.ActivityBaseBinding
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.ui.dashboard.user.ResultFragment
import com.example.seacatering.utils.BottomVisibilityController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BaseActivity : AppCompatActivity(), BottomVisibilityController {

    companion object{
        var TAG = BaseActivity::class.java.simpleName
    }

    private lateinit var binding : ActivityBaseBinding
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager(applicationContext)

        lifecycleScope.launch {
            val user = dataStoreManager.userData.first()
                if(user == null){
                    startActivity(Intent(this@BaseActivity, AuthActivity::class.java))
                    finish()
                } else {
                    Log.d(TAG, "onCreate: User Sudah Login: ${user.name}")
                }

        }

        loadFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.menu_home -> HomeFragment()
                R.id.menu_meal -> MealFragment()
                R.id.menu_subscription -> ResultFragment()
                else -> null
            }

            fragment?.let {
                loadFragment(it)
                true
            } ?: false
        }



    }

    override fun setBottomNavVisible(visible: Boolean) {
        binding.bottomNav.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}