package com.example.androidic;


import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

import static java.lang.Math.round;

public interface Classifier {

    public class Recogonition{
        private String id;
        private String title;
        private float confidence;

        public Recogonition(String id,String title,float confidence){
            this.id= id;
            this.title = title;
            this.confidence = confidence;
        }

        public String getTitle(){
            return title;
        }
        public String getId(){
            return id;
        }

        public float getConfidence(){
            return confidence;
        }

        @Override
        public  String toString(){
            String result ="";
            if(confidence != 0) {
                if(confidence<0.5f) {
                    result = "   "+title + " :  " +round(100*(1- confidence)) + "% \n";
                }else {
                    result ="   " +title + " :  " + round(100*confidence) + "% \n";
                }
            }else{
                result = "";
            }
            return result;
        }
    }
    List<Recogonition> recImg(Bitmap bm);

    void close();
}
