package com.poproject.app;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

enum CreationMode {
    SingleImageGif,
    MultipleImageGif,
    SimpleGif
}

public class GifCreator {

    MeanImage meanImage;
    Warper warper;
    CreationMode creationMode;
    ArrayList<BufferedImage> list;
    BufferedImage singleImage;
    float step;
    GifferTask currentTask;
    int estimatedFramesToProcess;
    double currentFrames;
    boolean loop;

    void estimateFrames(int numerOfTransforms, int singleTransformTime) {
        estimatedFramesToProcess = numerOfTransforms*singleTransformTime;
    }

    void newFrame(float weight) throws GifTaskCancelledException{
        currentFrames+=weight;
        if(currentTask != null && currentTask.isCancelled()) {
            throw new GifTaskCancelledException();
        }
        if(currentTask!= null) currentTask.updateGifferProgress(currentFrames, estimatedFramesToProcess);
    }

    public ArrayList<BufferedImage> generateFrames(GifferTask task) throws GifTaskCancelledException{
        currentTask = task;
        ArrayList<BufferedImage> res = null;
        switch (creationMode) {
            case SingleImageGif:
                res = singleImageFrames();
                break;
            case MultipleImageGif:
                res = multipleImageFrames();
                break;
            case SimpleGif:
                res = list;
        }
        if(loop) {
            res = loopMe(res);
        }
        return res;
    }

    private ArrayList<BufferedImage> loopMe(ArrayList<BufferedImage> list) {
        ArrayList<BufferedImage> tmp = new ArrayList<>(list);
        Collections.reverse(tmp);
        ArrayList<BufferedImage> res = new ArrayList<>(list);
        res.addAll(tmp);
        return res;
    }

    private ArrayList<BufferedImage> singleImageFrames() throws GifTaskCancelledException{
        /* Takes buffer image and applies warp as warpFunction progresses */
        estimateFrames(1,(int) (warper.estimator()*(1.0/step)));
        ArrayList<BufferedImage> res = new ArrayList<>();
        for(double alpha = 0; alpha <1; alpha+=step) {
            System.out.println(alpha);
            newFrame(warper.estimator());
            res.add(Warping.warp(singleImage, warper.function().apply(alpha)));
        }
        return res;

    }

    private ArrayList<BufferedImage> multipleImageFrames() throws GifTaskCancelledException{
        ArrayList<BufferedImage> res = new ArrayList<>();
        estimateFrames(list.size()-1,(int) (meanImage.estimateFrames(step) + warper.estimator()*(1.0/step)));
        for(int i = 0; i+1 < list.size(); ++i) {
            res.addAll(twoImageFrames(list.get(i), list.get(i + 1)));
        }
        return res;
    }

    private ArrayList<BufferedImage> twoImageFrames(BufferedImage image1, BufferedImage image2) throws GifTaskCancelledException {
        ArrayList<BufferedImage> tmp = meanImage.apply(image1, image2, step, this);
        int n = tmp.size()/2;
        int m = tmp.size();
        for(int i = 0; i < n; ++i) {
            System.out.println((double) i / (double) n);
            newFrame(warper.estimator());
            tmp.set(i, Warping.warp(tmp.get(i), warper.function().apply((double) i / (double) n)));
        }

        for(int i = n; i < m; ++i) {
            double left = (double) (m-i-1)/(double) (m-n-1);
            System.out.println(left);
            newFrame(warper.estimator());
            tmp.set(i, Warping.warp(tmp.get(i),  warper.function().apply(left)));
        }
        return tmp;
    }
}