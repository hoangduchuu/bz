package com.ping.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ContactAutoCompleteAdapter extends ArrayAdapter<User> {
    private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<User> items;
    private ArrayList<User> itemsAll;
    private ArrayList<User> suggestions;
    private UserRepository userRepository;

    Filter nameFilter = new Filter() {
        private HashMap<String, User> userList = new HashMap<>();

        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((User) (resultValue)).pingID;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                String textSearch = constraint.toString();
                searchUsers(textSearch);
//                suggestions.clear();
//                for (User contact : itemsAll) {
//                    if (CommonMethod.isFiltered(contact, constraint.toString())) {
//                        suggestions.add(contact);
//                    }
//                }
//                FilterResults filterResults = new FilterResults();
//                filterResults.values = suggestions;
//                filterResults.count = suggestions.size();
//                return filterResults;
//            } else {
//
//            }
            }
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Collection<User> filteredList = (Collection<User>) results.values;
            if (results.count > 0) {
                clear();
                for (User c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }

        private void searchUsers(String text) {
            userRepository.searchUsersWithText(text, "first_name", new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    if (error == null) {
                        DataSnapshot snapshot = (DataSnapshot) data[0];
                        handleUsersData(snapshot);
                    }
                }
            });
        }

        private void handleUsersData(DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (!userList.containsKey(snapshot.getKey())) {
                    User user = new User(snapshot);
                    userList.put(snapshot.getKey(), user);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = userList.values();
            filterResults.count = userList.size();
            publishResults(null, filterResults);
        }
    };
    private int viewResourceId;

    public ContactAutoCompleteAdapter(Context context, int viewResourceId, ArrayList<User> items) {
        super(context, viewResourceId, (ArrayList<User>) items.clone());
        this.items = (ArrayList<User>) items.clone();
        this.itemsAll = (ArrayList<User>) items.clone();
        this.suggestions = new ArrayList<User>();
        this.viewResourceId = viewResourceId;
        this.userRepository = new UserRepository();
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        if (suggestions == null || suggestions.size() == 0) {
            return v;
        }
        if(position < 0 || position >= suggestions.size())
            return v;
        User user = suggestions.get(position);
        if (user != null) {
            TextView tvContact = (TextView) v.findViewById(R.id.item_complete_contact);
            tvContact.setText(user.getDisplayName());
        }
        return v;
    }

    @Override
    public int getCount() {
        return items.size();
    }
}