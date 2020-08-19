package com.example.platys.success

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.platys.R

class SuccessFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.success_fragment, container, false)

        val imageView = root.findViewById<ImageView>(R.id.successCircle)

        Handler().postDelayed({
            imageView.performClick()
        }, 500)

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Handler().postDelayed({
            activity?.onBackPressed()
        }, 3000)
    }
}