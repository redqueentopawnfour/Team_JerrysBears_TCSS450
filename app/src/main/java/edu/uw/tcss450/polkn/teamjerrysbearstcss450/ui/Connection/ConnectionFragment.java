package edu.uw.tcss450.polkn.teamjerrysbearstcss450.ui.Connection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import edu.uw.tcss450.polkn.teamjerrysbearstcss450.R;

public class ConnectionFragment extends Fragment {

    private ConnectionViewModel connectionViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        connectionViewModel =
                ViewModelProviders.of(this).get(ConnectionViewModel.class);
        View root = inflater.inflate(R.layout.fragment_connection, container, false);
//        final TextView textView = root.findViewById(R.id.text_connection);
//        connectionViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
}