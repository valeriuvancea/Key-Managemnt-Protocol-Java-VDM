package manufacturer;

import java.io.IOException;
import common.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String manufacturerName = "manufacturer";

        byte[] controller_1_id = "controller_1".getBytes();
        byte[] controller_2_id = "controller_2".getBytes();

        String pkManufacturerFilePath = "manufacturer_store/pk_m";
        String skManufacturerFilePath = "manufacturer_store/sk_m";
        String certManufacturerFilePath = "manufacturer_store/cert_m";
        /* Generate manufacturer objects */
        Pair<byte[], byte[]> manufacturerKeys = Common.GenerateKeyPair(pkManufacturerFilePath, skManufacturerFilePath);
        Common.GenerateSelfSignedCertificate(skManufacturerFilePath, certManufacturerFilePath, manufacturerName);

        /* Generate key vault objects */
        String skKeyVaultFilePath = "key_vault_store/sk_kv";
        String pkKeyVaultFilePath = "key_vault_store/pk_kv";
        String skCertificateAuthorityFilePath = "key_vault_store/sk_ca";
        String pkCertificateAuthorityFilePath = "key_vault_store/pk_ca";
        String certCertificateAuthorityFilePath = "key_vault_store/cert_ca";
        String certKeyVaultFilePath = "key_vault_store/cert_kv";
        String certManufacturerKeyVaultFilePath = "key_vault_store/cert_m";

        byte[] keyVaultId = "key_vault".getBytes();
        String keyVaultIdString = Common.ByteArrayToString(keyVaultId);

        Pair<byte[], byte[]> keyVaultKeys = Common.GenerateKeyPair(pkKeyVaultFilePath, skKeyVaultFilePath);
        Common.GenerateKeyPair(pkCertificateAuthorityFilePath, skCertificateAuthorityFilePath);
        byte[] certKeyVault = Common.GenerateCertificate(certManufacturerFilePath, skManufacturerFilePath,
                keyVaultKeys.GetFirst(), keyVaultIdString);
        Common.WriteToFile(certKeyVault, certKeyVaultFilePath);
        Common.GenerateSelfSignedCertificate(skCertificateAuthorityFilePath, certCertificateAuthorityFilePath,
                keyVaultIdString);

        /* Generate controller 1 object */
        Common.WriteToFile(controller_1_id, "controller_1_store/id");

        /* Generate controller 1 object */
        Common.WriteToFile(controller_1_id, "controller_2_store/id");
    }
}
