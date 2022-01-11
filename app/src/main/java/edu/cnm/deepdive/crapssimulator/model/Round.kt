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

import java.util.*

/**
 * Encapsulates a single round of the Craps shooter's play. No wagering actions or outcomes are
 * included in this representation, but only one or more rollsfrom the come-out roll to the
 * final win or loss outcome.
 */
class Round(private val rng: Random) {

    private val _rolls: MutableList<Roll> = LinkedList()
    val rolls: MutableList<Roll>
        get() = Collections.unmodifiableList(_rolls)

    /**
     * Returns the current [State] of this instance.
     *
     * @return [State]
     */
    var state: State = State.initial()
        private set

    /**
     * Returns a flag indicating whether the current `Round` instance has terminated in a win.
     *
     * @return `boolean`
     */
    var win = false
        private set

    /**
     * Plays a complete round of Craps, starting with the come-out roll and including as many rolls as
     * necessary to arrive at a win or loss. Since the rules of Craps don't allow for any decisions by
     * the shooter after the round begins, all rolls are executed by this method without interruption.
     *
     * @return Flag indicating whether this instance has completed with a win.
     */
    fun play(): Boolean {
        _rolls.clear()
        state = State.initial()
        win = false
        var point = 0
        var firstRoll = true
        do {
            val roll = Roll(rng, NUM_DICE, NUM_SIDES)
            state = state.next(roll, point)
            if (firstRoll) {
                if (state === State.POINT) {
                    point = roll.value
                }
                firstRoll = false
            }
            _rolls.add(roll)
        } while (!state.isTerminal)
        win = state === State.WIN
        return win
    }

    /**
     * Enumerates the high-level states of a round of play in Craps.
     */
    enum class State {
        /** Awaiting initial roll.  */
        COME_OUT {
            override val isTerminal = false

            override fun next(roll: Roll, point: Int): State {
                return when (roll.value) {
                    2, 3, 12 -> LOSS
                    7, 11 -> WIN
                    else -> POINT
                }
            }
        },

        /** Point established in initial roll; awaiting win (roll of the same point) or loss (roll of 7).  */
        POINT {
            override val isTerminal = false

            override fun next(roll: Roll, point: Int): State {
                return when (roll.value) {
                    point -> WIN
                    7 -> LOSS
                    else -> this
                }
            }
        },

        /** Round won by rolling a natural (7 or 11) in the come-out roll, or by making the point established in that roll.  */
        WIN,

        /** Round lost by rolling a 2, 3, or 12 in the come-out roll, or by rolling 7 before rolling the point established in that roll.  */
        LOSS;

        /**
         * Returns a flag indicating whether the current `State` instance is a terminal state,
         * with no further rolls allowed.
         *
         * @return `boolean`
         */
        open val isTerminal = true

        /**
         * Computes the next `State` instance, based on the current instance, the point previously
         * established (if any), and the current [Roll].
         *
         * @param roll Current [Roll].
         * @param point Previously established point; ignored unless the current instance is [.POINT].
         * @return Next `State` instance.
         * @throws IllegalStateException If invoked after this instance is already in a terminal state.
         */
        @Throws(IllegalStateException::class)
        open fun next(roll: Roll, point: Int): State {
            throw IllegalStateException()
        }

        companion object {
            /**
             * Returns the initial state ([.COME_OUT]) of a round of play in Craps.
             *
             * @return [State]
             */
            @JvmStatic
            fun initial(): State {
                return COME_OUT
            }
        }
    }

    companion object {
        private const val NUM_DICE = 2
        private const val NUM_SIDES = 6
    }

}