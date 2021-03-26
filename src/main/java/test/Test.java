package test;

import com.mycompany.app.Secured;

public class Test {

    @Secured(isLocked = true)
    public String lockedMethod() {
        System.out.println("Locked");
        return "asd";
    }

    @Secured(isLocked = false)
    public void unlockedMethod() {
        System.out.println("Unlocked");
    }
}