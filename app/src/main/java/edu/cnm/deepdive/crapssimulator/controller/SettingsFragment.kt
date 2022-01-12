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
package edu.cnm.deepdive.crapssimulator.controller;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import edu.cnm.deepdive.crapssimulator.R;

/**
 * Handles presentation of, and user interaction with, preference settings for simulation execution.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
    setupSeekBar(R.string.batch_size_pref_key, R.string.batch_size_pref_summary);
  }

  private void setupSeekBar(int keyResId, int summaryResId) {
    SeekBarPreference pref = findPreference(getString(keyResId));
    //noinspection ConstantConditions
    pref.setOnPreferenceChangeListener((preference, newValue) ->
        updateSummary(preference, (Integer) newValue, summaryResId));
    updateSummary(pref, pref.getValue(), summaryResId);
  }

  private boolean updateSummary(Preference preference, Integer newValue, int resId) {
    int quantity = (int) Math.round(Math.pow(10, newValue));
    String quantityString = getResources().getQuantityString(R.plurals.round_quantity, quantity);
    preference.setSummary(getString(resId, quantity, quantityString));
    return true;
  }

}
