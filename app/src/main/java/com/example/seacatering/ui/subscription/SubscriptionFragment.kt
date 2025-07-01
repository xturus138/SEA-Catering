package com.example.seacatering.ui.subscription

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentSubscriptionBinding
import com.example.seacatering.model.Meals
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.ui.testimonial.AddTestimonialFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.internal.ViewUtils.hideKeyboard
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class SubscriptionFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionBinding
    private lateinit var viewModel: SubscriptionViewModel
    private var selectedMeal: Meals? = null
    private lateinit var paymentsClient: PaymentsClient
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    private val baseCardPaymentMethod = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedAuthMethods", JSONArray().apply {
                put("PAN_ONLY")
                put("CRYPTOGRAM_3DS")
            })
            put("allowedCardNetworks", JSONArray().apply {
                put("VISA")
                put("MASTERCARD")
            })
            put("billingAddressRequired", true)
            put("billingAddressParameters", JSONObject().apply {
                put("format", "FULL")
            })
        })
        put("tokenizationSpecification", JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", "example")
                put("gatewayMerchantId", "exampleMerchantId")
            })
        })
    }

    private val googlePayBaseConfiguration = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)

        paymentsClient = createPaymentsClient(requireActivity())

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { _, _ ->
            hideKeyboard(requireActivity())
            view.clearFocus()
            false
        }

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(SubscriptionViewModel::class.java)


        selectedMeal = arguments?.getParcelable("meal_data")
        selectedMeal?.let {
            binding.inputPlanSelection.setText(it.name)
        }


        viewModel.userPhoneNumber.observe(viewLifecycleOwner) { phoneNumber ->
            phoneNumber?.let {
                binding.inputNumber.setText(it)
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            userName?.let {
                binding.inputName.setText(it)
            }
        }


        viewModel.totalPrice.observe(viewLifecycleOwner) { price ->
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(price)
            binding.totalPriceTextView.text = formattedPrice.replace("Rp", "Rp ")
        }


        binding.checkboxBreakfast.setOnCheckedChangeListener { _, _ -> updateTotalPrice() }
        binding.checkboxLunch.setOnCheckedChangeListener { _, _ -> updateTotalPrice() }
        binding.checkboxDinner.setOnCheckedChangeListener { _, _ -> updateTotalPrice() }


        binding.chipGroupDays.setOnCheckedStateChangeListener { group, checkedIds ->
            updateTotalPrice()
        }


        updateTotalPrice()



        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }


        binding.paymentButton.setOnClickListener {
            submitSubscriptionData()
        }


        viewModel.submissionResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                val readyToPayRequest =
                    IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString())

                val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
                readyToPayTask.addOnCompleteListener { task ->
                    if (!isAdded || activity == null) return@addOnCompleteListener

                    val paymentDataRequestJson = googlePayBaseConfiguration.apply {
                        put("transactionInfo", JSONObject().apply {
                            put("totalPrice", viewModel.totalPrice.value.toString())
                            put("totalPriceStatus", "FINAL")
                            put("currencyCode", "IDR")
                        })
                        put("merchantInfo", JSONObject().apply {
                            put("merchantName", "SeaCatering")
                        })
                    }

                    val paymentDataRequest = com.google.android.gms.wallet.PaymentDataRequest.fromJson(
                        paymentDataRequestJson.toString()
                    )

                    val safeActivity = requireActivity()
                    AutoResolveHelper.resolveTask(
                        paymentsClient.loadPaymentData(paymentDataRequest),
                        safeActivity,
                        LOAD_PAYMENT_DATA_REQUEST_CODE
                    )
                }

            } else {
                Toast.makeText(context, "Subscription failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 991) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    val paymentInfo = paymentData?.toJson()
                    Toast.makeText(requireContext(), "Payment Success!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AddTestimonialFragment())
                        .commit()
                }

                Activity.RESULT_CANCELED -> {
                    Toast.makeText(requireContext(), "Payment Canceled.", Toast.LENGTH_SHORT).show()
                }

                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Toast.makeText(requireContext(), "Payment Error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }


    private fun updateTotalPrice() {
        selectedMeal?.price?.let { planPrice ->
            val numberOfMealTypes = getSelectedMealTypesCount()
            val numberOfDeliveryDays = getSelectedDeliveryDaysCount()
            viewModel.calculateTotalPrice(planPrice, numberOfMealTypes, numberOfDeliveryDays)
        }
    }

    private fun getSelectedMealTypesCount(): Int {
        var count = 0
        if (binding.checkboxBreakfast.isChecked) count++
        if (binding.checkboxLunch.isChecked) count++
        if (binding.checkboxDinner.isChecked) count++
        return count
    }

    private fun getSelectedDeliveryDaysCount(): Int {
        return binding.chipGroupDays.checkedChipIds.size
    }

    private fun submitSubscriptionData() {
        val name = binding.inputName.text.toString().trim()
        val phoneNumber = binding.inputNumber.text.toString().trim()
        val planSelection = binding.inputPlanSelection.text.toString().trim()
        val allergies = binding.inputAllergies.text.toString().trim()


        if (name.isEmpty() || phoneNumber.isEmpty() || planSelection.isEmpty() || allergies.isEmpty() || !isMealTypeSelected() || !isDeliveryDaySelected()) {
            Toast.makeText(context, "Please fill in all required fields and select meal type/delivery days.", Toast.LENGTH_SHORT).show()
            return
        }


        val mealTypes = mutableListOf<String>()
        if (binding.checkboxBreakfast.isChecked) mealTypes.add("Breakfast")
        if (binding.checkboxLunch.isChecked) mealTypes.add("Lunch")
        if (binding.checkboxDinner.isChecked) mealTypes.add("Dinner")
        val mealTypeString = mealTypes.joinToString(", ")


        val deliveryDays = mutableListOf<String>()
        binding.chipGroupDays.checkedChipIds.forEach { id ->
            val chip = view?.findViewById<Chip>(id)
            chip?.text?.toString()?.let { day ->
                when (day) {
                    "Monday" -> deliveryDays.add("1")
                    "Tuesday" -> deliveryDays.add("2")
                    "Wednesday" -> deliveryDays.add("3")
                    "Thursday" -> deliveryDays.add("4")
                    "Friday" -> deliveryDays.add("5")
                    "Saturday" -> deliveryDays.add("6")
                    "Sunday" -> deliveryDays.add("7")
                }
            }
        }
        val deliveryDaysString = deliveryDays.joinToString(",")

        val planId = selectedMeal?.id ?: "UnknownID"
        val planName = selectedMeal?.name ?: "Unknown Plan"


        viewModel.submitSubscription(
            allergies,
            mealTypeString,
            deliveryDaysString,
            planId,
            planName,
            phoneNumber
        )

    }

    private fun isMealTypeSelected(): Boolean {
        return binding.checkboxBreakfast.isChecked || binding.checkboxLunch.isChecked || binding.checkboxDinner.isChecked
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isDeliveryDaySelected(): Boolean {
        return binding.chipGroupDays.checkedChipIds.isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}