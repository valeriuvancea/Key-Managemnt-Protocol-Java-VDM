package vdm;

import java.util.Timer;
import java.util.TimerTask;

import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;

public class RemoteController implements RemoteControl {
    public static RemoteInterpreter interpreter;

    public void run(RemoteInterpreter interpreter) throws Exception {
        RemoteController.interpreter = interpreter;
        System.out.println("Remote controller run");
        interpreter.create("controller", "new vdm_Controller(\"controller\")");
    }

}
