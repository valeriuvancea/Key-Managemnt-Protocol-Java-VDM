package vdm;

import java.util.Timer;
import java.util.TimerTask;

import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.SeqValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.VoidReturnValue;

import executor.CustomThread;
import executor.Equipment;

public class Controller extends Equipment {
    private int a = 4;

    public final Value init() {
        a = 5;
        execute("a", new SeqValue("test"), new SeqValue("executor")).then(result -> {
            System.out.println(result);
        }).then(() -> {
            return execute("getValue", 3).getReturnValue();
        }).then(result -> {
            System.out.println(result);
        });
        (new Thread(() -> {
            execute("getValue", 3).then(result -> {
                System.out.println(result);
                // This is just because it takes a long time to write on console, so the program
                // would close without printing
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RemoteController.interpreter.finish();
            });
        })).start();
        return new VoidReturnValue();
    }

    public Value getValue(Value id) throws ValueException {
        return new IntegerValue(a);
    }

    public Value a(Value b, Value c) {
        return new SeqValue(b.toString() + c.toString());
    }
}
