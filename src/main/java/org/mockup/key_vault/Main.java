package org.mockup.key_vault;

import org.mockup.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        KeyVault keyVault = new KeyVault();
        keyVault.Start();

        while (true) {
            Thread.sleep(5000);
            logger.info("Running");
            keyVault.Kick();
        }
    }
}
