package executor;

import java.util.Arrays;
import java.util.function.Consumer;

import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.VoidReturnValue;
import executor.CustomThread;

import vdm.RemoteController;

public abstract class Executor {
    public enum ExecutorType {
        JAVA, VDM
    }

    protected ExecutorType executorType = ExecutorType.VDM;
    private String vdmVariableName = "";

    public Executor() {
        this.executorType = ExecutorType.JAVA;
    }

    public Value initialize(Value vdmVariableName) {
        this.executorType = ExecutorType.VDM;
        String variableNameWithoutQuotes = vdmVariableName.toString().substring(1,
                vdmVariableName.toString().length() - 1); // Remove the quotes added by VDM
        this.vdmVariableName = variableNameWithoutQuotes;
        execute("init");
        return new VoidReturnValue();
    }

    abstract public Value init();

    public CustomThread execute(String functionName, Object... arguments) {
        return execute(functionName, x -> {
        }, arguments);
    }

    public synchronized CustomThread execute(String functionName, Consumer<Object> func, Object... arguments) {
        Runnable threadFunction;
        if (executorType == ExecutorType.JAVA) {
            threadFunction = () -> {
                try {
                    func.accept(this.getClass()
                            .getDeclaredMethod(functionName,
                                    (Arrays.stream(arguments).map(object -> object.getClass()).toArray(Class[]::new)))
                            .invoke(this, arguments));
                } catch (Exception exception) {
                    func.accept(null);
                }
            };

        } else {
            threadFunction = () -> {
                String stringArguments = (String) Arrays.stream(arguments).reduce("",
                        (accumulator, value) -> accumulator.toString() + "\"" + value.toString() + "\",");
                if (!stringArguments.isEmpty()) {
                    stringArguments = stringArguments.substring(0, stringArguments.length() - 1);
                }
                try {
                    Thread.sleep(1000);
                    func.accept(RemoteController.interpreter
                            .execute(vdmVariableName + "." + functionName + "(" + stringArguments + ")"));
                } catch (Exception exception) {
                    func.accept(null);
                }
            };
        }
        CustomThread thread = new CustomThread(threadFunction);
        thread.start();
        return thread;
    }
}
