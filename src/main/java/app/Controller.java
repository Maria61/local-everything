package app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import task.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    private Thread task;

    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化时，需要初始化数据库及表
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件
        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }

    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();
        // TODO
        srcDirectory.setText(path);
        //选择了目录后，需要对该目录进行扫描
        if(task != null){
            task.interrupt();
        }
        task = new Thread(new Runnable() {
            @Override
            public void run() {
                ScanCallback callback = new FileSave();
                FileScanner fileScanner = new FileScanner(callback);

                try {
                    System.out.println("执行文件扫描任务");
                    fileScanner.scan(path);//为提高效率，多线程执行扫描任务
                    //等待文件扫描任务执行完毕
                    fileScanner.waitFinish();
                    System.out.println("扫描完成，刷新表格");
                    //刷新表格,将扫描的结果显示在表格内
                    freshTable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        task.start();
    }

    // 刷新表格数据
    private void freshTable(){
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        // TODO

        String dir = srcDirectory.getText();
        if(dir != null && dir.trim().length() != 0){
            String content = searchField.getText();
            //TODO:提供数据库的插叙方法
            List<FileMeta> fileMetas = FileSearch.search(dir,content);
            metas.addAll(fileMetas);
        }
    }

//    private class ScannerThread {
//
//
//        public void shutdown(){
//            this.interrupt();
//            fileScanner.shutdown();
//        }
//    }


}