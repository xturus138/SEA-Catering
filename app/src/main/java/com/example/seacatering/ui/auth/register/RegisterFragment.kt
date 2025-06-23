package com.example.seacatering.ui.auth.register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.commit
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
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        viewModel = RegisterViewModel()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.seacatering.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signUpButton.setOnClickListener(){
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPass = binding.inputConfirmPassword.text.toString()
            val name = binding.inputName.text.toString()
            viewModel.register(email, password, confirmPass, name)
        }

        binding.googleButtonSignUp.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        observeViewModel()
        return binding.root

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
                    Toast.makeText(requireContext(), "Google ID Token is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel(){
        viewModel.registerResult.observe(viewLifecycleOwner){success ->
            if(success){
                Toast.makeText(requireContext(), "Done Create Account!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.commit {
                    replace(id, LoginFragment())
                }
            }
        }

        viewModel.googleLoginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Google Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), BaseActivity::class.java))
                requireActivity().finish()
            }
        }

        viewModel.errorResult.observe(viewLifecycleOwner){ error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }


}