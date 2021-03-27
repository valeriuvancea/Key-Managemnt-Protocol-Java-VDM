package key_vault;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Key vault started");
        KeyVault keyVault = new KeyVault();
        System.out.println(keyVault.toString());
    }
}
