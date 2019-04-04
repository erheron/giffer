package com.poproject.app;

import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.poproject.app.GifMode.*;

class GifTaskCancelledException extends Exception {

}


public class GifTasks {
    public static GifferTask GenerateGifSaver(GifCreator creator, EncodeOptions encodeOptions, File file) {

        return new GifferTask<Integer>() {
            private final GifCreator myCreator = creator;
            @Override
            protected Integer call() throws Exception {
                updateGifferMessage("Creating...");
                ArrayList<BufferedImage> list;
                try {
                    list = myCreator.generateFrames(this);
                } catch (GifTaskCancelledException e) {
                    System.out.println("Dowidzenia!");
                    return 1;
                }

                if (file != null) {
                    Encode.encodeGif(list, file, encodeOptions, this);
                }
                updateProgress(100, 100);
                return 0;
            }
            @Override
            protected void cancelled(){
                updateMessage("Cancelled");
                super.cancelled();

            }
        };
    }
}