package it.unibo.arces.wot.sepa.engine.processing.subscriptions;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPANotExistsException;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.engine.bean.SPUManagerBeans;
import it.unibo.arces.wot.sepa.engine.dependability.Dependability;
import it.unibo.arces.wot.sepa.engine.scheduling.InternalSubscribeRequest;

public class Subscriptions {
	private static final Logger logger = LogManager.getLogger();

	// SID ==> Subscriber
	private static final HashMap<String, Subscriber> subscribers = new HashMap<String, Subscriber>();

	// SPUID ==> Subscribers
	private static final HashMap<String, HashSet<Subscriber>> handlers = new HashMap<String, HashSet<Subscriber>>();

	// Request ==> SPU
	private static final HashMap<InternalSubscribeRequest, SPU> requests = new HashMap<InternalSubscribeRequest, SPU>();

	public synchronized static boolean contains(InternalSubscribeRequest req) {
		return requests.containsKey(req);
	}

	public synchronized static void register(InternalSubscribeRequest req, SPU spu) {
		handlers.put(spu.getSPUID(), new HashSet<Subscriber>());
		requests.put(req, spu);
		
		SPUManagerBeans.setActiveSPUs(handlers.size());
		logger.debug("@subscribe SPU activated: " + spu.getSPUID() + " total (" + handlers.size() + ")");
	}
	
	public synchronized static SPU getSPU(InternalSubscribeRequest req) {
		return requests.get(req);
	}

	public synchronized static Subscriber addSubscriber(InternalSubscribeRequest req, SPU spu) {
		Subscriber sub = new Subscriber(spu, req.getEventHandler());
		handlers.get(spu.getSPUID()).add(sub);
		subscribers.put(sub.getSID(), sub);

		SPUManagerBeans.addSubscriber();
		Dependability.onSubscribe(sub.getGID(), sub.getSID());
		
		return sub;
	}
	
	public synchronized static Subscriber getSubscriber(String sid) {
		return subscribers.get(sid);
	}

	public synchronized static boolean removeSubscriber(Subscriber sub) throws SEPANotExistsException {
		String sid = sub.getSID();
		String spuid = sub.getSPU().getSPUID();
		
		if (!subscribers.containsKey(sid)) {
			logger.warn("@internalUnsubscribe SID not found: " + sid);
			throw new SEPANotExistsException("SID not found: " + sid);
		}
		
		logger.trace("@internalUnsubscribe SID: " + sid + " from SPU: " + spuid + " with active subscriptions: "
				+ subscribers.size());

		handlers.get(spuid).remove(sub);
		subscribers.remove(sid);

		// No more handlers: return true
		if (handlers.get(spuid).isEmpty()) {
			logger.debug("@internalUnsubscribe no more subscribers. Kill SPU: " + sub.getSPU().getSPUID());

			requests.remove(sub.getSPU().getSubscribe());
			handlers.remove(spuid);
			
			return true;
		}
		
		// More handlers
		return false;
	}
	
	public synchronized static void notifySubscribers(String spuid,Notification notify) {
		for (Subscriber client : handlers.get(spuid)) {
			try {
				// Dispatching events
				Notification event = new Notification(client.getSID(), notify.getARBindingsResults(),
						client.nextSequence());
				client.getHandler().notifyEvent(event);
			} catch (Exception e) {
				logger.error("@notifyEvent " + e.getMessage());
			}
		}
	}
	
	public synchronized static boolean isZombieSpu(String spuid) {
		if (handlers.get(spuid) == null) return true;
		if (handlers.get(spuid).isEmpty()) return true;
		
		InternalSubscribeRequest req = null;
		for (Subscriber client : handlers.get(spuid)) {
			if (client.getHandler() == null) {
				req = subscribers.get(client.getSID()).getSPU().getSubscribe();
				subscribers.remove(client.getSID());
				continue;
			}
			return false;
		}
		
		handlers.remove(spuid);
		requests.remove(req);
		
		return true;
	}
}
