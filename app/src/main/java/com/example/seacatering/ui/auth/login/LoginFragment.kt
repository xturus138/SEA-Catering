package com.example.seacatering.ui.auth.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentLoginBinding
import com.example.seacatering.ui.AuthActivity
import com.example.seacatering.ui.BaseActivity
import com.example.seacatering.ui.auth.register.RegisterFragment
import com.example.seacatering.ui.home.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val factory = LoginViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.seacatering.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signInButton.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (!validateInputs(email, password)) return@setOnClickListener

            binding.progressBarLogin.visibility = View.VISIBLE
            viewModel.login(email, password)
        }

        binding.googleButtonSignIn.setOnClickListener {
            binding.progressBarLogin.visibility = View.VISIBLE
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.tvRegister.setOnClickListener {
            navigateToFragment(RegisterFragment())
        }

        checkAndRequestLocationPermission()
        observeViewModel()

        return binding.root
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.inputEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.inputEmail.error = null
        }

        if (password.isEmpty()) {
            binding.inputPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.inputPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.inputPassword.error = null
        }

        return isValid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    binding.progressBarLogin.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to retrieve your Google account data. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                binding.progressBarLogin.visibility = View.GONE
                Toast.makeText(requireContext(), "Google login failed. Please check your connection or try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.progressBarLogin.visibility = View.GONE
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), BaseActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }

        viewModel.googleLoginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.progressBarLogin.visibility = View.GONE
                Toast.makeText(requireContext(), "Google Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), BaseActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.progressBarLogin.visibility = View.GONE
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestLocationPermission() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            homeViewModel.fetchUserLocation()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(id, fragment)
            addToBackStack(null)
        }
    }
}
