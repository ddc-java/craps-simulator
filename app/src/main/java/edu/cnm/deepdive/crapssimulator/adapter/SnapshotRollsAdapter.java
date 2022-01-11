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
package edu.cnm.deepdive.crapssimulator.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.adapter.SnapshotRollsAdapter.Holder;
import edu.cnm.deepdive.crapssimulator.databinding.ItemRollBinding;
import edu.cnm.deepdive.crapssimulator.model.Roll;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Adapts a {@link Snapshot} for use in a {@link RecyclerView}. Each {@link Roll} in {@link
 * Snapshot#getRolls()} is displayed as an item in the list, using vector drawable resources to
 * present the dice faces. The entire list is given a semi-transparent background color
 * corresponding to the outcome: red for a loss, green for a win.
 */
public class SnapshotRollsAdapter extends RecyclerView.Adapter<Holder> {

  @DrawableRes
  private static final int[] faceResources = {
      R.drawable.face_1,
      R.drawable.face_2,
      R.drawable.face_3,
      R.drawable.face_4,
      R.drawable.face_5,
      R.drawable.face_6
  };

  private final LayoutInflater inflater;
  private final Drawable[] faces;
  @ColorInt private final int winColor;
  @ColorInt private final int lossColor;
  private final List<Roll> rolls;
  private final boolean win;

  /**
   * Initializes this instance with the specified app {@link Context} and the {@link Snapshot} to be
   * adapted for display.
   *
   * @param context App context.
   * @param snapshot {@link Snapshot} to adapt.
   */
  public SnapshotRollsAdapter(Context context, Snapshot snapshot) {
    inflater = LayoutInflater.from(context);
    faces = IntStream
        .of(faceResources)
        .mapToObj((resId) -> ContextCompat.getDrawable(context, resId))
        .toArray(Drawable[]::new);
    winColor = ContextCompat.getColor(context, R.color.win_color);
    lossColor = ContextCompat.getColor(context, R.color.loss_color);
    rolls = snapshot.getRolls();
    win = snapshot.getWin();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new Holder(ItemRollBinding.inflate(inflater, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    holder.bind(position);
  }

  @Override
  public int getItemCount() {
    return rolls.size();
  }

  class Holder extends RecyclerView.ViewHolder {

    private final ItemRollBinding binding;

    private Holder(@NonNull ItemRollBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    private void bind(int position) {
      Roll roll = rolls.get(position);
      int[] dice = roll.getDice();
      binding.getRoot().setBackgroundColor(win ? winColor : lossColor);
      binding.die1.setImageDrawable(faces[dice[0] - 1]);
      binding.die2.setImageDrawable(faces[dice[1] - 1]);
      binding.value.setText(String.valueOf(roll.getValue()));
    }

  }

}
