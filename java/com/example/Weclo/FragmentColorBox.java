package com.example.Weclo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

public class FragmentColorBox extends Fragment {
    private ColorClassifier colorClassifier;
    ImageView[] mColorView = new ImageView[4];
    CheckBox[] mCheckBox = new CheckBox[4];
    String[] selected_colors = {"", "", "", "", "", "", ""};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup)inflater.inflate(R.layout.checkbox_fragment,container,false);


        mColorView[0] = (ImageView)rootView.findViewById(R.id.colorBox1);
        mColorView[1] = (ImageView)rootView.findViewById(R.id.colorBox2);
        mColorView[2] = (ImageView)rootView.findViewById(R.id.colorBox3);
        mColorView[3] = (ImageView)rootView.findViewById(R.id.colorBox4);

        mCheckBox[0] = (CheckBox)rootView.findViewById(R.id.checkBox1);
        mCheckBox[1] = (CheckBox)rootView.findViewById(R.id.checkBox2);
        mCheckBox[2]= (CheckBox)rootView.findViewById(R.id.checkBox3);
        mCheckBox[3] = (CheckBox)rootView.findViewById(R.id.checkBox4);

        return rootView;
    }

    //이미지에서 테마 색 추출 후 표시
    public void setColorBox(Palette palette)
    {
        int[] colors = new int[7];
        String[] selected_colors = new String[7];
        String hexColor="";
        int i = 0;
        if(palette==null){
            Toast.makeText(getContext(),"색추출실패",Toast.LENGTH_SHORT).show();
            return;
        }

        else {
            if (palette.getDominantSwatch() != null) {

                colors[i] = palette.getDominantSwatch().getRgb();
                i++;

            }
            if (palette.getVibrantSwatch() != null) {

                colors[i] = palette.getVibrantSwatch().getRgb();
                i++;
            }
            if (palette.getLightVibrantSwatch() != null) {

                colors[i] = palette.getLightVibrantSwatch().getRgb();
                i++;

            }
            if (palette.getDarkVibrantSwatch() != null) {

                colors[i] = palette.getDarkVibrantSwatch().getRgb();
                i++;

            }
            if (palette.getMutedSwatch() != null) {

                colors[i] = palette.getDominantSwatch().getRgb();
                i++;
            }
            if (palette.getLightMutedSwatch() != null) {

                colors[i] = palette.getLightMutedSwatch().getRgb();
                i++;

            }
            if (palette.getDarkMutedSwatch() != null) {

                colors[i] = palette.getDarkMutedSwatch().getRgb();
                i++;

            }
        }

        Log.e("palette", "dominantColorFromBitmap = " + Integer.toString(colors[0], 16));

        for(i=0; i<4; i++) {
            mColorView[i].setBackgroundColor(colors[i]);
        }
//        hexColor = String.format("#%06X", (0xFFFFFF & colors[]));
        colorClassifier = new ColorClassifier();
        for(int j=0; j<4; j++) {
            selected_colors[j] = colorClassifier.classifyColors(colors[j]);
//            mCheckBox[j].setText(selected_colors[j]);
        }

    }

    public void getSelectedColor (){
        int num = 0;
        for (int i = 0; i<4 ; i++){
            if(mCheckBox[i].isChecked()){
                selected_colors[num] = mCheckBox[i].getText().toString();
                num++;
            }
        }
    }


    public String getColor1 (){
        return selected_colors[0];
    }

    public String getColor2 (){
        int count=0;
        for(int j=0; j<4; j++){
            if (mCheckBox[j].isChecked()){
                count++;
            }
        }
        if(count > 1){
            return selected_colors[1];
        }
        else
            return "none";
    }


    public String getPattern(){
        int count=0;
        for(int j=0; j<4; j++){
            if (mCheckBox[j].isChecked()){
                count++;
            }
        }
        if(count > 1){
            return "true";
        }
        else
            return "false";
    }

    public void setFinalColors(TextView mTextView) {
        String[] selected_colors = {"", "", "", "", "", "", ""};
        mTextView.setText(selected_colors[0]);
    }
}
