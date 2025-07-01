package com.example.seacatering.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.widget.addTextChangedListener
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.seacatering.ui.home.HomeViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var viewModel: ProfileViewModel
    private var isAddressManuallyChanged = false
    private lateinit var googleMap: GoogleMap
    private lateinit var homeViewModel: HomeViewModel


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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        dataStoreManager = DataStoreManager(requireContext())
        val factory = ProfileViewModelFactory(dataStoreManager, requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        homeViewModel.fetchUserLocation()
        homeViewModel.locationLatLng.observe(viewLifecycleOwner) { latLng ->
            if (latLng.first != 0.0 && latLng.second != 0.0) {
                viewModel.setLocationLatLng(latLng)
                viewModel.getCityAndProvince(latLng.first, latLng.second)
            }
        }
        binding.textInputAddress.addTextChangedListener {
            if (binding.textInputAddress.isFocused) {
                val address = it.toString()
                viewModel.geocodeAddress(address) { latLng ->
                    latLng?.let {
                        viewModel.setLocationLatLng(it)
                    }
                }
            }
        }


        lifecycleScope.launch {
            dataStoreManager.userData.collectLatest { userData ->
                if (userData == null) {
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    isAddressManuallyChanged = false
                    binding.nameProfile.text = userData.name
                    binding.emailProfile.text = userData.email
                    binding.textInputName.setText(userData.name)
                    binding.textInputEmail.setText(userData.email)
                    binding.textInputHp.setText(userData.noHp)
                    binding.textShowAddress.setText(userData.address)
                    binding.textInputAddress.setText(userData.address)

                    if (userData.latitude != 0.0 && userData.longitude != 0.0) {
                        viewModel.setLocationLatLng(Pair(userData.latitude, userData.longitude))
                    }

                    if (userData.profileImageUrl.isNotEmpty()) {
                        context?.let {
                            Glide.with(it)
                                .load(userData.profileImageUrl.toUri())
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


        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Update success", Toast.LENGTH_SHORT).show()
                isAddressManuallyChanged = false
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
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

        binding.cardView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        viewModel.locationLatLng.observe(viewLifecycleOwner) { latLng ->
            val defaultLatLng = LatLng(-6.200000, 106.816666)
            val lokasi = if (latLng.first == 0.0 && latLng.second == 0.0) defaultLatLng else LatLng(latLng.first, latLng.second)

            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions()
                    .position(lokasi)
                    .title(viewModel.locationTitle.value ?: "Lokasi Default")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 16f))
        }

        googleMap.setOnMapClickListener { latLng ->
            viewModel.setLocationLatLng(Pair(latLng.latitude, latLng.longitude))
            viewModel.getCityAndProvince(latLng.latitude, latLng.longitude)
        }

        viewModel.locationTitle.observe(viewLifecycleOwner) {
            binding.textInputAddress.setText(it)
        }
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { _, _ ->
            hideKeyboard(requireActivity())
            view.clearFocus()
            false
        }
        (activity as? BottomVisibilityController)?.setBottomNavVisible(false)

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
        val latitude = viewModel.locationLatLng.value?.first ?: 0.0
        val longitude = viewModel.locationLatLng.value?.second ?: 0.0
        val password = binding.textInputPassword.text.toString()
        val confirmPassword = binding.textInputConfirmPassword.text.toString()

        if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return
            }

            val user = FirebaseAuth.getInstance().currentUser
            user?.updatePassword(password)?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                binding.textInputPassword.text?.clear()
                binding.textInputConfirmPassword.text?.clear()
            }?.addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memperbarui password: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateUser(name, email, address, noHp, latitude, longitude)
    }


    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}