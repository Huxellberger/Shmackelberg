// package StackelbergAgent;

public final class RegressionData
{
    public final float sumLeader;
    public final float sumLeaderSquared;
    public final float sumFollower;
    public final float sumLeaderFollowerProduct;

    public RegressionData(float inSumLeader, float inSumLeaderSquared, float inSumFollower, float inSumLeaderFollowerProduct)
    {
	sumLeader = inSumLeader;
	sumLeaderSquared = inSumLeaderSquared;
	sumFollower = inSumFollower;
	sumLeaderFollowerProduct = inSumLeaderFollowerProduct;
    }
}
