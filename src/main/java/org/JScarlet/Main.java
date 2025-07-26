package org.JScarlet;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.hc.core5.util.TextUtils;

import java.io.*;


public class Main extends Application {
    @FXML
    private Button btn_filePick,btn_audioParse,btn_outPut;
    @FXML
    private Text tv_filePath;
    @FXML
    private TextArea ta_resultContent;

    String filePath = null;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("GPT 音訊檔解析轉文字工具");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        System.out.println("App started");
            try {
                launch(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    @FXML
    private void onClick_filePick(){
        Stage stage = (Stage)btn_filePick.getScene().getWindow();
        FilePicker picker = new FilePicker();
        picker.pickAudioFile(stage);
    }

    @FXML
    public void audioParse() throws InterruptedException, IOException {
        if(!TextUtils.isEmpty(filePath)){
            AI_Poster poster = new AI_Poster(filePath);
            ta_resultContent.setText("音訊檔案   分析中");
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    System.out.println(poster.getOraginalResult());
                    poster.AudioTranscriber();
                    return poster.getFinalResult();
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    ta_resultContent.setText("\n分析結果：\n" + getValue());
                    String tidiedContent = poster.tidyTheContent(poster.getFinalResult());
                    ta_resultContent.appendText("\n整理結果：\n" + tidiedContent);
                }

                @Override
                protected void failed() {
                    super.failed();
                    ta_resultContent.setText("錯誤：" + getException().getMessage());
                }
            };

            // 開啟背景執行緒
            new Thread(task).start();

        }

    }

    public class FilePicker {
        public void pickAudioFile(Stage stage) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇音訊檔案");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                filePath = selectedFile.getAbsolutePath();  // ✅ 絕對路徑
                tv_filePath.setText(filePath);
                // 你可以將此 path 傳給 Whisper API 或做後續處理
            } else {
                tv_filePath.setText("未成功選取檔案");
            }
        }
    }

    private String binaryReader(String path) throws IOException {
        FileInputStream fis = null;
        // 原始音訊檔案
        File inputFile = new File(path); // 可支援 MP3, WAV 等格式
        // 輸出的 .bin 檔案
        File outputFile = new File("C:\\path\\to\\your\\output.bin");

        try {
            fis = new FileInputStream(inputFile);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return fis.toString();
    }

}


