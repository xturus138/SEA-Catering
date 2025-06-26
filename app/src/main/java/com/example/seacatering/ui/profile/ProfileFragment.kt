package com.example.seacatering.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentProfileBinding
import com.example.seacatering.ui.AuthActivity
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var viewModel: ProfileViewModel
    private lateinit var bottomNavHelper: BottomVisibilityController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        dataStoreManager = DataStoreManager(requireContext())
        val factory = ProfileViewModelFactory(dataStoreManager)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        lifecycleScope.launch {
            dataStoreManager.userData.collectLatest { userData ->
                if (userData == null) {
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    binding.nameProfile.text = userData.name
                    binding.emailProfile.text = userData.email
                    binding.textInputName.setText(userData.name)
                    binding.textInputEmail.setText(userData.email)
                    binding.textInputHp.setText(userData.noHp)
                    binding.textInputAddress.setText(userData.address)
                }
            }
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.seacatering.R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.updateButton.setOnClickListener {
            updateProfile()
        }

        binding.logOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BottomVisibilityController)?.setBottomNavVisible(false)
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Update berhasil", Toast.LENGTH_SHORT).show()
                refreshProfile()
            } else {
                Toast.makeText(requireContext(), "Update gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? BottomVisibilityController)?.setBottomNavVisible(true)
    }


    private fun updateProfile() {
        val name = binding.textInputName.text.toString()
        val email = binding.textInputEmail.text.toString()
        val address = binding.textInputAddress.text.toString()
        val noHp = binding.textInputHp.text.toString()
        viewModel.updateUser(name, email, address, noHp)
    }

    private fun refreshProfile() {
        lifecycleScope.launch {
            dataStoreManager.userData.collectLatest { userData ->
                binding.textInputName.setText(userData?.name)
                binding.textInputEmail.setText(userData?.email)
                binding.textInputHp.setText(userData?.noHp)
                binding.textInputAddress.setText(userData?.address)
            }
        }
    }
}
