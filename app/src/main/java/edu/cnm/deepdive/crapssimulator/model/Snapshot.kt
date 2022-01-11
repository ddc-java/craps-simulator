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
 * Encapsulates a snapshot in some moment of a sequence of Craps rounds. No wagering actions or
 * outcomes are included in this snapshot; only the tally of wins and losses, along with the
 * sequence of [Roll] instances recorded in the most recent [Round], are included.
 *
 * As might be inferred from the name, instances of this class are immutable.
 */
class Snapshot(round: Round? = null, val wins: Long = 0, private val losses: Long = 0) {
    /**
     * Returns the [List&amp;lt;Roll&amp;gt;][List] from the most recently completed (when this `Snapshot` instance was created) [Round].
     *
     * @return [List&amp;lt;Roll&amp;gt;][List]
     */
    val rolls: List<Roll>

    /**
     * Returns the [State] of the most recent [Round] included in this snapshot.
     *
     * @return [State]
     */
    val state: Round.State

    /**
     * Returns a flag indicating whether the most recent [Round] terminated in the [ ][State.WIN] state.
     *
     * @return `boolean`
     */
    val win: Boolean

    /**
     * Initializes this instance to encapsulate the [List&amp;lt;Roll&amp;gt;][List] from the specified
     * [Round], and long with the specified tally of wins and losses.
     *
     * @param round A single [Round]presumably the most recently completed.
     * @param wins Tally of wins.
     * @param losses Tally of losses.
     */
    init {
        rolls = ArrayList(round?.rolls ?: Collections.emptyList())
        state = round?.state ?: Round.State.initial()
        win = round?.win ?: false
    }

    /**
     * Returns the total number of plays (wins + losses) in this snapshot.
     *
     * @return `long`
     */
    val rounds: Long
        get() = wins + losses
}