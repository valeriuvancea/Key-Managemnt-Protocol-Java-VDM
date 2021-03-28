package org.mockup;

import org.mockup.common.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Communication communicaiton = new Communication();
        communicaiton.Start();

        while (true) {
            System.out.println("Running");
            Thread.sleep(1000);
        }
    }
}
