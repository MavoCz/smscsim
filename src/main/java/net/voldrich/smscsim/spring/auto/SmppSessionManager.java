package net.voldrich.smscsim.spring.auto;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppServerSession;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages all RX and TRX bound SMPP connections represented by session instance.
 * These connections are kept in global list and separate list for each
 * distinct application id.
 * Class is thread safe.
 * When SMSCsim wants to send out a MO message, it requests a session by calling getNextServerSession. A session is
 * chosen based on round robin algorithm.
 **/
@Component
public class SmppSessionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SmppSessionManager.class);
	
	private final Map<String, SessionList> applicationSessionMap;
	
	private final AtomicInteger sequenceNumber = new AtomicInteger(0);
	
	private final SessionList globalSessionList = new SessionList();
	
	public SmppSessionManager() {
		applicationSessionMap = new ConcurrentHashMap<String, SessionList>();
	}
	
	public synchronized void addServerSession(SmppServerSession session) {		
		if (session.getBindType() == SmppBindType.TRANSMITTER) {
			// we ignore transmitters as they can not be used to send delivers
			return;
		}
		
		String systemId = session.getConfiguration().getSystemId();
		SessionList sessionList = applicationSessionMap.get(systemId);
		if (sessionList == null) {
			sessionList = new SessionList();
			applicationSessionMap.put(systemId, sessionList);
		}
		sessionList.add(session);
		globalSessionList.add(session);
	}
	
	public synchronized void removeServerSession(SmppServerSession session) {
		if (session.getBindType() == SmppBindType.TRANSMITTER) {
			// we ignore transmitters as they can not be used to send delivers
			return;
		}
		
		String systemId = session.getConfiguration().getSystemId();
		SessionList sessionList = applicationSessionMap.get(systemId);
		boolean removed = false;
		if (sessionList != null) {
			removed = sessionList.remove(session);
			globalSessionList.remove(session);
		}
		if (!removed) {
			logger.warn("Failed to remove session %s, Session not found.", session);
		}
	}
	
	public synchronized SmppServerSession getNextServerSession(String appSystemId) {		
		SessionList sessionList = applicationSessionMap.get(appSystemId);
		if (sessionList != null) {
			return sessionList.getNext();
		}
		return null;
	}
		
	public synchronized SmppServerSession getNextServerSession() {
		return globalSessionList.getNext();
	}
	
	public int getNextSequenceNumber() {
		return sequenceNumber.incrementAndGet();
	}

    /**
     * Internal session list implementing simple round robin using a cycle iterator.
     * This class is not thread safe.
     **/
	private final class SessionList extends ArrayList<SmppServerSession> {
		private static final long serialVersionUID = 1L;
		private Iterator<SmppServerSession> roundRobinIterator;
		
		@Override
		public boolean add(SmppServerSession e) {
			boolean ret = super.add(e);
			resetRoundRobinIterator();
			return ret;
		}
		
		@Override
		public boolean remove(Object o) {
			boolean ret = super.remove(o);
			resetRoundRobinIterator();
			return ret;			
		}
		
		private void resetRoundRobinIterator() {
			roundRobinIterator = Iterables.cycle(this).iterator();
		}
		
		public SmppServerSession getNext() {
			if (size() == 0) {
				return null;
			}
			if (roundRobinIterator.hasNext()) {
				return roundRobinIterator.next();
			}
			return null;
		}
		
	}
}
