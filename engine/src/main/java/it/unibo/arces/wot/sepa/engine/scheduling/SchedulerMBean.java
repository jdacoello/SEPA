package it.unibo.arces.wot.sepa.engine.scheduling;

public interface SchedulerMBean {
	public String getStatistics();

	public long getRequests_scheduled();
	
	public long getErrors();

	public long getRequests_pending();

	public long getRequests_max_pending();

	public long getRequests_rejected();

	public float getTimings_Update();

	public float getTimings_Query();

	public float getTimings_Subscribe();

	public float getTimings_Unsubscribe();

	public void reset();
}
