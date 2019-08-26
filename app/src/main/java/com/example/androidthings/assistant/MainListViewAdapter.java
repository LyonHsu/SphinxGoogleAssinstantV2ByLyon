package com.example.androidthings.assistant;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.androidthings.assistant.Youtube.Item;

import java.util.ArrayList;



public class MainListViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<Item> arrayList = new ArrayList<>();
    String TAG = MainListViewAdapter.class.getSimpleName();
    private LayoutInflater mLayInf;
    public MainListViewAdapter(Context context, ArrayList<Item> arrayList){
        this.context=context;
        this.arrayList = arrayList;
        mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Item item = arrayList.get(i);
        switch (item.Type) {
            case Item.SPEAKTOGOTYPE:
                view = gotoSpeakToGoString(item.getString(), viewGroup);
                break;
            case Item.CUSTOMERTYPE:
                view = gotoCustomerString(item.getString(), viewGroup);
                break;
        }




        return view;
    }

    private View gotoSpeakToGoString(String s, ViewGroup viewGroup){
        View v = mLayInf.inflate(R.layout.string_list_view_item, viewGroup, false);
        TextView textView =(TextView) v.findViewById(R.id.StringText);
        textView.setGravity(Gravity.LEFT);
        textView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
        textView.setText("SpeakToGO:"+s);
        return v;
    }

    private View gotoCustomerString(String s, ViewGroup viewGroup){
        View v = mLayInf.inflate(R.layout.string_list_view_item, viewGroup, false);
        TextView textView =(TextView) v.findViewById(R.id.StringText);
        textView.setGravity(Gravity.RIGHT);
        textView.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
        textView.setText("Customer:"+s);
        return v;
    }
}
