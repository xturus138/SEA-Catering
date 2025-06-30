package com.example.seacatering.ui.bottombs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.Button
import androidx.core.net.toUri

class InfoBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_info, container, false)

        val buttonWhatsapp: Button = view.findViewById(R.id.buttonWhatsapp)
        buttonWhatsapp.setOnClickListener {
            val number = "08123456789"
            val message = "Halo! Ada yang ingin saya tanyakan mengenai Sea Catering, Yaitu: "
            val uri = "https://wa.me/$number?text=${Uri.encode(message)}".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        return view
    }
}
