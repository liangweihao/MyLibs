package javax.jmdns;

import android.content.Context;

import com.nothing.commonutils.utils.Lg;
import com.nothing.myserver.MyHttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 包含 mDNS 相关工具方法，如创建可被发现的服务和发现网络中的服务。
 * 该类提供了静态方法，方便进行 mDNS 服务的注册与发现操作。
 *
 * @author lwh
 * @since 2025/7/24
 */
public final class JmDnsUtils {
    private static final String TAG = "JmDnsUtils";
    public static final String DEFAULT_SERVICE_TYPE = "_http._tcp.local.";
    private static final long DEFAULT_DISCOVERY_TIMEOUT = 30; // 单位：秒
    public static final String SERVICE_NAME_SUFFIX = "-service"; // 单位：秒

    // 私有构造函数，防止实例化
    private JmDnsUtils() {
    }

    /**
     * 基于应用上下文创建可被发现的 mDNS 服务，自动生成可用随机端口。
     * <p>
     * 可执行线程：非 UI 线程。因为涉及网络操作，在 Android 等环境中，
     * 网络操作不能在 UI 线程执行，否则会抛出 NetworkOnMainThreadException。
     *
     * @param context
     * @param serviceName        服务名称
     * @param serviceDescription 服务描述
     * @return JmmDNS 实例，若创建失败则返回 null
     */
    public static JmmDNS createDiscoverableService(Context context, String serviceName, String serviceDescription) {
        InetAddress inetAddress = getLocalInetAddress();
        if (inetAddress == null) {
            return null;
        }

        JmmDNS jmmDNS = getJmmDNSInstance();
        if (jmmDNS == null) {
            return null;
        }

        int servicePort = findAvailablePort();
        if (servicePort == -1) {
            Lg.e(TAG, "Failed to find an available port.");
            return null;
        }

        try {
            ServiceInfo serviceInfo = ServiceInfo.create(
                    DEFAULT_SERVICE_TYPE,
                    serviceName,
                    servicePort,
                    serviceDescription
            );
            jmmDNS.registerService(serviceInfo);
            jmmDNS.start();
            new MyHttpServer(context, servicePort).start();
            Lg.i(TAG, "Service %s is now discoverable on port %d.", serviceName, servicePort);
            return jmmDNS;
        } catch (IOException e) {
            Lg.e(TAG, "Failed to register service: %s", e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * 查找一个可用的随机端口。
     *
     * @return 可用端口号，若未找到则返回 -1
     */
    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            Lg.e(TAG, "Failed to find available port: %s", e.getLocalizedMessage());
            return -1;
        }
    }

    /**
     * 基于应用上下文创建客户端，调用 createDiscoverableService 方法。
     * <p>
     * 可执行线程：非 UI 线程。因为内部调用了 createDiscoverableService 方法，涉及网络操作。
     *
     * @param context 应用上下文
     * @return JmmDNS 实例，若创建失败则返回 null
     */
    public static JmmDNS createContextClient(Context context) {
        String packageName = context.getPackageName();
        String serviceName = packageName + SERVICE_NAME_SUFFIX;
        String serviceDescription = "mDNS service for " + packageName;
        return createDiscoverableService(context,serviceName, serviceDescription);
    }

    /**
     * 创建一个 mDNS 服务，使其能被网络中的其他设备发现。
     * <p>
     * 可执行线程：非 UI 线程。原因同上，涉及网络操作。
     *
     * @param serviceName        服务名称
     * @param servicePort        服务端口
     * @param serviceDescription 服务描述
     * @return JmmDNS 实例，若创建失败则返回 null
     */
    @Nullable
    public static JmmDNS createDiscoverableService(
            String serviceName,
            int servicePort,
            String serviceDescription
    ) {
        InetAddress inetAddress = getLocalInetAddress();
        if (inetAddress == null) {
            return null;
        }


        JmmDNS jmmDNS = getJmmDNSInstance();
        if (jmmDNS == null) {
            return null;
        }

        try {
            ServiceInfo serviceInfo = ServiceInfo.create(
                    DEFAULT_SERVICE_TYPE,
                    serviceName,
                    servicePort,
                    serviceDescription
            );
            jmmDNS.registerServiceType(DEFAULT_SERVICE_TYPE);
            jmmDNS.registerService(serviceInfo);
            jmmDNS.start();
            Lg.i(TAG, "Service %s is now discoverable on the network.", serviceName);
            return jmmDNS;
        } catch (IOException e) {
            Lg.e(TAG, "Failed to register service: %s", e.getLocalizedMessage());
            return null;
        }
    }



    /**
     * 发现网络中指定类型的 mDNS 服务。
     * 使用默认的超时时间。
     * <p>
     * 可执行线程：非 UI 线程。该方法不仅涉及网络操作，还包含线程休眠操作，
     * 若在 UI 线程执行会阻塞 UI 界面，导致界面卡顿或无响应。
     *
     * @return
     */
    @Nullable
    public static JmmDNS discoverDefaultServices(@Nullable JmDnsDiscoveryCallback callback) {
        return discoverServices(DEFAULT_SERVICE_TYPE, callback);
    }

    /**
     * 发现服务的回调接口，用于自定义处理服务和网络事件。
     */
    public interface JmDnsDiscoveryCallback {
        /**
         * 当有新服务添加时调用。
         *
         * @param event 服务添加事件
         */
        void onServiceAdded(@NonNull ServiceEvent event);

        /**
         * 当服务被移除时调用。
         *
         * @param event 服务移除事件
         */
        void onServiceRemoved(@NonNull ServiceEvent event);

        /**
         * 当服务解析完成时调用。
         *
         * @param event 服务解析事件
         */
        void onServiceResolved(@NonNull ServiceEvent event);

        /**
         * 当网络地址添加时调用。
         *
         * @param event 网络地址添加事件
         */
        void onInetAddressAdded(@NonNull NetworkTopologyEvent event);

        /**
         * 当网络地址移除时调用。
         *
         * @param event 网络地址移除事件
         */
        void onInetAddressRemoved(@NonNull NetworkTopologyEvent event);
    }

    /**
     * 发现网络中指定类型的 mDNS 服务。
     * <p>
     * 可执行线程：非 UI 线程。理由同 discoverServices() 无参方法，
     * 涉及网络操作和线程休眠。
     *
     * @param serviceType 要发现的服务类型
     * @return
     */
    @Nullable
    public static JmmDNS discoverServices(
            String serviceType,
            @Nullable JmDnsDiscoveryCallback callback
    ) {
        JmmDNS jmmDNS = getJmmDNSInstance();
        if (jmmDNS == null) {
            return null;
        }

        try {
            jmmDNS.addServiceListener(
                    serviceType, new ServiceListener() {
                        @Override
                        public void serviceAdded(ServiceEvent event) {
                            if (callback != null) {
                                callback.onServiceAdded(event);
                            } else {
                                Lg.i(TAG, "Service added: %s", event.getName());
                            }
                            jmmDNS.requestServiceInfo(event.getType(), event.getName(), 2000);

                        }

                        @Override
                        public void serviceRemoved(ServiceEvent event) {
                            if (callback != null) {
                                callback.onServiceRemoved(event);
                            } else {
                                Lg.i(TAG, "Service removed: %s", event.getName());
                            }
                        }

                        @Override
                        public void serviceResolved(ServiceEvent event) {
                            if (callback != null) {
                                callback.onServiceResolved(event);
                            } else {
                                ServiceInfo info = event.getInfo();
                                Lg.i(TAG, "Service resolved: %s", info.getName());
                                Lg.i(TAG, "  Type: %s", info.getType());
                                Lg.i(TAG, "  Port: %d", info.getPort());
                                Lg.i(TAG, "  Host: %s", info.getHostAddresses()[0]);
                                Lg.i(TAG, "  Description: %s", info.getNiceTextString());
                            }
                        }
                    }
            );

            jmmDNS.addNetworkTopologyListener(new NetworkTopologyListener() {
                @Override
                public void inetAddressAdded(NetworkTopologyEvent event) {
                    if (callback != null) {
                        callback.onInetAddressAdded(event);
                    } else {
                        Lg.i(TAG, "Network address added: %s", event.getInetAddress());
                    }
                }

                @Override
                public void inetAddressRemoved(NetworkTopologyEvent event) {
                    if (callback != null) {
                        callback.onInetAddressRemoved(event);
                    } else {
                        Lg.i(TAG, "Network address removed: %s", event.getInetAddress());
                    }
                }
            });
            jmmDNS.start();
            Lg.i(TAG, "Service discovery started. Listening for %s services...", serviceType);
        } catch (Throwable e) {
            Lg.e(TAG, "Error during service discovery: %s", e.getLocalizedMessage());
        }
        return jmmDNS;
    }

    /**
     * 获取本地主机的 InetAddress 实例。
     * <p>
     * 可执行线程：非 UI 线程。因为获取本地主机信息可能涉及网络操作，
     * 在 Android 等环境中不允许在 UI 线程执行。
     *
     * @return 本地主机的 InetAddress 实例，若获取失败则返回 null
     */
    private static InetAddress getLocalInetAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            Lg.i(TAG, "Local Address: %s", inetAddress);
            return inetAddress;
        } catch (UnknownHostException e) {
            Lg.e(TAG, "Failed to get local host address: %s", e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * 获取 JmmDNS 实例。
     * <p>
     * 可执行线程：非 UI 线程。创建 JmmDNS 实例可能涉及网络操作，
     * 在 Android 等环境中不允许在 UI 线程执行。
     *
     * @return JmmDNS 实例，若获取失败则返回 null
     */
    private static JmmDNS getJmmDNSInstance() {
        try {
            return JmmDNS.Factory.newJmmDNS();
        } catch (Throwable e) {
            Lg.e(TAG, "Failed to create JmmDNS instance: %s", e.getLocalizedMessage());
            return null;
        }
    }
}
