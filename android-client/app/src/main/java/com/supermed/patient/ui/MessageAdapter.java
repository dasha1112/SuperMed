package com.supermed.patient.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.supermed.patient.R;
import com.supermed.patient.model.Message;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    private String currentUsername;
    private static final int VIEW_TYPE_INCOMING = 0;
    private static final int VIEW_TYPE_OUTGOING = 1;

    public MessageAdapter(@NonNull Context context, int resource, @NonNull List<Message> messages) {
        super(context, resource, messages);
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        this.currentUsername = prefs.getString("username", "");
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = getItem(position);
        if (msg == null) return VIEW_TYPE_INCOMING;
        return msg.getSenderUsername().equals(currentUsername) ?
                VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Message msg = getItem(position);
        if (msg == null) {
            return convertView != null ? convertView :
                    LayoutInflater.from(getContext()).inflate(R.layout.item_message_incoming, parent, false);
        }

        int viewType = getItemViewType(position);
        int layoutRes = viewType == VIEW_TYPE_OUTGOING ?
                R.layout.item_message_outgoing : R.layout.item_message_incoming;

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.tvText = convertView.findViewById(R.id.tv_message_text);
            holder.tvTime = convertView.findViewById(R.id.tv_message_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder.tvText != null) {
            holder.tvText.setText(msg.getMessageText());
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText(formatTimestamp(msg.getTimestamp()));
        }

        return convertView;
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";

        // Форматируем timestamp для удобного отображения
        try {
            if (timestamp.contains(" ")) {
                String[] parts = timestamp.split(" ");
                if (parts.length > 1) {
                    String timePart = parts[1];
                    if (timePart.contains(":") && timePart.length() > 5) {
                        return timePart.substring(0, 5); // HH:mm
                    }
                }
            }
            return timestamp;
        } catch (Exception e) {
            return timestamp;
        }
    }

    static class ViewHolder {
        TextView tvText;
        TextView tvTime;
    }
}