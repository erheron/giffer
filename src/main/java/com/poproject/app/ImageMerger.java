package com.poproject.app;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class ImageMerger {
/*
    public static ArrayList<BufferedImage> TransformBy(ArrayList<BufferedImage> imageList, Function<Double, Function<Warping.Vector2D, Warping.Vector2D>> doubleFunctionFunction, double v, GifferTask<Integer> gifferTask) {
        ArrayList<BufferedImage> images = MeanImage.normalize(imageList, imageList.get(0).getWidth(), imageList.get(0).getHeight());
        ArrayList<BufferedImage> res = new ArrayList<>();
        for(int i = 0; i +1 < images.size(); ++i) {

            res.addAll(MergeTwo(images.get(i), images.get(i+1), doubleFunctionFunction, v, gifferTask));

        }
        return res;
    }

    private static Collection<? extends BufferedImage> MergeTwo(BufferedImage image, BufferedImage image1, Function<Double, Function<Warping.Vector2D, Warping.Vector2D>> doubleFunctionFunction, double v, GifferTask<Integer> gifferTask) {
        ArrayList<BufferedImage> tmp = Warping.singleImageGif(image, doubleFunctionFunction,v,  gifferTask);
        ArrayList<BufferedImage> tmp1 = Warping.singleImageGif(image1, doubleFunctionFunction, v, gifferTask);
        ArrayList<BufferedImage> res = new ArrayList<>(tmp);

        ArrayList<BufferedImage> res1 = new ArrayList<>(tmp1);
        Collections.reverse(tmp);
        Collections.reverse(tmp1);
        res.addAll(tmp);
        res1.addAll(tmp1);
        return MeanImage.mergeTwoSeries(res, res1);
    }*/
}