package com.example.poyraz.cse476project;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.TextView;

import de.greenrobot.event.EventBus;

public class LocalProductFragment extends DialogFragment {

    TextView productName;
    Button neg, pos;

    public LocalProductFragment newInstance(String productName, String price) {

        LocalProductFragment pf = new LocalProductFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString("productName", productName);
        bundle.putString("price", price);
        pf.setArguments(bundle);
        return pf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.product_information_local, container, false);

        getDialog().setTitle("Search Result");

        String productInfo = getArguments().getString("productName");
        productInfo += "\n" + getArguments().getString("price") + " TL";
        productName = (TextView) rootView.findViewById(R.id.productInformation);
        productName.setText(productInfo);

        neg = (Button) rootView.findViewById(R.id.buttonNegative);
        pos = (Button) rootView.findViewById(R.id.buttonPositive);

        neg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        pos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                getDialog().dismiss();
            }
        });

        return rootView;
    }

}
