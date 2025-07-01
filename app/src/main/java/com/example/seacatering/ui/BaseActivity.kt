package com.example.seacatering.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.ActivityBaseBinding
import com.example.seacatering.model.Role
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.ui.dashboard.admin.AdminDashboardFragment
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
    private lateinit var connectivityReceiver: android.content.BroadcastReceiver
    private var currentUserRole: Role = Role.USER
    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        dataStoreManager = DataStoreManager(applicationContext)

        lifecycleScope.launch {
            val user = dataStoreManager.userData.first()
            if (user == null) {
                startActivity(Intent(this@BaseActivity, AuthActivity::class.java))
                finish()
            } else {
                currentUserRole = user.role


                if (savedInstanceState == null) {
                    if (currentUserRole == Role.ADMIN) {
                        loadFragment(AdminDashboardFragment())
                        binding.bottomNav.visibility = View.GONE
                    } else {
                        loadFragment(HomeFragment())
                        binding.bottomNav.selectedItemId = R.id.menu_home
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            val fragment: Fragment? = when (menuItem.itemId) {
                R.id.menu_home -> HomeFragment()
                R.id.menu_meal -> MealFragment()
                R.id.menu_subscription -> {
                    if (currentUserRole == Role.ADMIN) {
                        AdminDashboardFragment()
                    } else {
                        ResultFragment()
                    }
                }
                else -> null
            }

            fragment?.let {
                loadFragment(it)
                if (it is AdminDashboardFragment) {
                    binding.bottomNav.visibility = View.GONE
                } else {
                    binding.bottomNav.visibility = View.VISIBLE
                }
                true
            } ?: false
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }


    override fun setBottomNavVisible(visible: Boolean) {
        binding.bottomNav.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        registerNetworkReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(connectivityReceiver)
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun registerNetworkReceiver() {
        val filter = android.content.IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
        connectivityReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (!isNetworkAvailable(context)) {
                    if (!isDialogShown) showNoInternetDialog()
                } else {
                    isDialogShown = false
                }
            }
        }
        registerReceiver(connectivityReceiver, filter)
    }

    private fun showNoInternetDialog() {
        isDialogShown = true
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please enable internet connection to continue")
            .setCancelable(false)
            .setPositiveButton("Try again!") { d, _ ->
                d.dismiss()
                if (!isNetworkAvailable(this)) {
                    showNoInternetDialog()
                } else {
                    isDialogShown = false
                }
            }
            .create()

        dialog.show()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

}