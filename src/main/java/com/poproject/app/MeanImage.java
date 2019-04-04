/* Author ziejcow */
package com.poproject.app;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

/* Class MeanImage implements methods to smoothly transform one image into another
    of the same with and height,
 */

public interface MeanImage {
    int estimateFrames(float step);
    ArrayList<BufferedImage> apply(BufferedImage image1, BufferedImage image2, float step, GifCreator gifCreator) throws GifTaskCancelledException;
}

abstract class MeanImageIndependent implements MeanImage{
    public BufferedImage getImageMean(BufferedImage a, BufferedImage b, float h) {
        return null;
    }
    @Override
    public int estimateFrames(float step) {
        return (int) ((1.0/step)*estimator());
    }
    float estimator() {
        return 0.5f;
    }
    @Override
    public ArrayList<BufferedImage> apply(BufferedImage a, BufferedImage b, float step, GifCreator gifCreator) throws GifTaskCancelledException{
        ArrayList<BufferedImage> res = new ArrayList<>();
        res.add(a);
        for(float h = 1 ; h >= 0; h-=step) {
            gifCreator.newFrame(estimator());
            res.add(getImageMean(a, b, h));
        }
        return res;
    }
}

class MeanImageFactory {

    public static MeanImage sqrtMeanImage() {
        return new MeanImageIndependent() {
            @Override
            public BufferedImage getImageMean(BufferedImage a, BufferedImage b, float h) {
                BufferedImage res = new BufferedImage(a.getWidth(), a.getHeight(), a.getType());

                /* It's cache optimal iteration */
                for(int j = 0; j < res.getHeight(); ++j) {
                    for(int i = 0; i < res.getWidth(); ++i) {
                        res.setRGB(i, j, meanSqrRGB(a.getRGB(i, j), b.getRGB(i, j), h));
                    }
                }
                return res;
            }
        };
    }

    public static MeanImage linearMeanImage() {
        return new MeanImageIndependent() {
            @Override
            public BufferedImage getImageMean(BufferedImage a, BufferedImage b, float h) {
                BufferedImage res = new BufferedImage(a.getWidth(), a.getHeight(), a.getType());

                /* It's cache optimal iteration */
                for(int j = 0; j < res.getHeight(); ++j) {
                    for(int i = 0; i < res.getWidth(); ++i) {

                        res.setRGB(i, j, meanRGB(a.getRGB(i, j), b.getRGB(i, j), h));
                    }
                }
                return res;
            }
        };
    }

    public static MeanImage blurrMeanImage() {
        return new MeanImage() {
            @Override
            public int estimateFrames(float step) {
                return (int) ((1.0/step)*estimator());
            }
            float estimator() {
                return 0.7f;
            }
            @Override
            public ArrayList<BufferedImage> apply(BufferedImage image1, BufferedImage image2, float step, GifCreator gf) throws GifTaskCancelledException{
                boolean filled[][] = new boolean[image2.getWidth()][image2.getHeight()];
                int toFill = image2.getHeight()*image2.getWidth();
                ArrayList<BufferedImage> res = new ArrayList<>();
                res.add(image1);
                int pixelsPerStep = (int) Math.max((int) toFill * step, (int) 2);
                while(toFill>0) {
                    System.out.println("To Fill = " + toFill);
                    int currFilling = Math.min(pixelsPerStep, toFill);
                    gf.newFrame(estimator());
                    res.add(fillPixels(image1, image2,filled,  currFilling));
                    toFill-=currFilling;
                }
                return res;
            }
        };
    }

    public static BufferedImage scaleImageToFit(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage res = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = res.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return res;
    }

     /* computes weighted mean of two images, first with weight h, another with weight 1-h */
    static BufferedImage getLinearMean(BufferedImage a, BufferedImage b, float h) {
        BufferedImage res = new BufferedImage(a.getWidth(), a.getHeight(), a.getType());
        /* It's cache optimal iteration */
        for(int j = 0; j < res.getHeight(); ++j) {
            for(int i = 0; i < res.getWidth(); ++i) {
                res.setRGB(i, j, meanSqrRGB(a.getRGB(i, j), b.getRGB(i, j), h));
            }
        }


        return res;

    }

    /* weighted mean of two pixels */
    private static int meanRGB(int rgb, int rgb1, float h) {
        Color color = new Color(rgb);
        Color color1 = new Color(rgb1);
        int Red = (int) (h*(color.getRed()) + (1-h)*(color1.getRed()));
        int Green = (int) (h * (color.getGreen()) + (1 - h) * (color1.getGreen()));
        int Blue = (int) (h * (color.getBlue()) + (1 - h) * (color1.getBlue()));
        int Alpha = (int) (h* (color.getAlpha()) + (1 -h) * (color1.getAlpha()));
        return new Color(Red, Green, Blue, Alpha).getRGB();
    }

    private static int meanSqrRGB(int rgb, int rgb1, float h) {
        Color color = new Color(rgb);
        Color color1 = new Color(rgb1);
        int Red = (int) Math.sqrt(h*(color.getRed()*color.getRed()) + (1-h)*(color1.getRed()*color1.getRed()));
        int Green = (int) Math.sqrt(h * (color.getGreen()*color.getGreen()) + (1 - h) * (color1.getGreen()*color1.getGreen()));
        int Blue = (int) Math.sqrt(h * (color.getBlue()*color.getBlue()) + (1 - h) * (color1.getBlue()*color1.getBlue()));
        int Alpha = (int) Math.sqrt(h* (color.getAlpha()*color.getAlpha()) + (1 -h) * (color1.getAlpha()*color1.getAlpha()));
        return new Color(Red, Green, Blue, Alpha).getRGB();
    }



    /* Computes series of images starting from first, getting more and more similar to second */
    static ArrayList<BufferedImage> getSmoothSeries(BufferedImage a, BufferedImage b, float step) {
        ArrayList<BufferedImage> res = new ArrayList<>();
        res.add(a);
        for(float h = 1 ; h >= 0; h-=step) {
            res.add(getLinearMean(a, b, h));
        }
        return res;
    }



    public static ArrayList<BufferedImage> normalize(ArrayList<BufferedImage> list, int width, int height) {
        ArrayList<BufferedImage> res = new ArrayList<>();
        for(BufferedImage i : list) {
            res.add(scaleImageToFit(i, width, height));
        }
        return res;
    }

    private static ArrayList<BufferedImage> getSmoothSeriesRandomized(BufferedImage image, BufferedImage image1, float step) {

        boolean filled[][] = new boolean[image1.getWidth()][image1.getHeight()];
        int toFill = image1.getHeight()*image1.getWidth();
        ArrayList<BufferedImage> res = new ArrayList<>();
        res.add(image);
        int pixelsPerStep = (int) Math.max((int) toFill * step, (int) 2);
        while(toFill>0) {
            System.out.println("To Fill = " + toFill);
            int currFilling = Math.min(pixelsPerStep, toFill);
            res.add(fillPixels(image, image1,filled,  currFilling));
            toFill-=currFilling;
        }
        return res;
    }
    public static class Pair{
        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int x;
        public int y;
    }
    private static BufferedImage fillPixels(BufferedImage temp, BufferedImage image, boolean filled[][], int howMany) {
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        /* It's cache optimal iteration */

        ArrayList<Pair> empty = new ArrayList<>();
        for(int i = 0; i < filled.length; ++i) {
            for(int j = 0; j < filled[i].length; ++j) {
                if(!filled[i][j]) {
                    empty.add(new Pair(i, j));
                }
            }
        }

        Collections.shuffle(empty);

        for(int i = 0; i < howMany; ++i) {
            filled[empty.get(i).x][empty.get(i).y] = true;
        }

        for(int j = 0; j < res.getHeight(); ++j) {
            for(int i = 0; i < res.getWidth(); ++i) {
                res.setRGB(i, j, filled[i][j] ? image.getRGB(i, j) : temp.getRGB(i, j));
            }
        }
        return res;
    }


}