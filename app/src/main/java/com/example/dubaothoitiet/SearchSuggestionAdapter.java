package com.example.dubaothoitiet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SearchSuggestionAdapter extends ArrayAdapter<SearchSuggestionAdapter.SuggestionItem> {

    public static class SuggestionItem {
        public final String title;
        public final String subtitle;
        public final String query;
        public final boolean isHistory;

        public SuggestionItem(String title, String subtitle, String query, boolean isHistory) {
            this.title = title;
            this.subtitle = subtitle;
            this.query = query;
            this.isHistory = isHistory;
        }
    }

    public SearchSuggestionAdapter(Context context, List<SuggestionItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search_suggestion, parent, false);
        }

        SuggestionItem item = getItem(position);
        if (item != null) {
            TextView tvTitle = convertView.findViewById(R.id.tvSuggestionTitle);
            TextView tvSubtitle = convertView.findViewById(R.id.tvSuggestionSubtitle);
            ImageView ivIcon = convertView.findViewById(R.id.ivSuggestionIcon);

            boolean en = SettingsActivity.isEnglish(getContext());
            tvTitle.setText(en ? VietnamLocations.toEnglishLocation(item.title) : item.title);
            if (item.subtitle != null && !item.subtitle.isEmpty()) {
                tvSubtitle.setText(en ? VietnamLocations.toEnglishLocation(item.subtitle) : item.subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }

            if (item.isHistory) {
                ivIcon.setImageResource(android.R.drawable.ic_menu_recent_history);
            } else {
                ivIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
            }
        }

        return convertView;
    }
}
