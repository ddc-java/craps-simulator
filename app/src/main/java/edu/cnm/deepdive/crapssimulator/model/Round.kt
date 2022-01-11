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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates a single round of the Craps shooter's play. No wagering actions or outcomes are
 * included in this representation, but only one or more rolls&mdash;from the come-out roll to the
 * final win or loss outcome.
 */
public class Round {

  private static final int NUM_DICE = 2;
  private static final int NUM_SIDES = 6;

  private final Random rng;
  private final List<Roll> rolls;

  private State state;
  private boolean win;

  /**
   * Initializes this instance with the specified source of randomness.
   *
   * @param rng Instance of {@link Random} from which random numbers are sampled.
   */
  public Round(Random rng) {
    this.rng = rng;
    rolls = new LinkedList<>();
  }

  /**
   * Plays a complete round of Craps, starting with the come-out roll and including as many rolls as
   * necessary to arrive at a win or loss. Since the rules of Craps don't allow for any decisions by
   * the shooter after the round begins, all rolls are executed by this method without interruption.
   *
   * @return Flag indicating whether this instance has completed with a win.
   */
  public boolean play() {
    rolls.clear();
    state = State.initial();
    win = false;
    int point = 0;
    boolean firstRoll = true;
    do {
      Roll roll = new Roll(rng, NUM_DICE, NUM_SIDES);
      state = state.next(roll, point);
      if (firstRoll) {
        if (state == State.POINT) {
          point = roll.getValue();
        }
        firstRoll = false;
      }
      rolls.add(roll);
    } while (!state.isTerminal());
    win = (state == State.WIN);
    return win;
  }

  /**
   * Returns a copy of the {@link List List&lt;Roll&gt;} rolled so far in this instance.
   *
   * @return {@link List List&lt;Roll&gt;}
   */
  public List<Roll> getRolls() {
    return Collections.unmodifiableList(rolls);
  }

  /**
   * Returns the current {@link State} of this instance.
   *
   * @return {@link State}
   */
  public State getState() {
    return state;
  }

  /**
   * Returns a flag indicating whether the current {@code Round} instance has terminated in a win.
   *
   * @return {@code boolean}
   */
  public boolean isWin() {
    return win;
  }

  /**
   * Enumerates the high-level states of a round of play in Craps.
   */
  public enum State {
    /** Awaiting initial roll. */
    COME_OUT {

      @Override
      protected boolean isTerminal() {
        return false;
      }

      @Override
      protected State next(Roll roll, int ignoredPoint) {
        State state;
        switch (roll.getValue()) {
          case 2:
          case 3:
          case 12:
            state = LOSS;
            break;
          case 7:
          case 11:
            state = WIN;
            break;
          default:
            state = POINT;
            break;
        }
        return state;
      }

    },
    /** Point established in initial roll; awaiting win (roll of the same point) or loss (roll of 7). */
    POINT {

      @Override
      protected boolean isTerminal() {
        return false;
      }

      @Override
      protected State next(Roll roll, int point) {
        State state;
        int value = roll.getValue();
        if (value == point) {
          state = WIN;
        } else if (value == 7) {
          state = LOSS;
        } else {
          state = this;
        }
        return state;
      }

    },
    /** Round won by rolling a natural (7 or 11) in the come-out roll, or by making the point established in that roll. */
    WIN,
    /** Round lost by rolling a 2, 3, or 12 in the come-out roll, or by rolling 7 before rolling the point established in that roll. */
    LOSS;

    /**
     * Returns the initial state ({@link #COME_OUT}) of a round of play in Craps.
     *
     * @return {@link State}
     */
    public static State initial() {
      return COME_OUT;
    }

    /**
     * Returns a flag indicating whether the current {@code State} instance is a terminal state,
     * with no further rolls allowed.
     *
     * @return {@code boolean}
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isTerminal() {
      return true;
    }

    /**
     * Computes the next {@code State} instance, based on the current instance, the point previously
     * established (if any), and the current {@link Roll}.
     *
     * @param roll Current {@link Roll}.
     * @param point Previously established point; ignored unless the current instance is {@link #POINT}.
     * @return Next {@code State} instance.
     * @throws IllegalStateException If invoked after this instance is already in a terminal state.
     */
    protected State next(Roll roll, int point) throws IllegalStateException {
      throw new IllegalStateException();
    }

  }

}
