package executor;

import java.util.Arrays;
import java.util.function.Consumer;

import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.VoidReturnValue;

import vdm.RemoteController;

public abstract class Equipment {
    public enum ExecutorType {
        JAVA, VDM
    }

    protected ExecutorType executorType = ExecutorType.VDM;
    private String vdmVariableName = "";

    public Equipment() {
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
        Consumer<Consumer<Object>> threadFunction;
        if (executorType == ExecutorType.JAVA) {
            threadFunction = (saveResultFunction) -> {
                try {
                    saveResultFunction.accept(this.getClass()
                            .getDeclaredMethod(functionName,
                                    (Arrays.stream(arguments).map(object -> object.getClass()).toArray(Class[]::new)))
                            .invoke(this, arguments));
                } catch (Exception exception) {
                    saveResultFunction.accept(null);
                }
            };

        } else {
            String stringArguments = ((String) Arrays.stream(arguments).reduce("",
                    (accumulator, value) -> accumulator.toString() + "," + value.toString()));
            if (!stringArguments.isEmpty()) {
                stringArguments = stringArguments.substring(1);
            }
            final String vdmFunctionArguments = stringArguments;
            threadFunction = (saveResultFunction) -> {
                synchronized (this) {
                    try {
                        Thread.sleep(100);
                        saveResultFunction.accept(RemoteController.interpreter
                                .execute(vdmVariableName + "." + functionName + "(" + vdmFunctionArguments + ")"));
                    } catch (Exception exception) {
                        saveResultFunction.accept(null);
                    }
                }
            };
        }
        CustomThread thread = new CustomThread(threadFunction);
        thread.start();
        return thread;
    }
}
