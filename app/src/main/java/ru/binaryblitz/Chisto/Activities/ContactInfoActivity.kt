package ru.binaryblitz.Chisto.Activities

import android.os.Bundle
import android.view.View

import ru.binaryblitz.Chisto.Base.BaseActivity

import com.crashlytics.android.Crashlytics

import io.fabric.sdk.android.Fabric
import ru.binaryblitz.Chisto.R

class ContactInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile_contact_info)

        findViewById(R.id.left_btn).setOnClickListener { finish() }
    }

}
