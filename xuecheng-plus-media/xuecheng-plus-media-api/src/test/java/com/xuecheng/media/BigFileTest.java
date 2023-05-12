package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * @autuor 范大晨
 * @Date 2023/5/7 19:36
 */
public class BigFileTest {
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("E:\\workspace\\workspace6\\xczx\\upload\\1.mp4");
        String chunkPath = "E:\\workspace\\workspace6\\xczx\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }

        int chunkSize = 1024 * 1024 * 5;
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            int len = -1;
            File file = new File(chunkPath + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                while ((len = raf_read.read(bytes)) != -1) {
                    raf_write.write(bytes, 0, len);
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("分块完成" + i);
            }
        }
        raf_read.close();
    }

    @Test
    public void mergeChunk() throws IOException {
        //源文件
        File sourceFile = new File("E:\\workspace\\workspace6\\xczx\\upload\\1.mp4");
        //分块文件目录
        File chunkFolder = new File("E:\\workspace\\workspace6\\xczx\\chunk\\");
        //合并后文件
        File mergeFile = new File("E:\\workspace\\workspace6\\xczx\\upload\\2.mp4");

        //得到目录中所有文件
        File[] files = chunkFolder.listFiles();
        //将数组转换为集合
        assert files != null;
        List<File> list = Arrays.asList(files);
        //进行排序 (使用Collection工具类)
        Collections.sort(list, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));

        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        byte[] bytes = new byte[1024];
        //进行合并
        for (File file : list) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();

        String sourceMd5Hex = DigestUtils.md5Hex(Files.newInputStream(sourceFile.toPath()));
        String mergeMd5Hex = DigestUtils.md5Hex(Files.newInputStream(mergeFile.toPath()));
        if (sourceMd5Hex.equals(mergeMd5Hex)) {
            System.out.println("合并成功");
        }else {
            System.out.println("合并失败");
        }
    }

}
