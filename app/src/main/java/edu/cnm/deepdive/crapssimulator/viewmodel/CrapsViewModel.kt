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
package edu.cnm.deepdive.crapssimulator.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.service.CrapsRepository;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Exposes simulation-control methods and manages lifecycle-aware subset of system state (model
 * content), for consumption by one or more UI controllers and views.
 */
public class CrapsViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

  private final CrapsRepository crapsRepository;
  private final MutableLiveData<Snapshot> snapshot;
  private final MutableLiveData<Boolean> running;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;
  private final SharedPreferences preferences;
  private final String batchSizePrefKey;
  private final int batchSizePrefDefault;


  /**
   * Initializes this instance with the specified {@link Application} as a context.
   *
   * @param application App context.
   */
  public CrapsViewModel(@NonNull Application application) {
    super(application);
    crapsRepository = new CrapsRepository();
    snapshot = new MutableLiveData<>(new Snapshot());
    running = new MutableLiveData<>(false);
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    preferences = PreferenceManager.getDefaultSharedPreferences(application);
    Resources resources = application.getResources();
    batchSizePrefKey = resources.getString(R.string.batch_size_pref_key);
    batchSizePrefDefault = resources.getInteger(R.integer.batch_size_pref_default);
  }

  /**
   * Returns the {@link LiveData}&lt;{@link Snapshot}&gt; publishing the most recent simulation
   * tally and round.
   *
   * @return {@link LiveData}&lt;{@link Snapshot}&gt;
   */
  public LiveData<Snapshot> getSnapshot() {
    return snapshot;
  }

  /**
   * Returns the {@link LiveData LiveData&lt;Boolean&gt;} publishing the current running state of
   * the simulation.
   *
   * @return {@link LiveData LiveData&lt;Boolean&gt;}
   */
  public LiveData<Boolean> getRunning() {
    return running;
  }

  /**
   * Returns the {@link LiveData}&lt;{@link Throwable}&gt; publishing the most recent {@link
   * Throwable} thrown by the simulation.
   *
   * @return {@link LiveData}&lt;{@link Throwable}&gt;
   */
  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  /**
   * Starts the simulation in continuous-execution mode, with each batch of rounds starting as soon
   * as the previous batch completes.
   */
  public void runFast() {
    running.setValue(true);
    crapsRepository.runFast(getBatchSizePreference());
  }

  /**
   * Simulates one batch of rounds of play.
   */
  public void runOnce() {
    crapsRepository.runOnce(getBatchSizePreference());
  }

  /**
   * Stops execution of a continuous-mode simulation.
   */
  public void stop() {
    crapsRepository.stop();
    running.postValue(false);
  }

  /**
   * Resets the simulation, publishing an empty snapshot representing the initial simulation state.
   */
  public void reset() {
    crapsRepository.reset();
    snapshot.setValue(new Snapshot());
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onResume(owner);
    subscribeToSnapshots();
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    pending.clear();
    DefaultLifecycleObserver.super.onPause(owner);
  }

  private void subscribeToSnapshots() {
    pending.add(
        crapsRepository
            .getSnapshots()
            .subscribe(
                snapshot::postValue,
                this::postThrowable
            )
    );
  }

  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

  private int getBatchSizePreference() {
    return (int) Math.pow(10, preferences.getInt(batchSizePrefKey, batchSizePrefDefault));
  }

}
