import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SupremeLeaderTests  
{
    @Test
    public void getA_CalculatesExpectedValueForRegressionData()
    {
	RegressionData inData = new RegressionData(10.0f, 2.0f, 3.0f, 4.0f, 5.0f);
	final int windowSize = 10;

	assertEquals
	(
	    ((inData.sumLeaderSquared * inData.sumFollower) - (inData.sumLeader * inData.sumLeaderFollowerProduct)) 
	    / ((windowSize * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader)), 
	    SupremeLeader.getA(inData)
	);
    }

    @Test
    public void getB_CalculatesExpectedValueForRegressionData()
    {
	RegressionData data = new RegressionData(10.0f, 3.0f, 3.0f, 4.0f, 5.0f);
	final int windowSize = 10;

	assertEquals
	(
	    ((windowSize * inData.sumLeaderFollowerProduct) - (inData.sumLeader * inData.sumFollower)) / 
		((windowSize * inData.sumLeaderSquared) - (inData.sumLeader * inData.sumLeader)), 
	    SupremeLeader.getB(inData)
	);
    }

    @Test
    public void getOptimalLeaderCost_CalculatesExpectedValueForAAndB()
    {
        final float a = 10.0f;
	final float b = 2.0f;

	assertEquals
	(
	    (2 + UNIT_COST + (0.3f * a)) / (2 - (0.3f * b)), 
	    SupremeLeader.getOptimalLeaderCost(a, b)
	);
    }
}
