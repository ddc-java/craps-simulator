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
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import edu.cnm.deepdive.crapssimulator.R
import edu.cnm.deepdive.crapssimulator.adapter.SnapshotRollsAdapter
import edu.cnm.deepdive.crapssimulator.databinding.FragmentCrapsBinding
import edu.cnm.deepdive.crapssimulator.model.Snapshot
import edu.cnm.deepdive.crapssimulator.viewmodel.CrapsViewModel

/**
 * Handles presentation of, and user interaction with, a display of the current tally of wins and
 * losses; the rolls and outcome of most recent round of play; and controls to start, stop, and
 * reset the simulation.
 */
class CrapsFragment : Fragment() {

    private val actions: MutableMap<Int, Runnable> = HashMap()
    private var binding: FragmentCrapsBinding? = null
    private lateinit var viewModel: CrapsViewModel
    private var running = false
    private lateinit var summaryFormat: String
    private lateinit var errorMessageFormat: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCrapsBinding.inflate(inflater, container, false)
        summaryFormat = getString(R.string.summary_format)
        errorMessageFormat = getString(R.string.error_message_format)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CrapsViewModel::class.java).apply {
            lifecycle.addObserver(this)
            val owner = viewLifecycleOwner
            snapshot.observe(owner) { updateDisplay(it) }
            running.observe(owner) { setRunning(it) }
            throwable.observe(owner) { showError(it) }
        }
        buildMenuActionsMap()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        with(menu) {
            super.onPrepareOptionsMenu(this)
            findItem(R.id.action_play_once).isVisible = !running
            findItem(R.id.action_play_fast).isVisible = !running
            findItem(R.id.action_pause).isVisible = running
            findItem(R.id.action_reset).isVisible = !running
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = booleanArrayOf(true)
        actions
                .getOrDefault(item.itemId, Runnable { handled[0] = super.onOptionsItemSelected(item) })
                .run()
        return handled[0]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun updateDisplay(snapshot: Snapshot) {
        val wins = snapshot.wins
        val rounds = snapshot.rounds
        val winningPercentage = if (rounds > 0) 100.0 * wins / rounds else 0.toDouble()
        val resources = resources
        val winQuantity = resources.getQuantityString(R.plurals.win_quantity, wins.toInt())
        val roundQuantity = resources.getQuantityString(R.plurals.round_quantity, rounds.toInt())
        val adapter = SnapshotRollsAdapter(context, snapshot)
        binding?.let {
            it.summary.text = String.format(summaryFormat, wins, winQuantity, rounds, roundQuantity, winningPercentage)
            it.rolls.adapter = adapter
        }
    }

    private fun showError(throwable: Throwable) {
        binding?.let {
            Snackbar
                    .make(it.root, String.format(errorMessageFormat, throwable.message),
                            Snackbar.LENGTH_LONG)
                    .show()
        }
    }

    private fun buildMenuActionsMap() {
        with(actions) {
            clear()
            put(R.id.action_play_once) { viewModel.runOnce() }
            put(R.id.action_play_fast) { viewModel.runFast() }
            put(R.id.action_pause) { viewModel.stop() }
            put(R.id.action_reset) { viewModel.reset() }
            put(R.id.action_settings) { openSettings() }
            put(R.id.action_about) { openAbout() }
        }
    }

    private fun setRunning(running: Boolean) {
        this.running = running
        activity!!.invalidateOptionsMenu()
    }

    private fun openSettings() {
        binding?.let {
            Navigation
                    .findNavController(it.root)
                    .navigate(CrapsFragmentDirections.openSettings())
        }
    }

    private fun openAbout() {
        Navigation
                .findNavController(binding!!.root)
                .navigate(CrapsFragmentDirections.openAbout())
    }
}