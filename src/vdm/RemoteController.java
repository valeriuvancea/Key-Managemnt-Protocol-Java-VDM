package vdm;

import java.util.Timer;
import java.util.TimerTask;

import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;

public class RemoteController implements RemoteControl {
    public void run(RemoteInterpreter interpreter) throws Exception {
        System.out.println("Remote controller run");
        System.out.println("The answer is " + interpreter.execute("1 + 1"));
        interpreter.create("controller", "new vdm_Controller()");
        System.out.println(interpreter.execute("controller.a(4)"));
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    System.out.println(interpreter.execute("controller.getValue(3)"));
                    interpreter.finish();
                } catch (Exception exception) {

                }
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 2000L;
        timer.schedule(task, delay);
    }
}
