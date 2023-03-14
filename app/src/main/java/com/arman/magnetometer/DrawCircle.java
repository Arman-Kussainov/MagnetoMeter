package com.arman.magnetometer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import java.util.Random;

public class DrawCircle {
    public static Bitmap Circle(Bitmap MyBitmap,int x, int y, int R, float B){


        for(int i=-R;i<=R;++i){
            for(int j=-R;j<=R;++j){

                if(i*i+j*j<=R*R) {
                    //MyBitmap.setPixel(x + i, y + j,
                    //        Color.BLACK);
                    //if (i*i+j*j>=(R-25) * (R-25)) {

                        @ColorInt int color = Color.HSVToColor(new float[]{B, 100.f, 100.f});
                        //@ColorInt int color1 = Color.argb( 255,0, 255, 255);
                        //Log.v("KCCP", String.valueOf(B));
                        MyBitmap.setPixel(x + i, y + j,color);
                        //MyBitmap.setPixel(x + i, y + j,
                        //        Color.argb(255, 255, 255, 255));


                    //}

                }
            }
        }

        return MyBitmap;
    }
}
