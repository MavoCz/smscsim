package net.voldrich.smscsim.server;

import com.cloudhopper.smpp.util.DaemonExecutors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mavo on 8.1.2015.
 */
public class SmscServerThreadPoolFactory {

    public ThreadPoolExecutor createMainExecutor() {
        return (ThreadPoolExecutor) DaemonExecutors.newCachedDaemonThreadPool();
    }
    
    public ScheduledThreadPoolExecutor createMonitorExecutor() {
        return (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });
    }
}
