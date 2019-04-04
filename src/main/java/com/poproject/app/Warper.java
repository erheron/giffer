package com.poproject.app;

//import sun.nio.cs.ext.MacThai;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class Warper {
    Function<Double, Function<Warping.Vector2D, Warping.Vector2D>> warperFunction;

    Function<Double, Function<Warping.Vector2D, Warping.Vector2D>> function() {
        return warperFunction;
    }
    float frameEst;
    float estimator() {
        return frameEst;
    }
}

class WarperFactory {
    static double edgeDistPerc = 0.00;
    public static Warping.Vector2D cutCorners(Warping.Vector2D v, int n, int m) {
        Warping.Vector2D vec = new Warping.Vector2D(Math.max(Math.min(v.x, (int) ((1 - edgeDistPerc) * (n - 1))),(int) (edgeDistPerc*n)), Math.max(Math.min(v.y,(int) ((1-edgeDistPerc)*(m-1))), (int)(edgeDistPerc*m)));
        //System.out.println(vec.x+ " " + vec.y + "  " + v.x + " " + v.y + " " + n + " " + m + "  " + (int) ((1-edgeDistPerc)*(n-1)) + " " + (int) (1-edgeDistPerc)*(m-1));
        return vec;
    }

    public static Warper expand(int width, int height, float factor) {
        Warper warper = new Warper();
        warper.warperFunction = Alpha-> Vec->{
            Warping.Vector2D center = new Warping.Vector2D(width/2, height/2);
            Warping.Vector2D normalized = new Warping.Vector2D(Vec.x - center.x, Vec.y - center.y);
            Warping.Vector2D newNormalized = new Warping.Vector2D((int) (normalized.x*Math.pow(0.0001+Math.abs(normalized.x)/ center.norm(), factor*Alpha )),(int) (normalized.y*Math.pow(0.0001 + Math.abs(normalized.y) / (center.norm()), factor*Alpha)));
            return cutCorners(new Warping.Vector2D(newNormalized.x+center.x, newNormalized.y+center.y), width, height);
        };
        warper.frameEst = 2f;
        return warper;
    }

    public static Warper funnyRotate(int width, int height, double angle) {
        Warper warper = new Warper();
        warper.warperFunction= Alpha-> Vec->{
            Warping.Vector2D center = new Warping.Vector2D(width/2, height/2);

            Warping.Vector2D normalized = new Warping.Vector2D(Vec.x - center.x, Vec.y - center.y);
            double newAngle = -angle*Alpha*normalized.norm()/center.norm()*3;
            Warping.Vector2D newNormalized = new Warping.Vector2D((int) (Math.cos(newAngle)*normalized.x - Math.sin(newAngle)*normalized.y),
                    (int) (Math.sin(newAngle)*normalized.x + Math.cos(newAngle)*normalized.y));

            return cutCorners(new Warping.Vector2D(newNormalized.x+center.x, newNormalized.y+center.y), width, height);
        };
        warper.frameEst = 3f;
        return warper;
    }
    public static Warper rotate(int width, int height, double angle) {
        Warper warper=new Warper();
        warper.warperFunction =Alpha->{
            return Vec->{
                Warping.Vector2D center = new Warping.Vector2D(width/2, height/2);

                Warping.Vector2D normalized = new Warping.Vector2D(Vec.x - center.x, Vec.y - center.y);
                double newAngle = -angle*Alpha;
                Warping.Vector2D newNormalized = new Warping.Vector2D((int) (Math.cos(newAngle)*normalized.x - Math.sin(newAngle)*normalized.y),
                        (int) (Math.sin(newAngle)*normalized.x + Math.cos(newAngle)*normalized.y));

                return cutCorners(new Warping.Vector2D(newNormalized.x+center.x,newNormalized.y+center.y), width, height);
            };
        };
        warper.frameEst = 3f;
        return warper;
    }

    public static Warper id() {
        Warper warper = new Warper();
        warper.warperFunction = Alpha->{
            return Vec->Vec;
        };
        warper.frameEst = 0.5f;
        return warper;
    }

}