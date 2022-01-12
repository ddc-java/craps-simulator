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
package edu.cnm.deepdive.crapssimulator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import edu.cnm.deepdive.crapssimulator.service.CrapsRepository
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.disposables.CompositeDisposable
import android.content.SharedPreferences
import android.util.Log
import edu.cnm.deepdive.crapssimulator.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import edu.cnm.deepdive.crapssimulator.model.Snapshot

/**
 * Exposes simulation-control methods and manages lifecycle-aware subset of system state (model
 * content), for consumption by one or more UI controllers and views.
 *
 * @param application [Application] context of this instance.
 */
class CrapsViewModel(application: Application) :
    AndroidViewModel(application),
    DefaultLifecycleObserver {

    private val crapsRepository: CrapsRepository = CrapsRepository()
    private val pending: CompositeDisposable = CompositeDisposable()
    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)
    private val batchSizePrefKey: String =
        application.resources.getString(R.string.batch_size_pref_key)
    private val batchSizePrefDefault: Int =
        application.resources.getInteger(R.integer.batch_size_pref_default)
    private val batchSizePreference: Int
        get() = Math.pow(
            10.0,
            preferences.getInt(batchSizePrefKey, batchSizePrefDefault).toDouble()
        ).toInt()

    private val _snapshot: MutableLiveData<Snapshot> = MutableLiveData()

    /**
     * Returns the [LiveData]&lt;[Snapshot]&gt; publishing the most recent simulation
     * tally and round.
     *
     * @return [LiveData]&lt;[Snapshot]&gt;
     */
    val snapshot: LiveData<Snapshot>
        get() = _snapshot

    private val _running: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Returns the [LiveData&amp;lt;Boolean&amp;gt;][LiveData] publishing the current running state of
     * the simulation.
     *
     * @return [LiveData&amp;lt;Boolean&amp;gt;][LiveData]
     */
    val running: LiveData<Boolean>
        get() = _running

    private val _throwable: MutableLiveData<Throwable> = MutableLiveData()

    /**
     * Returns the [LiveData]&lt;[Throwable]&gt; publishing the most recent [Throwable] thrown by
     * the simulation.
     *
     * @return [LiveData]&lt;[Throwable]&gt;
     */
    val throwable: LiveData<Throwable>
        get() = _throwable

    /**
     * Starts the simulation in continuous-execution mode, with each batch of rounds starting as
     * soon as the previous batch completes.
     */
    fun runFast() {
        _running.value = true
        crapsRepository.runFast(batchSizePreference)
    }

    /**
     * Simulates one batch of rounds of play.
     */
    fun runOnce() {
        crapsRepository.runOnce(batchSizePreference)
    }

    /**
     * Stops execution of a continuous-mode simulation.
     */
    fun stop() {
        crapsRepository.stop()
        _running.value = false
    }

    /**
     * Resets the simulation, publishing an empty snapshot representing the initial simulation
     * state.
     */
    fun reset() {
        crapsRepository.reset()
        _snapshot.value = Snapshot()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        subscribeToSnapshots()
    }

    override fun onPause(owner: LifecycleOwner) {
        pending.clear()
        super.onPause(owner)
    }

    private fun subscribeToSnapshots() {
        pending.add(
            crapsRepository
                .snapshots
                .subscribe(
                    { _snapshot.postValue(it) },
                    { postThrowable(it) }
                )
        )
    }

    private fun postThrowable(throwable: Throwable) {
        Log.e(javaClass.simpleName, throwable.message, throwable)
        _throwable.postValue(throwable)
    }

}