package org.mockup.key_vault;

import org.mockup.common.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Key vault started");

        Communication communicaiton = new Communication();
        communicaiton.Start();

        KeyVault keyVault = new KeyVault();
        System.out.println("Key vault finished");
    }
}
