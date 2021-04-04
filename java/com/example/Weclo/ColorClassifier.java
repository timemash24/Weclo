package com.example.Weclo;

import android.graphics.Color;
import android.util.Log;

public class ColorClassifier {

    private String[] color_classes= {"RED", "ORANGE", "YELLOW", "GREEN", "CYAN", "BLUE", "PURPLE", "PINK", "RED"};

    //set the range of hue for color_classes
    //ex) RED:0~20 ORANGE:20~45 ...
    private int[] hue = {20, 45, 70, 135, 190, 255, 285, 340, 360};

    //set the range of saturation and value for mono colors
    private double[] mono = {0.1, 0.1};
    public String classifyColors(int rgb) {
        String result_color="RED";  //initial color is red
        float[] hsv = convertToHSV(rgb);

        for(int i=0; i<hue.length; i++) {
            if(hsv[1]<=mono[0] || hsv[2]<=mono[1]) {
                result_color = "MONO";
                return result_color;
            }
            if(hsv[0]<=hue[i]) {
                result_color = color_classes[i];
                return result_color;
            }
        }
        return result_color;
    }

    public float[] convertToHSV(int rgb) {
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        float[] hsv = new float[3];

        Color.RGBToHSV(r, g, b, hsv);
        Log.e("hsv", "h="+hsv[0]+"s="+hsv[1]+"v="+hsv[2]);
        return hsv;
    }

}
