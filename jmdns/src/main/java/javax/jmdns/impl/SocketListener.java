// Copyright 2003-2005 Arthur van Hoff, Rick Blair
// Licensed under Apache License version 2.0
// Original license LGPL

package javax.jmdns.impl;


import com.nothing.commonutils.utils.Lg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;

import javax.jmdns.impl.constants.DNSConstants;

/**
 * Listen for multicast packets.
 */
class SocketListener extends Thread {

    /**
     *
     */
    private final JmDNSImpl _jmDNSImpl;

    /**
     * @param jmDNSImpl
     */
    SocketListener(JmDNSImpl jmDNSImpl) {
        super("SocketListener(" + (jmDNSImpl != null ? jmDNSImpl.getName() : "") + ")");
        this.setDaemon(true);
        this._jmDNSImpl = jmDNSImpl;

    }

    private void sleepThread() {
        if (_jmDNSImpl._threadSleepDurationMs > 0) {
            try {
                // sleep a small amount of time in case the network is overloaded with mdns packets (some devices do this),
                // in order to allow other threads to get some cpu time
                Thread.sleep(_jmDNSImpl._threadSleepDurationMs);
            } catch (InterruptedException e) {
                Lg.w(this.getName() , ".run() interrupted ", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final String TAG = "SocketListener";
    @Override
    public void run() {
        try {
            byte buf[] = new byte[DNSConstants.MAX_MSG_ABSOLUTE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (!this._jmDNSImpl.isCanceling() && !this._jmDNSImpl.isCanceled()) {
                sleepThread();
                packet.setLength(buf.length);
                this._jmDNSImpl.getSocket().receive(packet);
                if (this._jmDNSImpl.isCanceling() || this._jmDNSImpl.isCanceled() || this._jmDNSImpl.isClosing() || this._jmDNSImpl.isClosed()) {
                    break;
                }
                try {
                    if (this._jmDNSImpl.getLocalHost().shouldIgnorePacket(packet)) {
                        continue;
                    }

                    DNSIncoming msg = new DNSIncoming(packet);
                    if (msg.isValidResponseCode()) {

                        for (DNSRecord answer : msg._answers) {
                            if (answer.getName().equals("_nvstream._tcp.local.")) {
                                if (packet.getAddress() instanceof Inet4Address) {
                                    Lg.d(TAG,
                                            "JmDNS Response: %s  %s ",
                                            packet.getSocketAddress(),
                                            answer.getServiceInfo().getTextString()
                                    );
                                }
                            }

                        }
//                        for (DNSQuestion question : msg._questions) {
////                            Lg.d(TAG,"JmDNS Response : (%s:%s)(%s) %s", packet.getAddress(), packet.getPort(),this.getName(), msg.print(false));
//                            Lg.d(TAG,"JmDNS Response %s,%s " , question.getName(),question.getKey());
//                        }
                        if (msg.isQuery()) {
                            if (packet.getPort() != DNSConstants.MDNS_PORT) {
                                this._jmDNSImpl.handleQuery(msg, packet.getAddress(), packet.getPort());
                            }
                            this._jmDNSImpl.handleQuery(msg, this._jmDNSImpl.getGroup(), DNSConstants.MDNS_PORT);
                        } else {
                            this._jmDNSImpl.handleResponse(msg);
                        }
                    } else {
                        Lg.d(TAG,"{}.run() JmDNS in message with error code: {}", this.getName(), msg.print(true));
                    }
                } catch (IOException e) {
                    Lg.w(TAG,".run() exception ", e);
                }
            }
        } catch (IOException e) {
            if (!this._jmDNSImpl.isCanceling() && !this._jmDNSImpl.isCanceled() && !this._jmDNSImpl.isClosing() && !this._jmDNSImpl.isClosed()) {
                this._jmDNSImpl.recover();
            }
        }
    }

    public JmDNSImpl getDns() {
        return _jmDNSImpl;
    }


}
