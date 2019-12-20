package com.example.androidic;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

public class TensorflowStuff implements Classifier{

    FirebaseCustomRemoteModel cloudModel;
    FirebaseCustomLocalModel localModel;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInputOutputOptions inoutOptions;
    FirebaseModelInputs inputs;

    float[] probs = new float[1];
    String [] labels = new String[2];
    int[] intValues = new int[224*224];
    String modelName;
    FirebaseModelInterpreterOptions options;

    public TensorflowStuff(){}

    public static Classifier make(AssetManager am, String modelName, String labelName, int imgSize) throws IOException {

        final TensorflowStuff tf = new TensorflowStuff();


        tf.cloudModel = new FirebaseCustomRemoteModel.Builder(modelName).build();
        tf.localModel = new FirebaseCustomLocalModel.Builder().setAssetFilePath("model.tflite").build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();
        FirebaseModelManager.getInstance().download(tf.cloudModel,conditions).addOnSuccessListener(new OnSuccessListener<Void>() {

            public void onSuccess(Void v){

            }
        });

        FirebaseModelManager.getInstance().isModelDownloaded(tf.cloudModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                 FirebaseModelInterpreterOptions options;
                if (downloaded) {

                     options = new FirebaseModelInterpreterOptions.Builder(tf.cloudModel).build();
                     System.out.println("________DOWNLOADED____________");



                }else{
                     options = new FirebaseModelInterpreterOptions.Builder(tf.localModel).build();
                    Log.i("tag","NOT DOWNLOADED");
                }
                try {
                    tf.interpreter = FirebaseModelInterpreter.getInstance(options);
                } catch (FirebaseMLException e) {
                   e.printStackTrace();
                    Log.i("tag","INTERPRETOR HASSLE____________");
               }

            }
        });

        try {
                tf.inoutOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, imgSize, imgSize, 3})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1}).build();
                System.out.println("_______ inoutDONE ________");
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            System.out.println("_______________inout ERROR______________");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(labelName)));

        String line;
        int i = 0;
        while ((line = br.readLine()) != null) {
            tf.labels[i] = line;
            System.out.println("_______"+line);
            i++;
        }

        br.close();

        return tf;
    }

    @Override
    public List<Recogonition> recImg(Bitmap bm) {
        bm.createScaledBitmap(bm, 300, 300, false);


        int batchNum = 0;
        float[][][][]input = new float[1][300][300][3];
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                int pixel = bm.getPixel(x, y);
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
            }
        }
        System.out.println("___INPUT__");



        try {
            inputs = new FirebaseModelInputs.Builder().add(input).build();
            System.out.println("________ INPUT DONE ___________");

             } catch (FirebaseMLException e) {
            e.printStackTrace();
            System.out.println("INPUT ERROR ___________");
        }
        System.out.println("______RUNNING__________");
        interpreter.run(inputs, inoutOptions).addOnSuccessListener(new OnSuccessListener<FirebaseModelOutputs>() {
            @Override
            public void onSuccess(FirebaseModelOutputs result) {
                float[][] results;
                results = result.getOutput(0);
                probs = results[0];

                System.out.println("______ RAN __________");
                System.out.println("___LENGTH__"+probs.length);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("____ERROR___"+e);
            }
        });


        System.out.println("CONTINUING________"+probs[0]);
       /* PriorityQueue<Recogonition> pq = new PriorityQueue<Recogonition>(3, new Comparator<Recogonition>() {
            @Override
            public int compare(Recogonition o1, Recogonition o2) {
                return Float.compare(o2.getConfidence(),o1.getConfidence());
            }
        });

        for(int i= 0 ;i < probs.length;i++) {
            pq.add(new Recogonition(""+i,labels[i],probs[i]));
            System.out.println(probs[i]+" Nigger " + labels[i]);
        }*/

        ArrayList<Recogonition> finalResult = new ArrayList<>();

            if(probs[0]==0){
                finalResult.add(new Recogonition("","",0));
            }

            else if(probs[0]<0.5f){
                finalResult.add(new Recogonition("","Cat",probs[0]));
            }
            else{
                finalResult.add(new Recogonition("","Dog",probs[0]));
            }




        return finalResult;

    }

    public void close() {
            interpreter.close();
        }

    }


