package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @autuor 范大晨
 * @Date 2023/5/4 13:08
 */

public class MinioTest {
    static
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void uploadTest() throws Exception {
        ContentInfo mimeTypeMatch = ContentInfoUtil.findMimeTypeMatch(".mp4");
        String mimeType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        if (mimeTypeMatch != null) {
            mimeType = mimeTypeMatch.getMimeType();
        }

        UploadObjectArgs upload = UploadObjectArgs.builder()
                .filename("F:\\java资料\\1-java基础\\2-环境搭建\\test.mp4")//本地文件
                .bucket("testbucket")                             //存储桶的名称
                .object("/test/test001.mp4")                            //存储后文件名称
                .contentType(mimeType)                                  //文件类型
                .build();
        minioClient.uploadObject(upload);
    }

    @Test
    public void deleteTest() throws Exception {
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("/test/test001.mp4").build();
        minioClient.removeObject(remove);
    }

    @Test
    public void getTest() throws Exception {
        GetObjectArgs get = GetObjectArgs.builder().bucket("testbucket")
                .object("test001.mp4")
                .build();
        FilterInputStream fileInputStream = minioClient.getObject(get);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("E:\\workspace\\workspace6\\xczx\\test.mp4"));
        try {
            IOUtils.copy(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String local_md5 = DigestUtils.md5Hex(new FileInputStream(new File("F:\\java资料\\1-java基础\\2-环境搭建\\test.mp4")));
        String source_md5 = DigestUtils.md5Hex(new FileInputStream(new File("E:\\workspace\\workspace6\\xczx\\test.mp4")));
        if (!local_md5.equals(source_md5)) {
            System.out.println("下载失败");
        }
    }

    @Test
    public void uploadChunk() throws Exception {
        for (int i = 0; i <= 6; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("E:\\workspace\\workspace6\\xczx\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
        }
    }

    @Test
    public void mergeChunk() throws Exception {
//        List<ComposeSource> sources = new ArrayList<>();
//        for (int i = 0; i < 7; i++) {
//            ComposeSource source = ComposeSource.builder()
//                    .bucket("testbucket")
//                    .object("/chunk/" + i)
//                    .build();
//            sources.add(source);
//        }

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(7)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("/chunk/".concat(i.toString()))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .sources(sources)
                .object("merge01.mp4")
                .build();
        minioClient.composeObject(composeObjectArgs);
    }

    @Test
    public void deleteChunkTest() {
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(7)
                .map(i -> new DeleteObject("chunk/" + i))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket("testbucket")
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError=null;
            try {
                deleteError=r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


//        //合并分块完成将分块文件清除
//        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
//                .limit(7)
//                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
//                .collect(Collectors.toList());
//
//        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
//                .bucket("testbucket")
//                .objects(deleteObjects)
//                .build();
//
//        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
//
//        results.forEach(r->{
//            DeleteError deleteError = null;
//            try {
//                deleteError = r.get();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });

    }
}
