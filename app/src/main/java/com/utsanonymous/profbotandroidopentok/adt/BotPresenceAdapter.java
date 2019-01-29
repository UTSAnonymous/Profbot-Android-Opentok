package com.utsanonymous.profbotandroidopentok.adt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.utsanonymous.profbotandroidopentok.R;

import java.util.ArrayList;

public class BotPresenceAdapter extends ArrayAdapter<BotPresence> {

    private final Object mContext;
    private final int mResource;

    public BotPresenceAdapter(Context context, int resource, ArrayList<BotPresence> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        RecyclerView.ViewHolder holder;

        String botModel = getItem(position).getBotModel();
        String assignRoom = getItem(position).getAssignedRoom();
        String user = getItem(position).getUser();

        BotPresence BP = new BotPresence(botModel,assignRoom,user);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_view, parent, false);
        }

        TextView tvBotModel = (TextView) convertView.findViewById(R.id.textView1);
        TextView tvAssignRoom = (TextView) convertView.findViewById(R.id.textView3);
        TextView tvUser = (TextView) convertView.findViewById(R.id.textView2);

        tvBotModel.setText(botModel);
        tvAssignRoom.setText(assignRoom);
        tvUser.setText(user);

        return convertView;
    }
}
