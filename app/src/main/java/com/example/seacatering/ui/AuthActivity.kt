package com.example.seacatering.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.seacatering.R
import com.example.seacatering.databinding.ActivityAuthBinding
import com.example.seacatering.ui.auth.login.LoginFragment

class AuthActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadFragment(LoginFragment())
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_main, fragment)
            .commit()
    }
}