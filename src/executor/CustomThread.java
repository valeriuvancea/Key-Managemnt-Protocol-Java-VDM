package executor;

public class CustomThread extends Thread {
    CustomThread(Runnable threadFunction) {
        super(threadFunction);
    }

    public void waitToFinish() {
        try {
            this.join();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
