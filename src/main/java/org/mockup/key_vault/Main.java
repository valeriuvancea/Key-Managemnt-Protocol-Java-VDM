package org.mockup.key_vault;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        KeyVault keyVault = new KeyVault();
        keyVault.Start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keyVault.Stop();
            }
        });

        while (true) {
            Thread.sleep(1000);
        }
    }
}
