package executor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomThread {
    private Object threadResult = null;
    private Runnable functionToBeCalled;
    private Thread thread;

    CustomThread(Consumer<Consumer<Object>> threadFunction) {
        functionToBeCalled = () -> threadFunction.accept(result -> {
            threadResult = result;
        });
    }

    public void start() {
        thread = new Thread(functionToBeCalled);
        thread.start();
    }

    public CustomThread then(Function<Object, Object> function) {
        return getThenFunction(saveResultFunction -> saveResultFunction.accept(function.apply(threadResult)));
    }

    public CustomThread then(Consumer<Object> function) {
        return getThenFunction(saveResultFunction -> function.accept(threadResult));
    }

    public CustomThread then(Runnable function) {
        return getThenFunction(saveResultFunction -> function.run());
    }

    public CustomThread then(Supplier<Object> function) {
        return getThenFunction(saveResultFunction -> saveResultFunction.accept(function.get()));
    }

    private CustomThread getThenFunction(Consumer<Consumer<Object>> function) {
        CustomThread threadToReturn = new CustomThread((saveResultFunction) -> {
            try {
                thread.join();
                function.accept(saveResultFunction);
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
        threadToReturn.start();
        return threadToReturn;

    }

    public Object getReturnValue() {
        try {
            thread.join();
            return threadResult;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    public void waitToFinish() {
        try {
            thread.join();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
