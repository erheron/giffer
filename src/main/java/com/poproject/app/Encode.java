package com.poproject.app;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by me on 09.05.18.
 */
public class Encode {
    public static void preview() {
        System.out.println(System.getProperty("user.dir"));
        ImageView iv = new ImageView();
        FlowPane fp = new FlowPane();
        Image im = new Image("file:" + System.getProperty("user.dir") + "/preview.gif");
        iv.setImage(im);
        fp.getChildren().addAll(iv);
        fp.setAlignment(Pos.TOP_CENTER);
        Scene scene = new Scene(fp, im.getWidth(), im.getHeight());
        Stage stage = new Stage();
        stage.setTitle("Preview");
        stage.setScene(scene);
        stage.show();


    }

    public static void encodeGif(Collection<BufferedImage> l, File file, EncodeOptions opts, GifferTask task) {
        //System.out.println(l.size());

        if(task!= null) {
            task.updateGifferProgress(0, l.size());
            task.updateGifferMessage("Saving... ");

        }
        try {
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            if(opts.repeat)
                encoder.setRepeat(0);
            if(opts.color != null)
                encoder.setTransparent(new java.awt.Color((float)opts.color.getRed(), (float)opts.color.getGreen(), (float)opts.color.getBlue(), (float)opts.color.getOpacity()));
            encoder.start(new FileOutputStream(file));
            encoder.setDelay(opts.delay);
            int currFrameNr = 0;
            for (BufferedImage i : l) {
                currFrameNr++;
                if(task!= null && task.isCancelled()) return;
                if(task!= null) task.updateGifferProgress(currFrameNr, l.size());
                encoder.addFrame(i);
            }
            encoder.finish();
        } catch (Exception e) {
        }
    }
}
