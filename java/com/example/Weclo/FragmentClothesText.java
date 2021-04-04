package com.example.Weclo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class FragmentClothesText extends Fragment {
    //View mView;
    public ImageClassifier classifier;
    public String predict;

    TextView mClothesText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup)inflater.inflate(R.layout.text_fragment,container,false);

        mClothesText = (TextView)rootView.findViewById(R.id.clothes_text);

        return rootView;
    }

    /** Load the model and labels. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            classifier = new ImageClassifier(getActivity());
        } catch (IOException e) {
            Log.e("classifier", "Failed to initialize an image classifier.");
        }

    }

    public String classifyLabel(Bitmap photo){
        String predict = classifier.classifyFrame(photo);
        return predict;
    }

    public void setTextChange(Bitmap photo){
        predict = classifier.classifyFrame(photo);
        mClothesText.setText(predict);
    }

}
