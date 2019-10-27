package com.zhenhui.demo.apps.tracer.permission;

import android.os.Bundle;

import com.zhenhui.demo.apps.tracer.R;

import androidx.preference.PreferenceFragmentCompat;

public class PreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
    }
}

