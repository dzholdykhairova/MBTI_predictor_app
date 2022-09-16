package kz.application.mbti.predictor;

//
// Created by Nurdaulet Anefiyayev on 25.04.20.
// Copyright (c) 2020 SDU Diploma Project. All rights reserved.
//

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

public class Predictor {

    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String INPUT_NODE = "ipnode";
    private static final String OUTPUT_NODE = "opnode";
    private static final int[] INPUT_SIZE = {1, 32, 32, 3};
    public static String[] mbti_types = new String[]{
            "ISTJ",
            "ISFJ",
            "INFJ",
            "INTJ",
            "ISTP",
            "ISFP",
            "INFP",
            "INTP",
            "ESTP",
            "ESFP",
            "ENFP",
            "ENTP",
            "ESTJ",
            "ESFJ",
            "ENFJ",
            "ENTJ"
    };

    private static int argmax(float[] elems) {
        int bestIdx = -1;
        float max = -1000;
        for (int i = 0; i < elems.length; i++) {
            float elem = elems[i];
            if (elem > max) {
                max = elem;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public static String recognize(Bitmap bitmap, AssetManager assetManager) {
        TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);

        final int inputSize = 32;
        final int width = 32;
        final int height = 32;

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);


        int[] intValues = new int[inputSize * inputSize];
        float[] floatValues = new float[inputSize * inputSize * 3];

        scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, width, height);
        for (int i = 0; i < intValues.length; i++) {
            int val = intValues[i];
            floatValues[i * 3] = (val >> 16) & 0xFF;
            floatValues[i * 3 + 1] = (val >> 8) & 0xFF;
            floatValues[i * 3 + 2] = val & 0xFF;
        }

        inferenceInterface.feed(INPUT_NODE, floatValues, 1, INPUT_SIZE.length, INPUT_SIZE.length, 1);
        inferenceInterface.run(new String[]{OUTPUT_NODE});
        float[] results = new float[10];
        Arrays.fill(results, 0.0f);
        inferenceInterface.fetch(OUTPUT_NODE, results);
        int class_id = argmax(results);
        return mbti_types[class_id];
    }
}

