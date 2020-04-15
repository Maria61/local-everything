package task;

import util.DBUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 1.初始化数据库
 * 2.并读取sql文件
 * 3.执行sql语句来初始化表
 */
public class DBInit {

    public static String[] readSQL(){
        //通过getClassLoader()
        try {
            InputStream is = DBInit.class.getClassLoader()
                    .getResourceAsStream("init.sql");//❓？？这些方法都是什么意思？

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is,"UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                if(line.contains("--")){
                    line = line.substring(0,line.indexOf("--"));
                }
                sb.append(line);
            }
            String[] sqls = sb.toString().split(";");
            return sqls;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取数据库文件错误",e);
        }
    }


    public static void init(){
        //数据库jdbc操作：sql语句的执行
        Connection connection = null;
        Statement statement = null;
        try {
            //1.加载驱动，创建数据库连接
            connection = DBUtil.getConnection();
            //2.创建sql语句执行对象Statement
            statement = connection.createStatement();
            String[] sqls = readSQL();
            for(String s : sqls){
                //3.执行sql语句
                statement.executeUpdate(s);
            }
            //4.如果是查询操作，获取结果集ResultSet,处理结果集
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("初始化数据库操作失败",e);
        }finally{
            //5.释放资源
            DBUtil.close(connection,statement);
        }


    }

//    public static void main(String[] args) {
//        String[] sql = readSQL();
//        for(String s : sql){
//            System.out.println(s);
//        }
//        init();
//    }
}
