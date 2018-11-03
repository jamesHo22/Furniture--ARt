package com.example.jamesho.olingetswhacky;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button getPhotoButton = findViewById(R.id.getPhoto);
        final ImageView histogram = findViewById(R.id.histogram);
        final ImageView originalImage = findViewById(R.id.imageView);
        final String artistImageUrl = "https://cdn.patchcdn.com/users/22847585/stock/T800x600/2016015694502bc5366.jpg";
        OpenCVLoader.initDebug();

        getPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "HIIII", Toast.LENGTH_SHORT).show();

                Picasso.with(getApplicationContext())
                        .load(artistImageUrl)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                // Create the required arrays and convert the bitmap (our image)
                                // so that it fits the array.
                                Mat rgba = new Mat();
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                                Utils.bitmapToMat(resizedBitmap, rgba);

                                // Get the bitmap size.
                                Size rgbaSize = rgba.size();

                                Log.v("Array Height", String.valueOf(rgbaSize.height));
                                Log.v("Array Width", String.valueOf(rgbaSize.width));
                                Log.v("Array Area", String.valueOf(rgbaSize.area()));
                                Log.v("Array secondPix", String.valueOf(rgba.get(0,0)[1]));

                                ArrayList<Double> reds = new ArrayList<Double>();
                                ArrayList<Double> blues = new ArrayList<Double>();
                                ArrayList<Double> greens = new ArrayList<Double>();


                                for (int i = 0; i < rgbaSize.height; i++) {
                                    //Log.v("height count", String.valueOf(i) + " BGR values: " + String.valueOf(rgba.get(i, 0)[0]));
                                    for (int j = 0; j < rgbaSize.width; j++) {
                                        reds.add(rgba.get(i, j)[0]);
                                        greens.add(rgba.get(i, j)[1]);
                                        blues.add(rgba.get(i, j)[2]);
                                    }
                                }

                                double sumReds = 0;
                                for(Double r : reds)
                                    sumReds += r/(255*rgbaSize.area());

                                double sumGreens = 0;
                                for(Double g : greens)
                                    sumGreens += g/(255*rgbaSize.area());

                                double sumBlues = 0;
                                for(Double b : blues)
                                    sumBlues += b/(255*rgbaSize.area());

                                Log.v("Colors!", String.valueOf(sumReds) +" ,"+ String.valueOf(sumGreens) + " ," +String.valueOf(sumBlues));

                                // Set the amount of bars in the histogram.
                                int histSize = 256;
                                MatOfInt histogramSize = new MatOfInt(histSize);

                                // Set the height of the histogram and width of the bar.
                                int histogramHeight = (int) rgbaSize.height;
                                int binWidth = 5;

                                // Set the value range.
                                MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

                                // Create two separate lists: one for colors and one for channels (these will be used as separate datasets).
                                Scalar[] colorsRgb = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
                                Log.v("Array Length", String.valueOf(colorsRgb[0]));
                                MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};

                                // Create an array to be saved in the histogram and a second array, on which the histogram chart will be drawn.
                                Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};
                                Mat histMatBitmap = new Mat(rgbaSize, rgba.type());

                                for (int i = 0; i < channels.length; i++) {
                                    Imgproc.calcHist(Collections.singletonList(rgba), channels[i], new Mat(), histograms[i], histogramSize, histogramRange);
                                    Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
                                    for (int j = 0; j < histSize; j++) {
                                        Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[i].get(j - 1, 0)[0]));
                                        Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[i].get(j, 0)[0]));
                                        Imgproc.line(histMatBitmap, p1, p2, colorsRgb[i], 2, 8, 0);
                                    }
                                }

                                Bitmap histBitmap = Bitmap.createBitmap(histMatBitmap.cols(), histMatBitmap.rows(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(histMatBitmap, histBitmap);
                                histogram.setImageBitmap(Bitmap.createScaledBitmap(histBitmap, 120, 120, false));

                                Picasso.with(getApplicationContext())
                                        .load(artistImageUrl)
                                        .into(new Target() {
                                            @Override
                                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                            /* Save the bitmap or do something with it here */

                                                // Set it in the ImageView
                                                originalImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));
                                            }

                                            @Override
                                            public void onBitmapFailed(Drawable errorDrawable) {

                                            }

                                            @Override
                                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                                            }
                                        });

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });

            }
        });


    }
}
