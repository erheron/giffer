/* author ziejcow */
package com.poproject.app;

import java.awt.image.BufferedImage;
import java.util.function.Function;



public class Warping {
    public static class Vector2D {
        Vector2D(int x, int y) {
            this.x = x;
            this.y = y;
        }
        int x;
        int y;
        double norm() {
            return Math.sqrt(x*x+y*y);
        }
    }
    /* Takes one  image and **Inverse** of morphism from [0,n)x[0,m) onto [0,n)x[0,m) */
    public static BufferedImage warp(BufferedImage image, Function<Vector2D, Vector2D> reverseMorphism) {
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        /* It's cache optimal iteration */
        for(int j = 0; j < res.getHeight(); ++j) {
            for(int i = 0; i < res.getWidth(); ++i) {

                Vector2D revXY = reverseMorphism.apply(new Vector2D(i, j));
                //System.out.println(revXY.x+ " " + revXY.y);
                res.setRGB(i, j, image.getRGB(revXY.x, revXY.y));
            }
        }
        return res;
    }








}