package com.m.cammstrind.ui.privacyPolicy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("PrivacyPolicy")
        val pageName = arguments?.getString("pageName")
        if (pageName != null) {
            if (pageName == "terms") {
                wv.loadUrl("https://cammasterindia.000webhostapp.com/terms.html")
            } else {
                wv.loadUrl("https://cammasterindia.000webhostapp.com/")
            }
        } else {
            wv.loadUrl("https://cammasterindia.000webhostapp.com/")
        }
    }
}