package org.mockup.key_vault;

import org.mockup.common.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Key vault started");
        KeyVault keyVault = new KeyVault();
        keyVault.Start();

        while (true) {

            Thread.sleep(3000);
            System.out.println("Key vault running");
        }
    }
}
