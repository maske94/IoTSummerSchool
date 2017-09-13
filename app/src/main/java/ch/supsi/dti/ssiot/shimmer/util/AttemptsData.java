package ch.supsi.dti.ssiot.shimmer.util;

public class AttemptsData {

    /**
     * The total number of attempts to do
     */
    private final int mTotalAttempts;

    /**
     * The current number of attempts
     */
    private int mAttempts;

    /**
     * Default constructor
     * @param totalAttempts
     */
    public AttemptsData(int totalAttempts) {
        this(totalAttempts, 1);
    }

    public AttemptsData(int totalAttempts, int attempts) {
        mTotalAttempts = totalAttempts;
        mAttempts = attempts;
    }

    public int getAttempts() {
        return mAttempts;
    }

    public int getTotalAttempts() {
        return mTotalAttempts;
    }

    /**
     *
     * @return
     */
    public boolean addAttempt(){
        return ++mAttempts > mTotalAttempts;
    }
}
