package net.voldrich.smscsim.spring.auto;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

@Component
public class SmscThreadPool {
	
	private ThreadPoolExecutor executor;
	
	private ScheduledThreadPoolExecutor monitorExecutor;
	
	private int maxThreadCount = 10;	
	
	@PostConstruct
	public void init() throws Exception {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreadCount); //newCachedThreadPool();
        
    	// for monitoring thread use, it's preferable to create your own instance
        // of an executor and cast it to a ThreadPoolExecutor from Executors.newCachedThreadPool()
        // this permits exposing things like executor.getActiveCount() via JMX possible
        // no point renaming the threads in a factory since underlying Netty 
        // framework does not easily allow you to customize your thread names
        monitorExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });  
	}
 
	@PreDestroy
	public void cleanUp() throws Exception {
	  System.out.println("Spring Container is destroy! Customer clean up");
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public ScheduledThreadPoolExecutor getMonitorExecutor() {
		return monitorExecutor;
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

}
