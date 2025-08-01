package com.nothing.dnsservice.server;

import com.nothing.dnsservice.server.bean.AppDataResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


public interface IServerAPI {
    @GET("file_data")
    public Call<AppDataResponse> getPrivateStorageList(@Query("rootPath") String rootPath);
    @GET("file_sdcard")
    public Call<AppDataResponse> getExternalStorageList(@Query("rootPath") String rootPath);

    /**
     * 根据文件路径下载文件
     * @param filePath 要下载的文件的路径
     * @return 包含文件内容的响应体
     */
    @Streaming
    @GET("download_file")
    public Call<ResponseBody> downloadFile(@Query("filePath") String filePath);


}
