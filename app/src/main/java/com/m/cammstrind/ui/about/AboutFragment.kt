package com.m.cammstrind.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.BuildConfig
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("About")
        AppUtils.getNetworkState(requireContext())
        iv_about_back.setOnClickListener {
            findNavController().popBackStack()
        }

        tv_privacy_policy.setOnClickListener {
            if (AppUtils.isInternetConnected()) {
                val bundle = bundleOf("pageName" to "policy")
                findNavController().navigate(
                    R.id.action_aboutFragment_to_privacyPolicyFragment,
                    bundle
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tv_terms.setOnClickListener {
            if (AppUtils.isInternetConnected()) {
                val bundle = bundleOf("pageName" to "terms")
                findNavController().navigate(
                    R.id.action_aboutFragment_to_privacyPolicyFragment,
                    bundle
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tv_app_version.text = resources.getString(R.string.version) + " " + BuildConfig.VERSION_NAME
    }
}