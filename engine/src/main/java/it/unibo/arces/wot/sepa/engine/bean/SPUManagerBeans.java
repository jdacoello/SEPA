package it.unibo.arces.wot.sepa.engine.bean;

public class SPUManagerBeans {
	private static long requests = 0;

	private static float minTime = -1;
	private static float averageTime = -1;
	private static float maxTime = -1;
	private static float time = -1;

	private static long activeSPUs = 0;
	private static long maxActiveSPUs = 0;

	private static long subscribeRequests = 0;
	private static long unsubscribeRequests = 0;

	private static int SPUProcessingTimeout = 30000;

	private static long unitScale = 1000000;

	private static int subscribers = 0;

	private static long subscribers_max = 0;

	private static long filteringRequests = 0;
	
	private static long filteringTime;
	private static long filteringMinTime;
	private static long filteringMaxTime;
	private static long filteringAverageTime;
	
	public static void scale_ms() {
		unitScale = 1000000;
	}
	
	public static void scale_us() {
		unitScale = 1000;
	}
	
	public static void scale_ns() {
		unitScale = 1;
	}
	
	public static String getUnitScale() {
		if (unitScale == 1) return "ns";
		else if (unitScale == 1000) return "us";
		return "ms";
	}
	
	public static long getUpdateRequests() {
		return requests;
	}
	
	public synchronized static long getSPUs_current() {
		return activeSPUs;
	}

	public static long getSPUs_max() {
		return maxActiveSPUs;
	}
	
	public synchronized static  void setActiveSPUs(long n) {
		activeSPUs = n;
		if (activeSPUs > maxActiveSPUs) maxActiveSPUs = activeSPUs;
	}
	
	public static void subscribeRequest() {
		subscribeRequests++;
	}
	
	public static void unsubscribeRequest() {
		unsubscribeRequests++;
	}
	
	public synchronized static void timings(long start, long stop) {
		requests++;
		time = stop - start;

		if (minTime == -1)
			minTime = time;
		else if (time < minTime)
			minTime = time;

		if (maxTime == -1)
			maxTime = time;
		else if (time > maxTime)
			maxTime = time;

		if (averageTime == -1)
			averageTime = time;
		else
			averageTime = ((averageTime * (requests - 1)) + time) / requests;
	}

	public static void reset() {
		requests = 0;
		minTime = -1;
		averageTime = -1;
		maxTime = -1;
		time = -1;
		
		subscribeRequests = 0;
		unsubscribeRequests = 0;
		subscribers = 0;
		subscribers_max = 0;
		
	}

	public static float getSPUs_time() {
		return time/unitScale;
	}
	
	public static float getSPUs_time_min() {
		return minTime/unitScale;
	}

	public static float getSPUs_time_max() {
		return maxTime/unitScale;
	}

	public static float getSPUs_time_average() {
		return averageTime/unitScale;
	}

	public static long getSubscribeRequests() {
		return subscribeRequests;
	}

	public static long getUnsubscribeRequests() {
		return unsubscribeRequests;
	}

	public static int getSPUProcessingTimeout() {
		return SPUProcessingTimeout;
	}
	
	public static void setSPUProcessingTimeout(int t) {
		SPUProcessingTimeout = t;
	}

	public synchronized static void addSubscriber() {
		subscribers++;
		if (subscribers > subscribers_max) subscribers_max = subscribers;
	}
	
	public synchronized static void removeSubscriber() {
		subscribers--;
	}
	
	public synchronized static int getSubscribers() {
		return subscribers;
	}

	public static long getSubscribersMax() {
		return subscribers_max;
	}

	public static void filteringTimings(long start, long stop) {
		filteringRequests ++;
		filteringTime = stop - start;

		if (filteringMinTime == -1)
			filteringMinTime = filteringTime;
		else if (filteringTime < filteringMinTime)
			filteringMinTime = filteringTime;

		if (filteringMaxTime == -1)
			filteringMaxTime = filteringTime;
		else if (filteringTime > filteringMaxTime)
			filteringMaxTime = filteringTime;

		if (filteringAverageTime == -1)
			filteringAverageTime = filteringTime;
		else
			filteringAverageTime = ((filteringAverageTime * (filteringRequests - 1)) + filteringTime) / filteringRequests;	
	}
	
	public static float getFiltering_time() {
		return filteringTime/unitScale;
	}
	
	public static float getFiltering_time_min() {
		return filteringMinTime/unitScale;
	}

	public static float getFiltering_time_max() {
		return filteringMaxTime/unitScale;
	}

	public static float getFiltering_time_average() {
		return filteringAverageTime/unitScale;
	}
}
