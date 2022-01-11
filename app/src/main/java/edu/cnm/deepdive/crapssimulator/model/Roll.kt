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
package edu.cnm.deepdive.crapssimulator.model

import java.util.Random
import java.util.stream.IntStream

/**
 * Encapsulates a singe roll of one or more dice. Instances of this class are immutable: The number
 * of dice, and the value of each die, is fixed during initialization (using a provided source of
 * randomness), and may not be changed after that.
 */
class Roll(rng: Random, numDice: Int, numSides: Int) {

    private val _dice: IntArray
    val dice: IntArray
        get() = _dice.copyOf(_dice.size)

    /**
     * Returns the sum of dice values in this `Roll`.
     *
     * @return `int`
     */
    val value: Int

    /**
     * Initializes this instance by generating the dice values using the provided source of
     * randomness.
     *
     * @param rng Source of randomness.
     * @param numDice Number of dice to roll.
     * @param numSides Number of sides on each die.
     */
    init {
        _dice = IntStream
            .generate { 1 + rng.nextInt(numSides) }
            .limit(numDice.toLong())
            .toArray()
        value = IntStream
            .of(*_dice)
            .sum()
    }

}