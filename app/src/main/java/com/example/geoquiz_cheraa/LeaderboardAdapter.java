package com.example.geoquiz_cheraa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ScoreViewHolder> {

    private List<Score> scoreList;

    public LeaderboardAdapter(List<Score> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.usernameTextView.setText(score.getUsername());
        holder.scoreValueTextView.setText(score.getScore() + " pts");
    }

    @Override
    public int getItemCount() {
        return scoreList != null ? scoreList.size() : 0;
    }

    public void updateData(List<Score> newScores) {
        this.scoreList = newScores;
        notifyDataSetChanged();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, scoreValueTextView;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreValueTextView = itemView.findViewById(R.id.scoreValueTextView);
        }
    }
}
