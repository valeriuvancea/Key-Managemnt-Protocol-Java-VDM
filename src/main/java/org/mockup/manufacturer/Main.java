package org.mockup.manufacturer;

import java.io.IOException;

import org.javatuples.Pair;
import org.mockup.common.Common;
import org.mockup.common.crypto.Crypto;

public class Main {
        public static void main(String[] args) throws IOException, InterruptedException {
                String manufacturerNameString = "manufacturer";

                byte[] controller_1_id = "controller_1".getBytes();
                byte[] controller_2_id = "controller_2".getBytes();

                String pkManufacturerFilePath = "deployment_store/manufacturer/pk_m";
                String skManufacturerFilePath = "deployment_store/manufacturer/sk_m";
                String certManufacturerFilePath = "deployment_store/manufacturer/cert_m";
                /* Generate manufacturer objects */
                Pair<byte[], byte[]> manufacturerKeys = Crypto.GenerateKeyPair(pkManufacturerFilePath,
                                skManufacturerFilePath);
                byte[] certManufacturer = Crypto.GenerateSelfSignedCertificate(skManufacturerFilePath,
                                certManufacturerFilePath, manufacturerNameString);

                /* Generate key vault objects */
                String skKeyVaultFilePath = "deployment_store/key_vault/sk_kv";
                String pkKeyVaultFilePath = "deployment_store/key_vault/pk_kv";
                String skCertificateAuthorityFilePath = "deployment_store/key_vault/sk_ca";
                String pkCertificateAuthorityFilePath = "deployment_store/key_vault/pk_ca";
                String certCertificateAuthorityFilePath = "deployment_store/key_vault/cert_ca";
                String certKeyVaultFilePath = "deployment_store/key_vault/cert_kv";
                String certManufacturerKeyVaultFilePath = "deployment_store/key_vault/cert_m";

                byte[] keyVaultId = "key_vault".getBytes();
                String keyVaultIdString = Common.ByteArrayToString(keyVaultId);

                Pair<byte[], byte[]> keyVaultKeys = Crypto.GenerateKeyPair(pkKeyVaultFilePath, skKeyVaultFilePath);
                Common.WriteToFile(certManufacturer, certManufacturerKeyVaultFilePath);
                Crypto.GenerateKeyPair(pkCertificateAuthorityFilePath, skCertificateAuthorityFilePath);
                byte[] certKeyVault = Crypto.GenerateCertificate(certManufacturerFilePath, skManufacturerFilePath,
                                keyVaultKeys.getValue0(), keyVaultIdString);
                Common.WriteToFile(certKeyVault, certKeyVaultFilePath);
                Crypto.GenerateSelfSignedCertificate(skCertificateAuthorityFilePath, certCertificateAuthorityFilePath,
                                keyVaultIdString);

                /*
                 * Upload cert_m and sk_m to the controllers. Controllers will generate and sign
                 * keys using the sk_m. It is easier to let them do it, as they will use the
                 * TPMs. After that they will delete the sk_m. Not the proper way, but fine for
                 * now.
                 */

                /* Generate controller 1 object */
                String idController1FilePath = "deployment_store/controller_1/id";
                String certManufacturerController1FilePath = "deployment_store/controller_1/cert_m";
                String skManufacturerController1FilePath = "deployment_store/controller_1/sk_m";
                Common.WriteToFile(controller_1_id, idController1FilePath);
                Common.WriteToFile(certManufacturer, certManufacturerController1FilePath);
                Common.WriteToFile(manufacturerKeys.getValue1(), skManufacturerController1FilePath);

                /* Generate controller 1 object */
                String idController2FilePath = "deployment_store/controller_2/id";
                String certManufacturerController2FilePath = "deployment_store/controller_2/cert_m";
                String skManufacturerController2FilePath = "deployment_store/controller_2/sk_m";
                Common.WriteToFile(controller_2_id, idController2FilePath);
                Common.WriteToFile(certManufacturer, certManufacturerController2FilePath);
                Common.WriteToFile(manufacturerKeys.getValue1(), skManufacturerController2FilePath);
        }
}
