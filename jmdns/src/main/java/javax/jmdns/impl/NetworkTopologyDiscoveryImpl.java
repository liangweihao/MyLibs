/**
 *
 */
package javax.jmdns.impl;



import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.jmdns.NetworkTopologyDiscovery;

/**
 * This class implements NetworkTopologyDiscovery.
 *
 * @author Pierre Frisch
 */
public class NetworkTopologyDiscoveryImpl implements NetworkTopologyDiscovery {


    /**
     *
     */
    public NetworkTopologyDiscoveryImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.JmmDNS.NetworkTopologyDiscovery#getInetAddresses()
     */
    @Override
    public InetAddress[] getInetAddresses() {
        Set<InetAddress> result = new HashSet<InetAddress>();
        try {

            for (Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces(); nifs.hasMoreElements();) {
                NetworkInterface nif = nifs.nextElement();
                for (Enumeration<InetAddress> iaenum = nif.getInetAddresses(); iaenum.hasMoreElements();) {
                    InetAddress interfaceAddress = iaenum.nextElement();

                    if (useInetAddress(nif, interfaceAddress)) {
                        result.add(interfaceAddress);
                    }
                }
            }
        } catch (SocketException se) {

        }
        return result.toArray(new InetAddress[result.size()]);
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.NetworkTopologyDiscovery#lockInetAddress(java.net.InetAddress)
     */
    @Override
    public void lockInetAddress(InetAddress interfaceAddress) {
        // Default implementation does nothing.
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.NetworkTopologyDiscovery#unlockInetAddress(java.net.InetAddress)
     */
    @Override
    public void unlockInetAddress(InetAddress interfaceAddress) {
        // Default implementation does nothing.
    }

    /*
     * (non-Javadoc)
     * @see javax.jmdns.JmmDNS.NetworkTopologyDiscovery#useInetAddress(java.net.NetworkInterface, java.net.InetAddress)
     */
    @Override
    public boolean useInetAddress(NetworkInterface networkInterface, InetAddress interfaceAddress) {
        try {
            if (!networkInterface.isUp()) {
                return false;
            }
// TODO:LWH  2025/5/9 如果不注解 会导致 虚拟网络无法 组播扫描
//           if (!networkInterface.supportsMulticast()) {
//              return false;
//           }

            if (networkInterface.isLoopback()) {
                return false;
            }

            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
