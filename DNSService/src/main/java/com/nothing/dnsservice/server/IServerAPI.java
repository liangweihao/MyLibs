package com.nothing.dnsservice.server;

import com.nothing.dnsservice.server.bean.AppDataResponse;
import com.nothing.dnsservice.server.bean.DeviceInfoResponse;
import com.nothing.dnsservice.server.bean.InstalledAppsResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


public interface IServerAPI {
    @GET("file_data")
    public Call<AppDataResponse> getApplicationPrivateStorageList(@Query("rootPath") String rootPath);

    @GET("file_external_data")
    public Call<AppDataResponse> getApplicationExternalStorageList(@Query("rootPath") String rootPath);

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

    /**
     * 请求服务器进行截图并下载截图文件
     * @return 包含截图文件内容的响应体
     */
    @Streaming
    @GET("screenshot")
    Call<ResponseBody> getScreenshot();


    /**
     * 获取设备信息
     * @return 包含设备信息的响应
     */
    @GET("device_info")
    Call<DeviceInfoResponse> getDeviceInfo();

    /**
     * 获取已安装 APP
     * @return
     */
    @GET("installed_apps")
    Call<InstalledAppsResponse> getInstallApps();


    /**
     * 根据 APK 文件路径下载 APK
     * @param apkPath 要下载的 APK 文件的路径
     * @return 包含 APK 文件内容的响应体
     */
    @Streaming
    @GET("download_apk")
    Call<ResponseBody> downloadApk(@Query("apkPath") String apkPath);



}
