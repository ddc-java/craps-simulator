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
package edu.cnm.deepdive.crapssimulator.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.cnm.deepdive.crapssimulator.R
import edu.cnm.deepdive.crapssimulator.databinding.ItemRollBinding
import edu.cnm.deepdive.crapssimulator.model.Roll
import edu.cnm.deepdive.crapssimulator.model.Snapshot
import java.util.stream.IntStream

/**
 * Adapts a [Snapshot] for use in a [RecyclerView]. Each [Roll] in [ ][Snapshot.getRolls] is displayed as an item in the list, using vector drawable resources to
 * present the dice faces. The entire list is given a semi-transparent background color
 * corresponding to the outcome: red for a loss, green for a win.
 */
class SnapshotRollsAdapter(context: Context, snapshot: Snapshot) :
    RecyclerView.Adapter<SnapshotRollsAdapter.Holder>() {

    private val inflater: LayoutInflater
    private val faces: Array<Drawable>
    @ColorInt
    private val winColor: Int
    @ColorInt
    private val lossColor: Int
    private val rolls: List<Roll>
    private val win: Boolean

    /**
     * Initializes this instance with the specified app [Context] and the [Snapshot] to be
     * adapted for display.
     *
     * @param context App context.
     * @param snapshot [Snapshot] to adapt.
     */
    init {
        inflater = LayoutInflater.from(context)
        faces = IntStream
            .of(*faceResources)
            .mapToObj { ContextCompat.getDrawable(context, it) }
            .toArray { arrayOfNulls(it) }
        winColor = ContextCompat.getColor(context, R.color.win_color)
        lossColor = ContextCompat.getColor(context, R.color.loss_color)
        rolls = snapshot.rolls
        win = snapshot.win
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemRollBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return rolls.size
    }

    inner class Holder (private val binding: ItemRollBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val roll = rolls[position]
            val dice = roll.dice
            with(binding) {
                root.setBackgroundColor(if (win) winColor else lossColor)
                die1.setImageDrawable(faces[dice[0] - 1])
                die2.setImageDrawable(faces[dice[1] - 1])
                value.text = roll.value.toString()
            }
        }
    }

    companion object {
        @DrawableRes
        private val faceResources = intArrayOf(
            R.drawable.face_1,
            R.drawable.face_2,
            R.drawable.face_3,
            R.drawable.face_4,
            R.drawable.face_5,
            R.drawable.face_6
        )
    }
}