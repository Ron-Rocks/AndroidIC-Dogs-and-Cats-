package com.example.androidic;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.camerakit.CameraKitView;
import com.example.androidic.R;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private int imgSize = 300;
    private int imgMean = 117;
    private float imgStd = 1;

    private String inputName = "input";
    private String outputName = "output";

    private String modelname = "AndroidICModel";
    private String labelName = "labels.txt";

    private Classifier classifier;
    private TextView resultView;
    private Button detectBtn;
    private ImageView iv;
    private CameraKitView cameraView;
    private Bitmap bm;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean isInit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("HELLOOOOOOOOOOOOOOOOOO");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = findViewById(R.id.resultImage);
        resultView = findViewById(R.id.resultText);
        detectBtn = findViewById(R.id.detectBtn);
        cameraView = findViewById(R.id.cameraView);




        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                /* To classify a stored image
                
                bm = getBitmap();
                bm.createScaledBitmap(bm, 300, 300, false);
                iv.setImageBitmap(bm);
                Bitmap newBm = bm.copy(Bitmap.Config.ARGB_8888,false);
                List<Classifier.Recogonition> results = classifier.recImg(newBm);

                String res = "";
                for(Classifier.Recogonition rec:results){
                    res+=rec.toString();
                }

                resultView.setText(res);
                */

                cameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                        Bitmap bm = BitmapFactory.decodeStream(stream);
                        iv.setImageBitmap(bm);

                        List<Classifier.Recogonition> results = classifier.recImg(bm);

                        resultView.setText(results.toString());
                    }
                });
            }
        });

        loadModel();


    }

    public Bitmap getBitmap() {
        InputStream is = null;
        try {
            is = getAssets().open("image.jpg");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("tag", "NOT GOT ASSET");
        }

        Bitmap bm = BitmapFactory.decodeStream(is);
        return bm;
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    @Override
    protected void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void loadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorflowStuff.make(getAssets(), modelname, labelName, imgSize);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
