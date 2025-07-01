package com.example.seacatering.ui.auth.register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentRegisterBinding
import com.example.seacatering.ui.BaseActivity
import com.example.seacatering.ui.auth.login.LoginFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)

        val dataStoreManager = DataStoreManager(requireContext())
        val factory = RegisterViewModelFactory(dataStoreManager)
        viewModel = ViewModelProvider(this, factory).get(RegisterViewModel::class.java)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.seacatering.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signUpButton.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString()
            val confirmPass = binding.inputConfirmPassword.text.toString()
            val name = binding.inputName.text.toString().trim()

            if (!validateInputs(email, password, confirmPass, name)) return@setOnClickListener

            binding.signUpButton.isEnabled = false

            viewModel.register(email, password, confirmPass, name, address = "Not Provided", noHp = "Not Provided")
        }

        binding.googleButtonSignUp.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.backButton.setOnClickListener {
            navigateToFragment(LoginFragment())
        }

        observeViewModel()
        return binding.root
    }

    private fun validateInputs(email: String, password: String, confirmPass: String, name: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.inputEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

        if (confirmPass.isEmpty()) {
            binding.inputConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPass) {
            binding.inputConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.inputConfirmPassword.error = null
        }

        if (name.isEmpty()) {
            binding.inputName.error = "Name cannot be empty"
            isValid = false
        } else {
            binding.inputName.error = null
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
                    Toast.makeText(requireContext(), "We couldn’t verify your Google account. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign-in failed. Please check your connection and try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(viewLifecycleOwner) { success ->
            binding.signUpButton.isEnabled = true

            if (success) {
                Toast.makeText(requireContext(), "Your account has been created successfully!", Toast.LENGTH_SHORT).show()
                navigateToFragment(LoginFragment())
            }
        }

        viewModel.googleLoginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Google Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), BaseActivity::class.java))
                requireActivity().finish()
            }
        }

        viewModel.errorResult.observe(viewLifecycleOwner) { error ->
            binding.signUpButton.isEnabled = true
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(id, fragment)
            addToBackStack(null)
        }
    }
}
