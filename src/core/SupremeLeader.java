// package StackelbergAgent;

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
	    RegressionData data = getDataForPreviousDays();
	    float a = getA(data);
	    float b = getB(data);
	    return getOptimalLeaderCost(a, b);
	}

        private void updatePriorRecords(final int currentDay)
        {
	    int iteratedDay = currentDay - 1;

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

       private RegressionData getDataForPreviousDays()
       {
	    float sumLeaderSquared = 0.0f;
	    float sumFollower = 0.0f;
	    float sumLeader = 0.0f;
	    float sumLeaderFollowerProduct = 0.0f;
	    
	    int scalingFactorIndex = WINDOW_SIZE - 1;
	    for (Record record : m_records)
	    {
		float currentScalingFactor = scalingFactorIndex * (float)Math.pow(FORGETTING_FACTOR, scalingFactorIndex);

		sumLeaderSquared += record.m_leaderPrice * record.m_leaderPrice * currentScalingFactor;
		sumFollower += record.m_followerPrice * currentScalingFactor;
		sumLeader += record.m_leaderPrice * currentScalingFactor;
		sumLeaderFollowerProduct += record.m_leaderPrice * record.m_followerPrice * currentScalingFactor;

		scalingFactorIndex--;
	    }

	    return new RegressionData(sumLeader, sumLeaderSquared, sumFollower, sumLeaderFollowerProduct);
       }

        private float getA(RegressionData inData)
        {
	    return ((inData.sumLeaderSquared * inData.sumFollower) - (inData.sumLeader * inData.sumLeaderFollowerProduct)) / 
		((WINDOW_SIZE * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader));
        }

        private float getB(RegressionData inData)
        {
	    return ((WINDOW_SIZE * inData.sumLeaderFollowerProduct) - (inData.sumLeader * inData.sumFollower)) / 
		((WINDOW_SIZE * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader));
        }

        private float getOptimalLeaderCost(final float a, final float b)
        {
	    return (2 - UNIT_COST + (0.3f * a)) / (2 - (0.3f * b));
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
