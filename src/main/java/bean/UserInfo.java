package bean;

/**
 * Created by THU73 on 17/7/18.
 */
public class UserInfo {
    private int failedCount;
    private int finishedCount;
    private int renderingCount;

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getFinishedCount() {
        return finishedCount;
    }

    public void setFinishedCount(int finishedCount) {
        this.finishedCount = finishedCount;
    }

    public int getRenderingCount() {
        return renderingCount;
    }

    public void setRenderingCount(int renderingCount) {
        this.renderingCount = renderingCount;
    }
}
