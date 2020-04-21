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
    private Label srcDirectory;//JavaFX中的标签控件

    private Thread task;

    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化时，需要初始化数据库及表
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件//监听界面的变化，根据相应变化启动相应程序
        searchField.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }

    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();//JavaFX内的目录选择器
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();
        srcDirectory.setText(path);//srcDirectory是JavaFX的标签控件，用于显示path
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
        ObservableList<FileMeta> metas = fileTable.getItems();//getItems 获取表格的数据
        metas.clear();//获取到当前显示页面的数据后，清除全部，方便添加新的需要显示的数据
        String dir = srcDirectory.getText();
        if(dir != null && dir.trim().length() != 0){
            String content = searchField.getText();//获取搜索框内的内容
            //提供数据库的插叙方法
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