package com.example.seacatering.ui.auth.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.seacatering.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signInButton.setOnClickListener(){
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful){

                    } else {
                        Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                 Toast.makeText(requireContext(), "Fields are not empty!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

}