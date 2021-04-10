package org.mockup.controller;

import java.io.IOException;

public class Main {
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
