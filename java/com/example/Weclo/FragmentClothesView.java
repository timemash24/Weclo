package com.example.Weclo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentClothesView extends Fragment {
    ImageView mClothesView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup)inflater.inflate(R.layout.viewer_fragment,container,false);

        mClothesView = (ImageView)rootView.findViewById(R.id.clothes_image);

        return rootView;
    }

    public void setImageChange(Bitmap photo){
        mClothesView.setImageBitmap(photo);
    }

}
