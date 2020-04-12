package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import task.DBInit;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {


    private static volatile DataSource DATA_SOURCE;

    /**
     * 提供获取数据库连接池的功能：
     * 使用单例模式（多线程安全版本）
     *
     * @return
     */
    private static DataSource getDataSource(){
        if(DATA_SOURCE == null){
            synchronized (DBUtil.class){
                if(DATA_SOURCE == null){
                    SQLiteConfig config =  new SQLiteConfig();
                    config.setDateStringFormat(util.Util.DATE_PATTERN);
                    DATA_SOURCE = new SQLiteDataSource(config);
                    ((SQLiteDataSource)DATA_SOURCE).setUrl(getUrl());
                }
            }
        }
        return DATA_SOURCE;
    }

    /**
     * 获取sqlite数据库文件url的方法
     * @return
     */
    //获取target编译文件夹的路径
    //通过classLoader.getResource()/classLoader.getResourceAsStream()这样的方法
    private static String getUrl(){
        //获取target编译文件夹的路径
        URL classUrl = DBInit.class.getClassLoader().getResource("./");
        String dir = new File(classUrl.getPath()).getParent();
        String url = "jdbc:sqlite://" + dir + File.separator + "local-everything.db";
        System.out.println("获取数据库文件的路径："+ url);
        return url;
    }
    /**
     * 提供获取数据库连接的方法
     * 从数据库连接池DataSource.getConnection()来获取数据库连接
     * @return
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }


    public static void close(Connection connection, Statement statement) {
        close(connection,statement,null);
    }
    /**
     * 释放数据库资源
     * @param connection 数据库连接
     * @param statement sql语句执行对象
     * @param  resultSet 查询语句的结果集
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if(connection != null){
                connection.close();
            }
            if(statement != null){
                statement.close();
            }
            if(resultSet != null){
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("关闭数据库资源失败",e);
        }
    }
}
