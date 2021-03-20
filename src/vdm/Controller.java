package vdm;

import java.util.Timer;
import java.util.TimerTask;

import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.SeqValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.VoidReturnValue;

import executor.Executor;

public class Controller extends Executor {
    private int a = 4;

    public final Value init() {
        a = 5;
        try {
            execute("a", x -> {
                System.out.println((String) x);
            }, "test", "executor");
        } catch (Exception ex) {

        }
        return new VoidReturnValue();
    }

    public Value getValue(Value id) throws ValueException {
        return new IntegerValue(a);
    }

    public Value a(Value b, Value c) {
        return new SeqValue(b.toString() + c.toString());
    }
}
