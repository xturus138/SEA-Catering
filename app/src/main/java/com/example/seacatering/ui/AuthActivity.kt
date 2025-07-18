package com.example.seacatering.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.ActivityAuthBinding
import com.example.seacatering.ui.auth.login.LoginFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthBinding
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        dataStoreManager = DataStoreManager(this)

        lifecycleScope.launch {
            val user = dataStoreManager.userData.first()
            if (user != null) {
                startActivity(Intent(this@AuthActivity, BaseActivity::class.java))
                finish()
            } else {
                loadFragment(LoginFragment())
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboard(this)
        currentFocus?.clearFocus()
        return super.onTouchEvent(event)
    }



    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_main, fragment)
            .commit()
    }
}