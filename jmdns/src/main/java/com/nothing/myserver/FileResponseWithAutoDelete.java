package com.nothing.myserver;


import com.nothing.commonutils.utils.Lg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

// 自定义一个响应类，用于在响应结束后删除文件
class FileResponseWithAutoDelete extends NanoHTTPD.Response {
   private final File file;

   private static final String TAG = "FileResponseWithAutoDel";
   public FileResponseWithAutoDelete(Status status, String mimeType, InputStream data, long totalBytes, File file) {
      super(status, mimeType, data, totalBytes);
      this.file = file;
   }

   @Override
   public void close() {
       try {
           super.close();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
       // 在响应关闭时删除文件
      if (file.exists()) {
         boolean deleted = file.delete();
         if (deleted) {
            Lg.i(TAG, "Screenshot file deleted successfully: " + file.getAbsolutePath());
         } else {
            Lg.e(TAG, "Failed to delete screenshot file: " + file.getAbsolutePath());
         }
      }
   }
}
