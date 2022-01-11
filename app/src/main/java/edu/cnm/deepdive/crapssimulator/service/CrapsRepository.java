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
package edu.cnm.deepdive.crapssimulator.service;

import edu.cnm.deepdive.crapssimulator.model.Round;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Encapsulates the Craps simulation engine, and acts as a source of simulation data. In this
 * implementation, the {@link org.apache.commons.rng.core.source64.XoShiRo256PlusPlus} pseudorandom
 * number generator is used as a source of randomness.
 */
public class CrapsRepository {

  private static final long SLEEP_INTERVAL = 100;

  private final ScheduledExecutorService executor;
  private final Scheduler scheduler;
  private final Round round;

  private ScheduledFuture<?> future;
  private long wins;
  private long losses;
  private int roundsPerSnapshot;
  private boolean runningFast;
  private boolean runningOnce;

  /**
   * Initializes this instance. On completion, the simulation is ready to begin.
   */
  public CrapsRepository() {
    Random rng = new JDKRandomBridge(RandomSource.XO_RO_SHI_RO_128_PP, null);
    executor = Executors.newSingleThreadScheduledExecutor();
    scheduler = Schedulers.single();
    round = new Round(rng);
  }

  /**
   * Resets the running state and win/loss tally of this instance.
   */
  public void reset() {
    runningFast = false;
    runningOnce = false;
    wins = 0;
    losses = 0;
  }

  /**
   * Triggers publication of simulation {@link Snapshot} data as a {@link Flowable}&lt;{@link
   * Snapshot}&gt;. Note that this is a "cold" source: no data is published unless there is a
   * subscriber; in fact, the simulation will not run at all without a subscriber.
   * <p>A maximum of 128 snapshots (by default) will be buffered: If a subscriber is not able to
   * consume simulation snapshots as fast as they are published, older snapshots will be discarded
   * when the number of unconsumed snapshots exceeds this buffer size.</p>
   *
   * @return {@link Flowable}&lt;{@link Snapshot}&gt;
   */
  public Flowable<Snapshot> getSnapshots() {
    return Flowable
        .create((FlowableEmitter<Snapshot> emitter) -> {
          future = executor.scheduleWithFixedDelay(() -> {
            if (!emitter.isCancelled()) {
              while (!emitter.isCancelled() && runningFast) {
                play(roundsPerSnapshot);
                emitter.onNext(new Snapshot(round, wins, losses));
              }
              if (runningOnce) {
                runningOnce = false;
                play(roundsPerSnapshot);
                emitter.onNext(new Snapshot(round, wins, losses));
              }
            } else {
              future.cancel(true);
            }
          }, 0, SLEEP_INTERVAL, TimeUnit.MILLISECONDS);
        }, BackpressureStrategy.LATEST)
        .subscribeOn(scheduler);
  }

  /**
   * Starts or resumes execution of the simulation in continuous mode, with the specified number of
   * rounds simulated between snapshot publications.
   *
   * @param roundsPerSnapshot Number of rounds to be simulated between snapshots.
   */
  public void runFast(int roundsPerSnapshot) {
    this.roundsPerSnapshot = roundsPerSnapshot;
    runningFast = true;
  }

  /**
   * Simulates one batch of rounds, of the specified size.
   *
   * @param rounds Number of rounds to be simulated.
   */
  public void runOnce(int rounds) {
    roundsPerSnapshot = rounds;
    runningOnce = true;
  }

  /**
   * Suspends continuous-mode simulation. Note that the simulation will actually stop only when the
   * current batch of rounds has completed; thus, there may be a noticeable lag between invocation
   * of this method and the actual suspension of execution.
   */
  public void stop() {
    runningFast = false;
  }

  private void play(int count) {
    long wins = 0;
    long losses = 0;
    for (int i = 0; i < count; i++) {
      if (round.play()) {
        wins++;
      } else {
        losses++;
      }
    }
    this.wins += wins;
    this.losses += losses;
  }

}
