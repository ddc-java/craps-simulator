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
package edu.cnm.deepdive.crapssimulator.model;

import edu.cnm.deepdive.crapssimulator.model.Round.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates a snapshot in some moment of a sequence of Craps rounds. No wagering actions or
 * outcomes are included in this snapshot; only the tally of wins and losses, along with the
 * sequence of {@link Roll} instances recorded in the most recent {@link Round}, are included.
 * <p>As might be inferred from the name, instances of this class are immutable.</p>
 */
public final class Snapshot {

  private final List<Roll> rolls;
  private final long wins;
  private final long losses;
  private final State state;
  private final boolean win;

  /**
   * Initializes this instance to represent the start of a sequence of rounds, before any rolls take
   * place.
   */
  public Snapshot() {
    rolls = Collections.emptyList();
    wins = 0;
    losses = 0;
    state = Round.State.initial();
    win = false;
  }

  /**
   * Initializes this instance to encapsulate the {@link List List&lt;Roll&gt;} from the specified
   * {@link Round}, and long with the specified tally of wins and losses.
   *
   * @param round A single {@link Round}&mdash;presumably the most recently completed.
   * @param wins Tally of wins.
   * @param losses Tally of losses.
   */
  public Snapshot(Round round, long wins, long losses) {
    rolls = new ArrayList<>(round.getRolls());
    this.wins = wins;
    this.losses = losses;
    state = round.getState();
    win = round.isWin();
  }

  /**
   * Returns the {@link List List&lt;Roll&gt;} from the most recently completed (when this {@code
   * Snapshot} instance was created) {@link Round}.
   *
   * @return {@link List List&lt;Roll&gt;}
   */
  public List<Roll> getRolls() {
    return rolls;
  }

  /**
   * Returns the tally of wins in this snapshot.
   *
   * @return {@code long}
   */
  public long getWins() {
    return wins;
  }

  /**
   * Returns the tally of losses in this snapshot.
   *
   * @return {@code long}
   */
  public long getLosses() {
    return losses;
  }

  /**
   * Returns the total number of plays (wins + losses) in this snapshot.
   *
   * @return {@code long}
   */
  public long getRounds() {
    return wins + losses;
  }

  /**
   * Returns the {@link State} of the most recent {@link Round} included in this snapshot.
   *
   * @return {@link State}
   */
  public State getState() {
    return state;
  }

  /**
   * Returns a flag indicating whether the most recent {@link Round} terminated in the {@link
   * State#WIN} state.
   *
   * @return {@code boolean}
   */
  public boolean isWin() {
    return win;
  }

}
