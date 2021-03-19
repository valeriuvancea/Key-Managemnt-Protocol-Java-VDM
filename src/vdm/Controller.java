package vdm;

import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.Value;

public class Controller {
    public Value getValue(Value id) throws ValueException {
        int result = 3;
        return new IntegerValue(result);
    }
}
