package com.poproject.app;

import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.lang.Integer.max;


public class MainController{

    /* FXML objects derived from form.fxml*/
    @FXML public Label setDelayLabel;
    @FXML public MenuItem gifmodeSingleImage;
    @FXML public MenuItem gifmodeMultiImage;
    @FXML public MenuItem gifmodeSimpleGif;
    @FXML public MenuItem gifWarpRotate;
    @FXML public MenuItem gifWarpFunnyRotate;
    @FXML public MenuItem gifWarpExpand;
    @FXML public MenuItem gifWarpId;
    @FXML public MenuItem gifMorphismSmBlurr;
    @FXML public MenuItem gifMorphismSqrMean;
    @FXML public MenuItem gifMorphismLinMean;
    @FXML public CheckBox loopCheckBox;
    @FXML public Button createButton;
    @FXML private MenuButton chooseMorphism;
    @FXML private MenuButton chooseGIFmode;
    @FXML private MenuButton chooseWarp;
    @FXML private ToggleButton hideImagesButton;
    @FXML private AnchorPane rootAnchorPane;
    @FXML private CheckBox repeatBox;
    @FXML private ImageView mainImageView;
    @FXML private ScrollPane downsideScrollPane;
    @FXML public TextField delayField;
    @FXML private ColorPicker colorTransparent;
    @FXML private CheckBox transparentBox;
    @FXML private Menu menuEdit;
    @FXML private MenuItem menuClearAll;
    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuOpenFromURL;
    @FXML private MenuItem menuSaveAs;
    @FXML private MenuItem menuExit;
    @FXML private MenuItem menuPreview;
    @FXML public MenuItem menuHelpAbout;
    @FXML private HBox segmentView;
    @FXML private TextField gifWidthField;
    @FXML private TextField gifHeightField;
    /*-------------------------2-------------------------
     *          global variables used in Controller      */

    //for animation
    private TranslateTransition translateTransition;
    private boolean animationCounter = true;

    //preview loaded images
    List<ImageView> allImagesDisplay = new LinkedList<>();

    //working with images and GIF
    private ArrayList<BufferedImage> loadedImages = new ArrayList<>();
    private BufferedImage mainImage = null;
    private Integer gifHeight = 480;
    private ImageView dragObj = null;
    private Integer gifWidth = 360;

    //pairing tasks, thread and stages
    private List<TaskThreadAndBar> tasksAndThreads = new LinkedList<>();

    //gif modes
    private CreationMode currentMode = CreationMode.SimpleGif;
    private GifWarp curGifWarp = GifWarp.Id;
    private GifMorphism curGifMorphism = GifMorphism.LinearMean;

    //handling open/save operations
    private final FileChooser openImageFileChooser = new FileChooser();
    private final FileChooser saveImageFileChooser = new FileChooser();

    public File about;
    /*-------------------end of block-------------------
    -------------------------2-------------------------*/

    /*----------------------2.1-------------------------
     *          initializer and constructor       */
    @FXML
    public void initialize(){
        defineImagesAnimation();
        setFileChooserExtensions();
    }
    public MainController(){ }
    /*-------------------end of block-------------------
    ------------------------2.1-------------------------*/

    /*-------------------------3-------------------------
     *                  animation methods                   */
    public void hideImagesOnAction(ActionEvent actionEvent) {
        defineImagesAnimation();
        if(animationCounter){
            hideImagesButton.setText("Show images");
            downsideScrollPane.setTranslateY(downsideScrollPane.getHeight());
        }else{
            hideImagesButton.setText("Hide images");
            downsideScrollPane.setTranslateY(0);
        }
        animationCounter = !animationCounter;
        rootAnchorPane.requestFocus();
        translateTransition.playFromStart();
    }
    public void scrollPaneOnScroll(ScrollEvent event) {
        if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
            downsideScrollPane.setHvalue(downsideScrollPane.getHvalue() - event.getDeltaY() / segmentView.getWidth());
        }
    }
    @FXML
    void albumDragReleased(MouseDragEvent event) {
        System.out.println("drag RELEASED");
    }
    @FXML
    void albumDragDone(DragEvent event) {
        System.out.println("drag Done");
    }
    @FXML
    void albumDragDropped(DragEvent event) {
        System.out.println("drag dropped");
    }
    @FXML
    void albumDragExited(DragEvent event) {
        System.out.println("drag exit");
    }
    @FXML
    void albumDragStart(MouseEvent event) {
        System.out.println("drag start");
    }
    @FXML
    void albumDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
        System.out.println("drag over");
    }
    @FXML
    void globalDragExit(MouseEvent event){
        System.out.println("global exit");
        downsideScrollPane.setPannable  (true);
        downsideScrollPane.removeEventFilter(ScrollEvent.ANY, EventHandlers.getScrollLock());
        rootAnchorPane.setCursor(Cursor.DEFAULT);
    }
    /*-------------------end of block-------------------
    -------------------------3-------------------------*/


    /*-------------------------4-------------------------
     *        bunch of methods to help to create GIF          */
    private WritableImage writeToMainView(BufferedImage bufferedImage){
        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pw = writableImage.getPixelWriter();
        for (int x = 0; x < writableImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        mainImageView.setImage(writableImage);
        mainImage = bufferedImage;
        return writableImage;
    }



    private void openFile(File file, BufferedImage bi){
        try{
            BufferedImage bufferedImage;
            if(file != null)
                bufferedImage = ImageIO.read(file);
            else
                bufferedImage = bi;

            if(bufferedImage != null) {
                loadedImages.add(bufferedImage);
                WritableImage writableImage = writeToMainView(bufferedImage);
                addImage(mainImageView);
                ImageView iv =  new ImageView(writableImage);
                iv.setPreserveRatio(true);
                iv.setFitHeight(160);
                iv.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        System.out.println("drag detacted");
                        iv.startFullDrag();

                        dragObj = iv;
                        //Dragboard d = iv.startDragAndDrop(TransferMode.MOVE);
                        //d.setDragView(iv.getImage());
                        ClipboardContent c = new ClipboardContent();
                        //c.put(new DataFormat(), iv);
                        //d.setContent(c);

                        rootAnchorPane.setCursor(new ImageCursor(iv.getImage()));
                        event.consume();
                        downsideScrollPane.setPannable(false);
                        downsideScrollPane.addEventFilter(ScrollEvent.ANY, EventHandlers.getScrollLock());
                    }
                });
                iv.setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                });
                iv.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
                    @Override
                    public void handle(MouseDragEvent event) {
                        System.out.println("drop");

                        if(dragObj != null){
                            int posSource = segmentView.getChildren().indexOf(dragObj);
                            int posTarget = segmentView.getChildren().indexOf(iv);
                            segmentView.getChildren().remove(dragObj);
                            segmentView.getChildren().add(posTarget, dragObj);
                            BufferedImage tmp = loadedImages.remove(posSource);
                            loadedImages.add(posTarget, tmp);

                            dragObj = null;
                        }

                    }
                });
                iv.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        int posTarget = segmentView.getChildren().indexOf(iv);
                        if(event.getButton() == MouseButton.SECONDARY){
                            //System.out.println("mouse clicked " + event.getButton());

                            segmentView.getChildren().remove(iv);
                            loadedImages.remove(posTarget);
                            if(loadedImages.size() > posTarget){
                                writeToMainView(loadedImages.get(posTarget));
                            }
                            else if (posTarget - 1 >= 0 && loadedImages.size() > 0){
                                writeToMainView(loadedImages.get(posTarget - 1));
                            }
                            else if (loadedImages.size() == 0){
                                mainImageView.setImage(null);
                                mainImage = null;
                            }
                        }
                        if(event.getButton() == MouseButton.PRIMARY){
                            writeToMainView(loadedImages.get(posTarget));
                        }
                    }
                });
                segmentView.getChildren().add(iv);
                iv.setOnContextMenuRequested(e -> new ContextMenu().show(iv, e.getScreenX(), e.getScreenY()));
                segmentView.setOnContextMenuRequested(e -> new ContextMenu().show(segmentView,100,200));
                //wyświetl
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("AN ERROR OCCURED");
            alert.showAndWait();
        }
    }
    private void addImage(ImageView image){
        allImagesDisplay.add(image);
        //do smth
    }
    /*-------------------end of block-------------------
    -------------------------4-------------------------*/

    /*-------------------------5-------------------------
     *   bunch of methods to handle interacting with menu
         e.g. "Open File", "Clear all", etc.             */
    public void menuOpenOnAction(ActionEvent actionEvent) {
        try {
            List<File> files = openImageFileChooser.showOpenMultipleDialog(MainApp.mainWindow);
            if(files != null){
                openImageFileChooser.setInitialDirectory(files.get(0).getParentFile());
            }
            for (File file : files) {
                if (file != null) {
                    openFile(file, null);
                }
            }
        }catch(Exception e){
            //do nothing, probably user did missclick or smth.like that
        }
    }

    public void menuOpenFromURLonAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setResizable(true);
        dialog.setTitle("Open image from URL");
        dialog.setContentText("Enter URL here:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(source -> {
            try {
                Image i = new Image(dialog.getResult());
                BufferedImage bi = SwingFXUtils.fromFXImage(i, null);
                openFile(null, bi);
            }catch(Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("OOPS... something went wrong");
                alert.setContentText("Are you sure there was no errors in\"" + dialog.getResult() + "\"?");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
            }
        });
    }

    public void menuSaveAsOnAction(ActionEvent actionEvent) {
        try {
            File fileToSave = saveImageFileChooser.showSaveDialog(MainApp.mainWindow);
            saveImageFileChooser.setInitialDirectory(fileToSave.getParentFile());
        }catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("AN ERROR OCCURED");
            alert.showAndWait();
        }
    }

    public void menuClearAllOnAction(ActionEvent actionEvent) {
        allImagesDisplay.clear();
        loadedImages.clear();
        segmentView.getChildren().removeIf(e -> true);
        mainImageView.setImage(new Image("com/poproject/app/logo.png"));
    }
    public void menuPreviewOnAction(ActionEvent actionEvent) {
        try {
            File file = new File("preview.gif");
            ArrayList<BufferedImage> list = new ArrayList<>();
            BufferedImage image;
            GifferTask task = GifTasks.GenerateGifSaver(generateGifCreator(), getEncodeOptsFromGUI(), file);
            Task task2 = new Task() {
                @Override
                protected Object call() throws Exception {
                    task.run();
                    //Encode.preview();
                    return null;
                }
            };
            task2.setOnSucceeded(v -> {
                System.out.println("preview ok");
                Encode.preview();
            });
            Stage progressStage = new Stage();
            ProgressBar progressBar = new ProgressBar(-1);
            progressBar.getStylesheets().add("css/mainTheme.css");
            progressStage.initModality(Modality.WINDOW_MODAL);
            progressStage.titleProperty().bind(task.messageProperty());
            progressStage.setWidth(300);
            progressStage.setHeight(130);
            progressStage.setScene(new Scene(new StackPane(progressBar), 200, 80));
            //progressBar.getStylesheets().add(getClass().getResource("css/mainTheme.css").toExternalForm());
            progressBar.progressProperty().bind(task.progressProperty());

            Thread th = new Thread(task2);
            TaskThreadAndBar TB = new TaskThreadAndBar(task, progressBar, progressStage, file, th, false);
            TB.setFunctions();
            tasksAndThreads.add(TB);
            th.setDaemon(true);
            th.start();
        }catch(ListNotLongEnough l){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(l.getMessage());
            alert.showAndWait();
        }

    }
    public void menuExitOnAction(ActionEvent actionEvent) {
        //TODO=kill all threads and exit
        System.exit(0);
    }

    public void menuHelpAboutOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About 'giffer'");
        alert.setHeaderText("All you need is... know how it works");
        String textAbout = "Panes:\n" +
                "Main Image Panel:\n" +
                "    Main image used to create Single Image Gifs\n" +
                "Consecutive image panel:\n" +
                "    Image panel used to create Multiple Image Gifs\n" +
                "\n" +
                "You can move pictures on bottom panel by drag and drop.  Right click drops picture from loaded pictures, left sets image as Main Image\n" +
                "\n" +
                "Buttons:\n" +
                "Set Delay: Sets the delay between consecutive frames\n" +
                "Transparent: Drops background color\n" +
                "Color: Choose the color of background\n" +
                "\n" +
                "GifHeight: Choose the height of gif\n" +
                "GifWidth: Choose width of gif\n" +
                "Repeat: Make gif run forever\n" +
                "Loop: Gif goes back and forth\n" +
                "Choose Gif Mode:\n" +
                "    Single Image: Creates gif based on warp, uses selected image\n" +
                "    Multiple Image: Creates gif based on sequence on bottom panel, using warp, and morph, to intertwine between images\n" +
                "\n" +
                "Choose Warp:\n" +
                "    Id: Identity warp\n" +
                "    Rotate: Rotate Image by 360 degrees, works best with images with uniformly coloured boundary\n" +
                "    FunnyRotate: Makes a spiral\n" +
                "    Expand: Moves image towards\n" +
                "Choose Morphism:\n" +
                "    Linear Mean: Takes linear mean of rgb colors\n" +
                "    Square Mean: Takes square mean of rgb colors\n" +
                "    Blurr Mean: Randomly changes pixels from one image to another\n" +
                "\n" +
                "Gif Creation:\n" +
                "    CREATE:\n" +
                "        Creates and saves image\n" +
                "    Preview:\n" +
                "        Creates and displays image in popup\n";
        /*try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/about/About.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            textAbout = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        TextArea textArea = new TextArea(textAbout);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
    /*-------------------end of block-------------------
     *-------------------------5-------------------------*/

    /*-------------------------6-------------------------
     * bunch of methods to handle interacting with TextField */
    public void delayFieldKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            rootAnchorPane.requestFocus();
        }
    }
    public void gifWidthKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            rootAnchorPane.requestFocus();
        }
    }
    public void gifHeightKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            rootAnchorPane.requestFocus();
        }
    }

    public void textFieldMouseExited(MouseDragEvent mouseDragEvent) {
        rootAnchorPane.requestFocus();
    }

    public void textFieldDragExited(DragEvent dragEvent) {
        rootAnchorPane.requestFocus();
    }
    /*-------------------end of block-------------------
     *-------------------------6-------------------------*/


    /*-------------------------7-------------------------
     * Class TT&B to pair current running task and the pop-up window "creating..."*/
    class TaskThreadAndBar {
        //holding task which we're paired to
        //in case of closing our bar/stage we'll cancel task too
        //TODO = now doesn't work properly
        GifferTask task;
        ProgressBar progressBar;
        Stage progressStage;
        String fileToCreate;
        Thread thread;
        boolean showAlertOnSuccess = false;

        private TaskThreadAndBar(GifferTask task, ProgressBar progressBar, Stage s, File f, Thread t, boolean printOnSuccess) {
            this.task = task;
            this.progressBar = progressBar;
            this.progressStage = s;
            this.fileToCreate = f.getName();
            this.thread = t;
            this.showAlertOnSuccess = printOnSuccess;
        }
        void setFunctions(){
            progressStage.setOnCloseRequest(e ->{
                task.cancel();
                progressStage.close();
            });
            task.setOnScheduled(e -> {
                progressStage.show();
            });
                task.setOnSucceeded(e -> {
                    progressStage.hide();
                    if(showAlertOnSuccess) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Information");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText(fileToCreate + " created successfully!");
                        successAlert.showAndWait();
                    }
            });
        }
    }
    /*-------------------end of block-------------------
     *-------------------------7-------------------------*/


    /*-------------------------8-------------------------
     *     working with GIF via GifCreator class and GUI       */
    public class ListNotLongEnough extends Exception {
        public ListNotLongEnough(String message){
            super(message);
        }
    }

    private GifCreator generateGifCreator() throws ListNotLongEnough{


        GifCreator gifCreator = new GifCreator();

        if(loadedImages.size() < 2 && currentMode == CreationMode.MultipleImageGif ) {
            throw new ListNotLongEnough("You don't have enough images to choose that mode");
        }

        try {
            gifHeight = new Integer(gifHeightField.getCharacters().toString());
        } catch (Exception e) {
            gifHeight = 380;
        }
        try {
            gifWidth = new Integer(gifWidthField.getCharacters().toString());
        } catch (Exception e) {
            gifWidth = 200;
        }

        switch (curGifWarp) {
            case Rotate:
                gifCreator.warper = WarperFactory.rotate(gifHeight, gifWidth, 2*Math.PI);
                break;
            case FunnyRotate:
                gifCreator.warper = WarperFactory.funnyRotate(gifHeight, gifWidth, 5);
                break;
            case Expand:
                gifCreator.warper = WarperFactory.expand(gifHeight, gifWidth, 2);
                break;
            case Id:
                gifCreator.warper = WarperFactory.id();
                break;
        }

        switch (curGifMorphism) {
            case BlurrMean:
                gifCreator.meanImage = MeanImageFactory.blurrMeanImage();
                break;
            case LinearMean:
                gifCreator.meanImage = MeanImageFactory.linearMeanImage();
                break;
            case SquareMean:
                gifCreator.meanImage = MeanImageFactory.sqrtMeanImage();

        }

        gifCreator.list = MeanImageFactory.normalize(loadedImages, gifHeight, gifWidth);
        gifCreator.singleImage= MeanImageFactory.scaleImageToFit(mainImage, gifHeight, gifWidth);
        gifCreator.creationMode = currentMode;
        gifCreator.step = (float) 0.02;
        gifCreator.loop = loopCheckBox.isSelected();
        return gifCreator;
    }

    private EncodeOptions getEncodeOptsFromGUI(){
        EncodeOptions opts = new EncodeOptions();
        Integer delayInt;
        try {
            delayInt = new Integer(delayField.getCharacters().toString());
        } catch (Exception e) {
            delayInt = 100;
        }
        opts.delay = max(delayInt, 10);
        opts.color = colorTransparent.getValue();
        if(!transparentBox.isSelected()) {
            opts.color = null;
        }
        opts.repeat = repeatBox.isSelected();
        return opts;
    }
    /*-------------------end of block-------------------
     *-------------------------8-------------------------*/

    /*-------------------------9-------------------------*/
    /* Generates a Giffer task and binds it to progress bar */
    public void createButtonOnAction(ActionEvent actionEvent) {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new ExtensionFilter("GIF files", "*.gif", "*.GIF"));

            File file = fc.showSaveDialog(MainApp.mainWindow);
            GifferTask task = GifTasks.GenerateGifSaver(generateGifCreator(), getEncodeOptsFromGUI(), file);

            Stage progressStage = new Stage();
            ProgressBar progressBar = new ProgressBar(-1);
            progressBar.getStylesheets().add("css/mainTheme.css");
            progressStage.initModality(Modality.WINDOW_MODAL);
            progressStage.titleProperty().bind(task.messageProperty());
            progressStage.setWidth(300);
            progressStage.setHeight(130);
            progressStage.setScene(new Scene(new StackPane(progressBar), 200, 80));
            //progressBar.getStylesheets().add(getClass().getResource("css/mainTheme.css").toExternalForm());
            progressBar.progressProperty().bind(task.progressProperty());

            Thread th = new Thread(task);
            TaskThreadAndBar TB = new TaskThreadAndBar(task, progressBar, progressStage, file, th, true);
            TB.setFunctions();
            tasksAndThreads.add(TB);
            th.setDaemon(true);
            th.start();
        }catch(ListNotLongEnough l){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(l.getMessage());
            alert.showAndWait();
        }
    }
    /*-------------------end of block-------------------
    -------------------------9-------------------------*/


    /*  ------------------------10-------------------------
    Methods which deal with choosing one of menu options e.g. GIF encoding */
    public void gifMorphismSmBlurrOnAction(ActionEvent actionEvent) {
        chooseMorphism.setText("Blurr Mean");
        curGifMorphism = GifMorphism.BlurrMean;
    }

    public void gifMorphismSqrMeanOnAction(ActionEvent actionEvent) {
        chooseMorphism.setText("Square mean");
        curGifMorphism = GifMorphism.SquareMean;
    }
    public void gifMorphismLinMeanOnAction(ActionEvent actionEvent) {
        chooseMorphism.setText("Linear Mean");
        curGifMorphism = GifMorphism.LinearMean;
    }
    public void gifWarpFunnyRotateOnAction(ActionEvent actionEvent) {
        chooseWarp.setText("Funny rotate");
        curGifWarp = GifWarp.FunnyRotate;
    }
    public void gifWarpRotateOnAction(ActionEvent actionEvent) {
        chooseWarp.setText("Rotate");
        curGifWarp = GifWarp.Rotate;
    }
    public void gifWarpExpandOnAction(ActionEvent actionEvent) {
        chooseWarp.setText("Expand");
        curGifWarp = GifWarp.Expand;
    }
    public void gifWarpIdOnAction(ActionEvent actionEvent) {
        chooseWarp.setText("Identity");
        curGifWarp = GifWarp.Id;
    }
    public void gifmodeMultiImageOnAction(ActionEvent actionEvent) {
        chooseGIFmode.setText("Multi-image GIF");
        currentMode = CreationMode.MultipleImageGif;
    }
    public void gifmodeSingleImageOnAction(ActionEvent actionEvent) {
        chooseGIFmode.setText("Single-image GIF");
        currentMode = CreationMode.SingleImageGif;
    }
    public void gifmodeSimpleGifOnAction(ActionEvent actionEvent) {
        chooseGIFmode.setText("Simple Gif");
        currentMode = CreationMode.SimpleGif;
    }
    /*-------------------end of block-------------------
    -------------------------10-------------------------*/

    /*-------------------------11-------------------------
     *         methods to be called from initializer       */
    void defineImagesAnimation() {
        translateTransition = new TranslateTransition();
        translateTransition.setNode(downsideScrollPane);
        translateTransition.toYProperty().bind(downsideScrollPane.translateYProperty());
        translateTransition.setDuration(new Duration(1000));
    }
    @Deprecated /*possibly*/
    void setMenusProperties(){
        menuOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        menuSaveAs.setAccelerator(KeyCombination.keyCombination("Ctrl + S"));
        menuOpenFromURL.setAccelerator(KeyCombination.keyCombination("Ctrl + U"));
        menuExit.setAccelerator(KeyCombination.keyCombination("Ctrl + X"));
    }
    private void setFileChooserExtensions() {
        openImageFileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new ExtensionFilter("All Files", "*.*"));
    }
    /*-------------------end of block-------------------
    -------------------------11-------------------------*/
}
