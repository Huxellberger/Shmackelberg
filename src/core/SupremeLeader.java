package StackelbergAgent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Moving Window approach to leader-follower imperfect information game
 * @author Group 19
 */
final class SupremeLeader
	extends PlayerImpl
{
        private static final int WINDOW_SIZE = 30;
        private static final float FORGETTING_FACTOR = 0.95f;

        private static final float UNIT_COST = 1.0f;
        

        private Record[] m_records;

	public SupremeLeader()
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Supreme Leader");
		m_records = new Record[WINDOW_SIZE];	
	}

	@Override
	public void goodbye()
		throws RemoteException
	{
		ExitTask.exit(500);
	}

	/**
	 * To inform this instance to proceed to a new simulation day
	 * @param p_date The date of the new day
	 * @throws RemoteException
	 */
	@Override
	public void proceedNewDay(int p_date)
		throws RemoteException
	{
		m_platformStub.publishPrice(m_type, genPrice(p_date));
	}

	/**
	 * Generate a price using the moving window approach
	 * 
	 * @param the new day to simulate
	 * @return The generated price
	 */
	private float genPrice(final int currentDay)
	{
	    updatePriorRecords(currentDay);
	    return (float) currentDay;
	}

        private void updatePriorRecords(final int currentDay)
        {
	    int iteratedDay = currentDay;

	    for (int i = WINDOW_SIZE - 1; i >= 0; i--)
	    {
		m_records[i] = getRecordForDay(iteratedDay);
		iteratedDay--;
	    }
        }

        private Record getRecordForDay(int queryDate)
        {
	    try
	    {
		 return m_platformStub.query(m_type, queryDate);
	    }
	    catch(RemoteException e)
	    {
		e.printStackTrace();
		return null;
	    }
	}

        private float getDailyProfit(final float leaderPrice, final float followerPrice)
        {
	    return (leaderPrice - UNIT_COST) * getDemandModelResult(leaderPrice, followerPrice);
        }

        private float getDemandModelResult(final float leaderPrice, final float followerPrice)
        {
	    return 2 - leaderPrice + (0.3f * followerPrice);
        }

	/**
	 * The task used to automatically exit the leader process
	 * @author Group 19
	 */
	private static class ExitTask
		extends TimerTask
	{
		static void exit(final long p_delay)
		{
			(new Timer()).schedule(new ExitTask(), p_delay);
		}
		
		@Override
		public void run()
		{
			System.exit(0);
		}
	}
}
