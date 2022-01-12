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

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.databinding.FragmentAboutBinding;
import io.noties.markwon.Markwon;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

/**
 * Displays an {@link android.widget.TextView TextView} containing credits, copyrights, and license
 * information for the app and the supporting libraries.
 */
public class AboutFragment extends Fragment {

  @SuppressWarnings("ConstantConditions")
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Resources resources = getContext().getResources();
    try (InputStream input = resources.openRawResource(R.raw.about)) {
      String markdown = IOUtils.toString(input, Charset.defaultCharset());
      FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
      Markwon
          .create(getContext())
          .setMarkdown(binding.getRoot(), markdown);
      return binding.getRoot();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
