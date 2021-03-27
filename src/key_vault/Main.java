package key_vault;

import java.io.IOException;
import common.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        KeyVault keyVault = new KeyVault();
        System.out.println(keyVault.toString());
    }
}
