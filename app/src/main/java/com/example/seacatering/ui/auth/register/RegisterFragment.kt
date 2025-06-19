package com.example.seacatering.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.seacatering.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUpButton.setOnClickListener(){
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPass = binding.inputConfirmPassword.text.toString()
            val name = binding.inputName.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()){
                if(password.equals(confirmPass)){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                        if(it.isSuccessful){
                            //for intent later
                        } else {
                            Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Password is not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root

    }


}