package com.example.seacatering.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentProfileBinding
import com.example.seacatering.ui.AuthActivity
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var viewModel: ProfileViewModel



    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                viewModel.setTempProfileImageUri(uri)
                context?.let {
                    Glide.with(it)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_profile_image)
                        .into(binding.imgProfile)
                }
            } else {
                Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show()
            }
        }

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

                    if (userData.profileImageUrl.isNotEmpty()) {
                        context?.let {
                            Glide.with(it)
                                .load(Uri.parse(userData.profileImageUrl))
                                .circleCrop()
                                .placeholder(R.drawable.ic_default_profile_image)
                                .error(R.drawable.ic_default_profile_image)
                                .into(binding.imgProfile)
                        }
                    } else {
                        binding.imgProfile.setImageResource(R.drawable.ic_default_profile_image)
                    }
                }
            }
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
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

        binding.btnContactWhatsapp.setOnClickListener {
            val phoneNumber = "08123456789"
            val message = "I want to ask.."

            try {
                val whatsappIntent = Intent(Intent.ACTION_VIEW)
                whatsappIntent.data = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}".toUri()
                startActivity(whatsappIntent)
            } catch (e: Exception) {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = "tel:$phoneNumber".toUri()
                try {
                    startActivity(dialIntent)
                } catch (dialException: Exception) {
                    Toast.makeText(requireContext(), "No application found to handle this request.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.cardView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BottomVisibilityController)?.setBottomNavVisible(false)
        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Update berhasil", Toast.LENGTH_SHORT).show()
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

}