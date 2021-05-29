package com.example.poyraz.cse476project;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;

public class ProductFragment extends DialogFragment {

    TextView productName;
    ImageView productImage;
    Button pos, neg;

    DownloadImage download = new DownloadImage();

    public ProductFragment newInstance (String url, String productName, double price, String currencyAverage) {

        ProductFragment pf = new ProductFragment();
        Bundle bundle = new Bundle(4);
        bundle.putString("imageURL", url);
        bundle.putString("productName", productName);
        bundle.putDouble("price", price);
        bundle.putString("currency", currencyAverage);
        pf.setArguments(bundle);
        return pf;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.product_information, container, false);
        getDialog().setTitle("Search Result");
        String imageURL = getArguments().getString("imageURL");
        String productInformation = getArguments().getString("productName");
        productInformation += "\n" + getArguments().getDouble("price") + " " + getArguments().getString("currency");

        productImage = (ImageView) rootView.findViewById(R.id.image);
        productName = (TextView) rootView.findViewById(R.id.productInformation);

        productName.setText(productInformation);

        Bitmap image = download.DownloadImage(imageURL);
        productImage.setImageBitmap(image);

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
