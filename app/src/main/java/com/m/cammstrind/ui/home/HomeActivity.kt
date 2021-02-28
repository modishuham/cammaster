package com.m.cammstrind.ui.home

import android.os.Bundle
import androidx.navigation.findNavController
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseActivity

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()
}