package task;

import app.FileMeta;
import util.DBUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSave implements task.ScanCallback {
    @Override
    public void callback(File dir) {
        File[] children = dir.listFiles();
        List<FileMeta> locals = new ArrayList<>();
        if(children != null) {
            for (File child : children) {
                locals.add(new FileMeta(child));
            }
        }

        List<FileMeta> metas = query(dir);

        //遍历数据库中的文件信息，如果本地没有，则删除数据库中的相应数据
        for(FileMeta meta : metas){
            if(!locals.contains(meta)){
                delete(meta);
            }
        }

        //遍历本地文件信息，如果数据库没有，则插入
        for(FileMeta meta : locals){
            if(!metas.contains(meta)){
                save(meta);
            }
        }


    }


    /**
     * 删除数据库有本地没有的数据
     * @param meta
     */

    public void delete(FileMeta meta){
        Connection connection = null;
        PreparedStatement ps = null;
        try{
            connection = DBUtil.getConnection();
            String sql = "delete from file_meta where" +
                    " (name=? and path=? and is_directory=?)";
            if(meta.getDirectory()){
                sql += " or path=? or path like ?";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());
            ps.setBoolean(3,meta.getDirectory());
            if(meta.getDirectory()){
                ps.setString(4,meta.getPath()+File.separator+meta.getName());
                ps.setString(5,meta.getPath()+File.separator+meta.getName()+File.separator);
            }
            ps.executeUpdate();

        }catch (Exception e){
            throw new RuntimeException("删除文件信息出错",e);
        }finally{
            DBUtil.close(connection,ps);
        }
    }


    /**
     * 查询数据库操作
     * @param dir
     * @return
     */
    private List<FileMeta> query(File dir){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> metas = new ArrayList<>();
        try{
            connection = DBUtil.getConnection();
            String sql = "select name, path, is_directory, size, last_modified" +
                    " from file_meta where path = ?";
             ps = connection.prepareStatement(sql);//预编译
             ps.setString(1,dir.getPath());
             rs = ps.executeQuery();
             while(rs.next()){
                 String name = rs.getString("name");
                 String path = rs.getString("path");
                 Boolean isDirectory = rs.getBoolean("is_directory");
                 Long size = rs.getLong("size");
                 Timestamp lastModified = rs.getTimestamp("last_modified");
                 FileMeta meta = new FileMeta(name, path, isDirectory, size,
                         new Date(lastModified.getTime()));
                 System.out.println("查询操作的结果："+meta);
                 metas.add(meta);
             }
            return metas;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("查询文件信息出错",e);
        }finally{
            DBUtil.close(connection,ps,rs);
        }

    }


    /**
     * 将扫描结果存入数据库
     * @param meta
     */
    private void save(FileMeta meta){
//        System.out.println(file.getPath());
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            //1.加载驱动，建立数据库连接
            connection = DBUtil.getConnection();
            //2.创建sql语句执行对象
            String sql = "insert into file_meta" +
                    " (name,path,is_directory,size,last_modified,pinyin,pinyin_first)" +
                    " values (?,?,?,?,?,?,?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1,meta.getName());
            statement.setString(2,meta.getPath());
            statement.setBoolean(3,meta.getDirectory());
            statement.setLong(4,meta.getSize());
            statement.setString(5,meta.getLastModifiedText());//上次的修改时间
//            String pinyin = null;
//            String pinyin_first = null;
//            //文件名中包含汉字才有拼音及拼音首字母
//            if(PinyinUtil.containsChinese(meta.getName())){
//                String[] pinyins = PinyinUtil.get(meta.getName());
//                pinyin = pinyins[0];
//                pinyin_first = pinyins[1];
//            }
            statement.setString(6,meta.getPinyin());
            statement.setString(7,meta.getPinyinFirst());
//            System.out.println("保存操作sql:"+sql);
            //3.执行sql
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("文件保存失败,"+e);
        }finally{
            //5.关闭结果集
            DBUtil.close(connection,statement);
        }
    }

    public static void main(String[] args) {
//        DBInit.init();
//        File file = new File("E:\\me\\1.jpg");
//        FileSave fileSave = new FileSave();
//        fileSave.save(file);
//        fileSave.query(file.getParentFile());

        List<FileMeta> locals = new ArrayList<>();
        locals.add(new FileMeta("新建文件夹",
                "F:\\Java学习bt\\项目-everything-like",
                true, 0L, new Date()));
        locals.add(new FileMeta("中华人名共和国",
                "F:\\Java学习bt\\项目-everything-like",
                true, 0L, new Date()));
        locals.add(new FileMeta("阿凡达.txt",
                "F:\\Java学习bt\\项目-everything-like\\中华人民共和国",
                false, 0L, new Date()));

        List<FileMeta> mates = new ArrayList<>();
        mates.add(new FileMeta("新建文件夹",
                "F:\\Java学习bt\\项目-everything-like",
                true, 0L, new Date()));
        mates.add(new FileMeta("中华人名共和国2",
                "F:\\Java学习bt\\项目-everything-like",
                true, 0L, new Date()));
        mates.add(new FileMeta("阿凡达.txt",
                "F:\\Java学习bt\\项目-everything-like\\中华人名共和国2",
                true, 0L, new Date()));


        for(FileMeta meta : locals){
            if(!mates.contains(meta)){
                System.out.println(meta);
            }
        }
    }
}
