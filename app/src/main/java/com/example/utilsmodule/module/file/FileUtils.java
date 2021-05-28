package com.example.utilsmodule.module.file;

import android.os.Environment;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * file utils tools
 *
 * @Date : create at 2021/5/13 15:07 by TaoLing
 */
public class FileUtils {

    /**
     * copy a file or folder to new path
     *
     * @param oldPath old path
     * @param newPath new path
     * @param ignoreFileNameList {@link ArrayList<String>} 忽略拷贝的文件名集合，若所有文件都需拷贝则传入null
     */
    public static void copyFileToNewPath(String oldPath, String newPath, @Nullable ArrayList<String> ignoreFileNameList){
        if (ignoreFileNameList == null){
            ignoreFileNameList = new ArrayList<>();
        }

        //判断oldPath中的文件是否存在，不存在则退出
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            return;
        }

        //创建拷贝oldPath中文件的新文件夹
        File dirNew = new File(newPath);
        dirNew.mkdirs();

        //判断newPath是否为oldPath的子文件夹
        if (newPath.length() >= oldPath.length()){
            String pathParent = newPath.substring(0,oldPath.length());
            String pathChild = newPath.substring(oldPath.length());
            if (pathParent.equals(oldPath)){
                String[] dirNames = pathChild.split("/");
                ignoreFileNameList.add(dirNames[0]);
            }
        }

        //采用递归的方式拷贝文件或者文件夹，因此如果拷贝文件的新文件夹是旧文件夹的父文件夹，则需要忽略新文件的拷贝
        File[] files = oldFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (ignoreFileNameList != null){
                if (!ignoreFileNameList.contains(name)) {
                    directory(files[i].getAbsolutePath(), newPath);
                }
            }
        }
    }

    /**
     * 递归复制
     *
     *  @param oldPath {@link String} old path
     * @param newPath {@link String} new path
     */
    private static void directory(String oldPath, String newPath) {
        File dirSource = new File(oldPath);
        if (!dirSource.isDirectory()) {
            return;
        }
        File destDir = new File(newPath + File.separator + dirSource.getName() + File.separator);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        File[] files = dirSource.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fromFile = oldPath + File.separator + files[i].getName();
            String toFile = destDir.getAbsolutePath() + File.separator + files[i].getName();
            if (files[i].isDirectory()) {
                directory(fromFile, toFile);
            }
            if (files[i].isFile()) {
                copyFile(fromFile, toFile);
            }
        }
    }

    /**
     * 文件写入
     *
     *  @param oldPath {@link String} old path
     * @param newPath {@link String} new path
     */
    private static void copyFile(String oldPath, String newPath) {
        int hasRead = 0;
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(oldFile);
                FileOutputStream fos = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((hasRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, hasRead);
                }
                fis.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * save log to external SdCard
     *
     * @param content the data to save
     */
    public static void saveLog(String content){
        try {
            String assembleContent = getCurTime() + ":" + content + "\r\n";
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Log";
            File logFile = new File(path);
            if (!logFile.exists()){
                logFile.mkdirs();
            }
            String name = path + File.separator + getCurDate() + ".txt";
            RandomAccessFile randomFile = new RandomAccessFile(name, "rw");
            long length = randomFile.length();
            randomFile.seek(length);
            randomFile.write(assembleContent.getBytes());
//            randomFile.writeBytes(getCurTime() + ":" + content + "\r\n");
            randomFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get current date
     *
     * @return current date
     */
    public static String getCurDate(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());
        return date;
    }

    /**
     * get current date and time
     *
     * @return current date and time
     */
    public static String getCurTime(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = formatter.format(new Date());
        return time;
    }
}
