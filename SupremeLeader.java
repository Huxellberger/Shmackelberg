import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.Record;
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
	public static void main(String[] args)
	{
	    try
	    { 
		if (args.length != 2)
		{
		    System.out.println("Wrong input arguments! Please submit window size and forgetting factor state");
		    return;
		}
	
	    	new SupremeLeader(Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]));
	    }
	    catch(RemoteException | NotBoundException e)
	    {
	    	System.out.println("Couldn't even start " + e);
	    }	
	}    

        private static final float FORGETTING_FACTOR = 0.99f;
        public static final float UNIT_COST = 1.0f;

        private Record[] m_records;
        private int m_windowSize = 100;
        private boolean m_shouldForget = false; 

        public SupremeLeader(int inWindowSize, boolean inShouldForget)
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Supreme Leader");

		m_windowSize = inWindowSize;
		m_shouldForget = inShouldForget;

		m_records = new Record[m_windowSize];	
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
	    float a = getA(data, m_windowSize);
	    float b = getB(data, m_windowSize);
	    return getOptimalLeaderCost(a, b);
	}

        private void updatePriorRecords(final int currentDay)
        {
	    int iteratedDay = currentDay - 1;

	    for (int i = m_windowSize - 1; i >= 0; i--)
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
	    
	    int scalingFactorIndex = 1;
	    for (Record record : m_records)
	    {
		float currentScalingFactor = 1.0f;
		if (m_shouldForget)
		{
		    currentScalingFactor = scalingFactorIndex * (float)Math.pow(FORGETTING_FACTOR, (scalingFactorIndex - 1));
		}	

		sumLeaderSquared += record.m_leaderPrice * record.m_leaderPrice * currentScalingFactor;
		sumFollower += record.m_followerPrice * currentScalingFactor;
		sumLeader += record.m_leaderPrice * currentScalingFactor;
		sumLeaderFollowerProduct += record.m_leaderPrice * record.m_followerPrice * currentScalingFactor;

		scalingFactorIndex++;
	    }

	    return new RegressionData(sumLeader, sumLeaderSquared, sumFollower, sumLeaderFollowerProduct);
       }

        // Exposed for testing
        public static float getA(RegressionData inData, int windowSize)
        {
	    return ((inData.sumLeaderSquared * inData.sumFollower) - (inData.sumLeader * inData.sumLeaderFollowerProduct)) / 
		((windowSize * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader));
        }

        // Exposed for testing
        public static float getB(RegressionData inData, int windowSize)
        {
	    return ((windowSize * inData.sumLeaderFollowerProduct) - (inData.sumLeader * inData.sumFollower)) / 
		((windowSize * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader));
        }

        // Exposed for testing
        public static float getOptimalLeaderCost(final float a, final float b)
        {
	    return (2 + UNIT_COST + (0.3f * a)) / (2 - (0.3f * b));
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
