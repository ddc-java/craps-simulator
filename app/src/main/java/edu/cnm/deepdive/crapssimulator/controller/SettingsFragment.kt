/*
 *  Copyright 2022 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.crapssimulator.controller

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import edu.cnm.deepdive.crapssimulator.R
import kotlin.math.pow

/**
 * Handles presentation of, and user interaction with, preference settings for simulation execution.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupSeekBar(R.string.batch_size_pref_key, R.string.batch_size_pref_summary)
    }

    private fun setupSeekBar(keyResId: Int, summaryResId: Int) {
        findPreference<SeekBarPreference>(getString(keyResId))!!.apply {
            Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
                updateSummary(preference, newValue as Int, summaryResId)
            }
                .also { it.onPreferenceChange(this, value) }
                .also { onPreferenceChangeListener = it }
        }
    }

    private fun updateSummary(preference: Preference, newValue: Int, resId: Int): Boolean {
        val quantity = 10.0
            .pow(newValue.toDouble())
            .toInt()
        val quantityString = resources.getQuantityString(R.plurals.round_quantity, quantity)
        preference.summary = getString(resId, quantity, quantityString)
        return true
    }
}