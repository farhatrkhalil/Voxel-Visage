package com.voxelvisage.modelviewer

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val developersPreference: Preference? = findPreference("developers_info")
        developersPreference?.setOnPreferenceClickListener {
            showAlertDialog(
                "Developers",
                "Abed El Fattah Amouneh\nKhalil Farhat\nSammy Medawar\nWally El Sayed"
            )
            true
        }

        val privacyPolicyPreference: Preference? = findPreference("privacy_policy")
        privacyPolicyPreference?.setOnPreferenceClickListener {
            showAlertDialog(
                "Privacy Policy",
                "Voxel Visage Privacy Policy:\n\nYour privacy is important to us. This app does not collect personal information."
            )
            true
        }

        val licensePreference: Preference? = findPreference("license")
        licensePreference?.setOnPreferenceClickListener {
            showAlertDialog(
                "App License",
                "This app is licensed under the MIT License. The MIT License is a type of software license that allows you, the user, a great deal of freedom. It means you can use, modify, and distribute this app according to the terms of the MIT License.\n\nHowever, please note that all 3D models samples included within the app do not belong to the app itself. If you choose to use or load 3D models from external sources, we do not claim ownership of those models. Additionally, if you generate a 3D model by providing images, you own the resulting model. You are free to do so as long as you provide appropriate attribution or credit to the original authors.\n\nThank you for using our app!"
            )
            true
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .show()
    }
}
