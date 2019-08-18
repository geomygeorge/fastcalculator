package com.geomy.fast.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.MultiSelectListPreference

import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference

import android.support.v7.preference.PreferenceFragmentCompat
import com.geomy.fast.R
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {


    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.settings_pref, rootKey)
        //addPreferencesFromResource(R.xml.settings_pref)

    }

    override fun onResume() {
        super.onResume()

        sharedPreferences = preferenceManager.sharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val preferenceScreen = preferenceScreen
        for (i in 0 until preferenceScreen.preferenceCount) {
            setSummary(getPreferenceScreen().getPreference(i))
        }
    }

    override fun onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = preferenceScreen.findPreference(key)
        pref?.let { setSummary(it) }
    }

    private fun setSummary(pref: Preference) {
        if (pref is EditTextPreference) {
            updateSummary(pref)
        } else if (pref is ListPreference) {
            updateSummary(pref)
        } else if (pref is MultiSelectListPreference) {
            updateSummary(pref)
        } else if (pref is PreferenceCategory){
            // needs to loop through child preferences
            for (i in 0 until pref.preferenceCount) {
                setSummary(pref.getPreference(i))
            }
        }
    }

    private fun updateSummary(pref: MultiSelectListPreference) {
        pref.setSummary(Arrays.toString(pref.values.toTypedArray()))
    }

    private fun updateSummary(pref: ListPreference) {
        pref.summary = pref.value
    }

    private fun updateSummary(preference: EditTextPreference) {

        preference.summary = preference.text
    }
}