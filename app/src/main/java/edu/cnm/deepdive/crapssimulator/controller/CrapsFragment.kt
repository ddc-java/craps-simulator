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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.adapter.SnapshotRollsAdapter;
import edu.cnm.deepdive.crapssimulator.databinding.FragmentCrapsBinding;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.viewmodel.CrapsViewModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles presentation of, and user interaction with, a display of the current tally of wins and
 * losses; the rolls and outcome of most recent round of play; and controls to start, stop, and
 * reset the simulation.
 */
public class CrapsFragment extends Fragment {

  private final Map<Integer, Runnable> actions = new HashMap<>();

  private FragmentCrapsBinding binding;
  private CrapsViewModel viewModel;
  private boolean running;
  private String summaryFormat;
  private String errorMessageFormat;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentCrapsBinding.inflate(inflater, container, false);
    summaryFormat = getString(R.string.summary_format);
    errorMessageFormat = getString(R.string.error_message_format);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(CrapsViewModel.class);
    getLifecycle().addObserver(viewModel);
    LifecycleOwner owner = getViewLifecycleOwner();
    viewModel.getSnapshot().observe(owner, this::updateDisplay);
    viewModel.getRunning().observe(owner, this::setRunning);
    viewModel.getThrowable().observe(owner, this::showError);
    buildMenuActionsMap();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_main, menu);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.action_play_once).setVisible(!running);
    menu.findItem(R.id.action_play_fast).setVisible(!running);
    menu.findItem(R.id.action_pause).setVisible(running);
    menu.findItem(R.id.action_reset).setVisible(!running);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    boolean[] handled = {true};
    //noinspection ConstantConditions
    actions
        .getOrDefault(item.getItemId(), () -> handled[0] = super.onOptionsItemSelected(item))
        .run();
    return handled[0];
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void updateDisplay(Snapshot snapshot) {
    long wins = snapshot.getWins();
    long rounds = snapshot.getRounds();
    double winningPercentage = (rounds > 0) ? (100.0 * wins / rounds) : 0;
    Resources resources = getResources();
    String winQuantity = resources.getQuantityString(R.plurals.win_quantity, (int) wins);
    String roundQuantity = resources.getQuantityString(R.plurals.round_quantity, (int) rounds);
    binding.summary.setText(
        String.format(summaryFormat, wins, winQuantity, rounds, roundQuantity, winningPercentage));
    SnapshotRollsAdapter adapter = new SnapshotRollsAdapter(getContext(), snapshot);
    binding.rolls.setAdapter(adapter);
  }

  private void showError(Throwable throwable) {
    Snackbar
        .make(binding.getRoot(), String.format(errorMessageFormat, throwable.getMessage()),
            Snackbar.LENGTH_LONG)
        .show();
  }

  private void buildMenuActionsMap() {
    actions.clear();
    actions.put(R.id.action_play_once, viewModel::runOnce);
    actions.put(R.id.action_play_fast, viewModel::runFast);
    actions.put(R.id.action_pause, viewModel::stop);
    actions.put(R.id.action_reset, viewModel::reset);
    actions.put(R.id.action_settings, this::openSettings);
    actions.put(R.id.action_about, this::openAbout);
  }

  private void setRunning(boolean running) {
    this.running = running;
    //noinspection ConstantConditions
    getActivity().invalidateOptionsMenu();
  }

  private void openSettings() {
    Navigation
        .findNavController(binding.getRoot())
        .navigate(CrapsFragmentDirections.openSettings());
  }

  private void openAbout() {
    Navigation
        .findNavController(binding.getRoot())
        .navigate(CrapsFragmentDirections.openAbout());
  }

}