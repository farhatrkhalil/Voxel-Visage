package com.voxelvisage.modelviewer

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
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
                "This app is licensed under the MIT License. The MIT License is a type of software license that allows you, the user, a great deal of freedom. It means you can use, modify, and distribute this app according to the terms of the MIT License.\n\nHowever, please note that all 3D models samples included within the app do not belong to the app itself. If you choose to use or load 3D models from external sources, we do not claim ownership of those models.\n\nAdditionally, if you generate a 3D model by providing images, you own the resulting model. You are free to do so as long as you provide appropriate attribution or credit to the original authors.\n\nThank you for using our app!"
            )
            true
        }

        val acknowledgementsPreference: Preference? = findPreference("acknowledgements")
        acknowledgementsPreference?.setOnPreferenceClickListener {
            showAlertDialog(
                "Acknowledgements",
                "Sample models provided by:\n\nAirless Ping Pong Ball.stl\n\nhttps://www.printables.com/model/795518-airless-ping-pong-ball\n\nBroom-bracket.stl\n\nhttps://www.printables.com/model/62410-broom-holder-print-in-place/files\n\nBunny.stl\n\nhttps://www.thingiverse.com/thing:88208\n\nCircleSquareTruss.stl\n\nhttps://free3d.com/3d-model/circle-square-truss-full-diameter-150cm-973959.html\n\nCube.stl\n\nhttps://free3d.com/3d-model/rubiks-cube-5499.html\n\nDragon.stl\n\nhttps://www.thingiverse.com/thing:3652793\n\nFace.stl\n\nhttps://www.turbosquid.com/3d-models/face_01-487745\n\nLucy.stl\n\nhttps://www.ameede.net/angel-artifact-figure-h006053-file-stl-free-download-3d-model-for-cnc-and-3d-printer/\n\nGlasses.obj\n\nhttps://www.turbosquid.com/3d-models/glasses-772170\n\nThin Eyeglasses filter1.obj\n\nhttps://www.turbosquid.com/3d-models/old-glasses-1992270"
            )
            true
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        val spannableMessage = SpannableString(message)
        Linkify.addLinks(spannableMessage, Linkify.WEB_URLS)

        builder.setMessage(spannableMessage)
        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.findViewById<TextView>(android.R.id.message)?.movementMethod = object : LinkMovementMethod() {
            override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
                val action = event.action

                if (action == MotionEvent.ACTION_UP) {
                    var x = event.x.toInt()
                    var y = event.y.toInt()

                    x -= widget.totalPaddingLeft
                    y -= widget.totalPaddingTop

                    x += widget.scrollX
                    y += widget.scrollY

                    val layout = widget.layout
                    val line = layout.getLineForVertical(y)
                    val off = layout.getOffsetForHorizontal(line, x.toFloat())

                    val link = buffer.getSpans(off, off, URLSpan::class.java)

                    if (link.isNotEmpty()) {
                        val url = link[0].url

                        AlertDialog.Builder(requireContext())
                            .setTitle("External Link")
                            .setMessage("You are about to leave the app and open the following link:\n\n$url")
                            .setPositiveButton("OK") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()

                        return true
                    }
                }

                return super.onTouchEvent(widget, buffer, event)
            }
        }
    }
}
