package com.poproject.app;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
 import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Filter{
    Filter(){}
    public BufferedImage blur(BufferedImage image){
        float[] blurMatrix = { 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f,
                1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f };
        BufferedImageOp blurFilter = new ConvolveOp(new Kernel(3, 3, blurMatrix),
                ConvolveOp.EDGE_NO_OP, null);
        return blurFilter.filter(image, null);
    }
    public BufferedImage sharpen(BufferedImage image) {
        float[] sharpenMatrix = {0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f};
        BufferedImageOp sharpenFilter = new ConvolveOp(new Kernel(3, 3, sharpenMatrix),
                ConvolveOp.EDGE_NO_OP, null);
        return sharpenFilter.filter(image, null);
    }
    public static int negativeRGB(int rgb) {
        return (1<<24)-1 - rgb;
    }
    public BufferedImage negative(BufferedImage image) {
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
         /* It's cache optimal iteration */
        for(int j = 0; j < res.getHeight(); ++j) {
            for(int i = 0; i < res.getWidth(); ++i) {
                res.setRGB(i, j, negativeRGB(image.getRGB(i, j)));
            }
        }
        return res;
    }
}