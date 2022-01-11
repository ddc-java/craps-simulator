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

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Encapsulates a singe roll of one or more dice. Instances of this class are immutable: The number
 * of dice, and the value of each die, is fixed during initialization (using a provided source of
 * randomness), and may not be changed after that.
 */
public final class Roll {

  private final int[] dice;
  private final int value;

  /**
   * Initializes this instance by generating the dice values using the provided source of
   * randomness.
   *
   * @param rng Source of randomness.
   * @param numDice Number of dice to roll.
   * @param numSides Number of sides on each die.
   */
  public Roll(Random rng, int numDice, int numSides) {
    dice = IntStream
        .generate(() -> 1 + rng.nextInt(numSides))
        .limit(numDice)
        .toArray();
    value = IntStream
        .of(dice)
        .sum();
  }

  /**
   * Returns a safe copy of the dice values in this {@code Roll}.
   *
   * @return {@code int[]}
   */
  public int[] getDice() {
    return Arrays.copyOf(dice, dice.length);
  }

  /**
   * Returns the sum of dice values in this {@code Roll}.
   *
   * @return {@code int}
   */
  public int getValue() {
    return value;
  }

}
