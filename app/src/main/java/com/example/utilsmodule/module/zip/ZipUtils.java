package com.example.utilsmodule.module.zip;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * zip utils tools
 *
 * @Date : create at 2021/5/13 14:49 by TaoLing
 */
public class ZipUtils {

    /**
     * compressed file
     *
     * @param srcFilePath {@link String} Pending compressed file path
     * @param outPutZipFilePath {@link String} output compressed file path
     * @throws Exception
     */
    public static void ZipFolder(String srcFilePath, String outPutZipFilePath) throws Exception {
        //创建ZIP
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(outPutZipFilePath));
        //创建文件
        File file = new File(srcFilePath);
        //压缩
        ZipFiles(file.getParent(), file.getName(), outZip);
        //完成和关闭
        outZip.finish();
        outZip.close();

    }

    /**
     * recursive compression file or folder
     *
     * @param fileParentPath {@link String} parent folder path for files to be compressed.
     * @param fileName {@link String} the name of compressed file
     * @param zipOutputStream {@link ZipOutputStream}
     * @throws Exception
     */
    private static void ZipFiles(String fileParentPath, String fileName, ZipOutputStream zipOutputStream) throws Exception{
        if (zipOutputStream == null) {
            return;
        }

        File file = new File(fileParentPath + File.separator + fileName);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, len);
            }
            zipOutputStream.closeEntry();
        } else {
            //文件夹
            String fileList[] = file.list();
            //没有子文件和压缩
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileName + File.separator);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.closeEntry();
            }
            //子文件和递归
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(fileParentPath + File.separator + fileName + "/", fileList[i], zipOutputStream);
            }
        }
    }
}
