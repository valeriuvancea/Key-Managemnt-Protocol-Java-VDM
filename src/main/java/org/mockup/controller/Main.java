package org.mockup.controller;

import java.io.IOException;

public class Main {
    private final static String EXIT_STRING = "exit()";

    public static void main(String[] args) throws IOException, InterruptedException {
        Controller controller = new Controller();
        controller.Start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                controller.Stop();
            }
        });

        while (true) {
            Thread.sleep(1000);
        }
    }
}
