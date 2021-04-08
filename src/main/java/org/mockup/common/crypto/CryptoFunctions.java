package org.mockup.common.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.javatuples.Pair;
import org.mockup.common.Common;

/**
 * This class contains static functions implementing various crypto operations.
 * Some of the functions are synchronized to prevent multiple threads executing
 * them at the same time. This is due to the fact that those functions share
 * files where they stash data in order to execute the crypto operations. This
 * is hacky and can be done better, but will do for our purposes.
 */
public class CryptoFunctions {
    private static final String TEMP_KEY_FILE = "temp/temp_key";
    private static final String TEMP_CSR_FILE = "temp/temp_csr";
    private static final String TEMP_CERT_FILE = "temp/temp_cert";
    private static final String TEMP_DATA_FILE = "temp/temp_data";
    private static final String TEMP_SIGN_FILE = "temp/temp_sign";
    private static final String TEMP_CIPHER_FILE = "temp/temp_cipher";

    private static String RunCommand(String command) throws IOException, InterruptedException {
        String[] commands = command.split("\\s+");
        Process process = Runtime.getRuntime().exec(commands);

        process.waitFor();

        String line = "";
        StringBuilder outputBuilder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line);
            outputBuilder.append(System.lineSeparator());
        }

        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line);
            outputBuilder.append(System.lineSeparator());
        }

        String output = outputBuilder.toString();
        return output;
    }

    public static Pair<byte[], byte[]> GenerateKeyPair(String pkFilePath, String skFilePath)
            throws IOException, InterruptedException {
        CryptoFunctions.RunCommand(String.format("openssl genrsa -out %s 2048", skFilePath));
        CryptoFunctions
                .RunCommand(String.format("openssl rsa -in %s -pubout -out %s -outform PEM", skFilePath, pkFilePath));
        byte[] pkBytes = Common.ReadFromFile(pkFilePath);
        byte[] skBytes = Common.ReadFromFile(skFilePath);
        return new Pair<byte[], byte[]>(pkBytes, skBytes);
    }

    public static byte[] GenerateSelfSignedCertificate(String skFilePath, String certFilePath, String issuerName)
            throws IOException, InterruptedException {
        CryptoFunctions.RunCommand(
                String.format("openssl req -x509 -new -nodes -key %s -sha256 -days 1825 -subj /CN=%s -out %s",
                        skFilePath, issuerName, certFilePath));
        byte[] certBytes = Common.ReadFromFile(certFilePath);
        return certBytes;
    }

    public static byte[] GenerateCertificate(String certCertificateAuthorityFilePath,
            String skCertificateAuthorityFilePath, byte[] pkBytes, String pkOwnerName, int daysValid)
            throws IOException, InterruptedException {
        /*
         * To do this in command line, a signing request is needed. Signing request
         * needs both the private and public keys. We don't have them. Therefore,
         * signing request is generated for the ca private and public keys. Then, a
         * certificate is generated using this dummy signing requests and a command
         * which forces it to use the public key that actually needs to be signed.
         */

        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(pkBytes, CryptoFunctions.TEMP_KEY_FILE);
                /* Generate dummy signing request. */
                CryptoFunctions.RunCommand(String.format("openssl req -new -sha256 -key %s -subj /CN=%s -out %s",
                        skCertificateAuthorityFilePath, pkOwnerName, CryptoFunctions.TEMP_CSR_FILE));
                /*
                 * Generate certificate by forcing to use different public key than the one in
                 * the dummy signing request.
                 */
                CryptoFunctions.RunCommand(String.format(
                        "openssl x509 -req -in %s -force_pubkey %s -CA %s -CAkey %s -CAcreateserial -out %s  -days %s -sha256",
                        CryptoFunctions.TEMP_CSR_FILE, CryptoFunctions.TEMP_KEY_FILE, certCertificateAuthorityFilePath,
                        skCertificateAuthorityFilePath, CryptoFunctions.TEMP_CERT_FILE, daysValid));
                byte[] certBytes = Common.ReadFromFile(CryptoFunctions.TEMP_CERT_FILE);
                return certBytes;
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_KEY_FILE, false);
                Common.RemoveFile(CryptoFunctions.TEMP_CSR_FILE, false);
                Common.RemoveFile(CryptoFunctions.TEMP_CERT_FILE, false);
            }
        }
    }

    public static byte[] GenerateCertificate(String certCertificateAuthorityFilePath,
            String skCertificateAuthorityFilePath, byte[] pkBytes, String pkOwnerName)
            throws IOException, InterruptedException {
        return CryptoFunctions.GenerateCertificate(certCertificateAuthorityFilePath, skCertificateAuthorityFilePath,
                pkBytes, pkOwnerName, 365);
    }

    public static byte[] GetDataDigest(byte[] data) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(data, CryptoFunctions.TEMP_DATA_FILE);
                CryptoFunctions.RunCommand(String.format("openssl dgst -out %s -sha256 %s",
                        CryptoFunctions.TEMP_SIGN_FILE, CryptoFunctions.TEMP_DATA_FILE));
                String outputText = new String(Common.ReadFromFile(CryptoFunctions.TEMP_SIGN_FILE));
                outputText = outputText.replace(String.format("SHA256(%s)= ", CryptoFunctions.TEMP_DATA_FILE), "");
                outputText = outputText.replace("\n", "");
                outputText = outputText.replace("\r", "");
                byte[] output = Common.StringToByteArray(outputText);
                return output;
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_SIGN_FILE);
            }
        }
    }

    public static byte[] Sign(String skFilePath, byte[] data) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                byte[] digest = CryptoFunctions.GetDataDigest(data);
                Common.WriteToFile(digest, CryptoFunctions.TEMP_DATA_FILE);
                CryptoFunctions.RunCommand(String.format("openssl rsautl -sign -out %s -inkey %s -keyform PEM -in %s",
                        CryptoFunctions.TEMP_SIGN_FILE, skFilePath, CryptoFunctions.TEMP_DATA_FILE));
                return Common.ReadFromFile(CryptoFunctions.TEMP_SIGN_FILE);
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_SIGN_FILE);
            }
        }
    }

    public static byte[] ExtractKeyFromCertificate(byte[] certificate) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(certificate, CryptoFunctions.TEMP_CERT_FILE);
                CryptoFunctions.RunCommand(String.format("openssl x509 -pubkey -out %s -noout -in %s",
                        CryptoFunctions.TEMP_KEY_FILE, CryptoFunctions.TEMP_CERT_FILE));
                return Common.ReadFromFile(CryptoFunctions.TEMP_KEY_FILE);
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_KEY_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_CERT_FILE);
            }
        }
    }

    public static boolean IsSignatureValid(byte[] certificate, byte[] data, byte[] signature)
            throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                byte[] key = CryptoFunctions.ExtractKeyFromCertificate(certificate);
                byte[] digest = CryptoFunctions.GetDataDigest(data);
                Common.WriteToFile(signature, CryptoFunctions.TEMP_SIGN_FILE);
                Common.WriteToFile(digest, CryptoFunctions.TEMP_DATA_FILE);
                Common.WriteToFile(key, CryptoFunctions.TEMP_KEY_FILE);

                String result = CryptoFunctions.RunCommand(String.format(
                        "openssl pkeyutl -pubin -inkey %s -verify -in %s -sigfile %s", CryptoFunctions.TEMP_KEY_FILE,
                        CryptoFunctions.TEMP_DATA_FILE, CryptoFunctions.TEMP_SIGN_FILE));

                if (result.contains("Signature Verified Successfully")) {
                    return true;
                } else {
                    return false;
                }
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_SIGN_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_KEY_FILE);
            }
        }
    }

    public static boolean IsCertificateValid(byte[] certToValidate, String certCAFilePath)
            throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(certToValidate, CryptoFunctions.TEMP_CERT_FILE);
                String result = CryptoFunctions.RunCommand(
                        String.format("openssl verify -CAfile %s %s", certCAFilePath, CryptoFunctions.TEMP_CERT_FILE));
                if (result.contains(String.format("%s: OK", CryptoFunctions.TEMP_CERT_FILE))) {
                    return true;
                } else {
                    return false;
                }
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_CERT_FILE);
            }
        }
    }

    public static byte[] Encrypt(byte[] cert, byte[] data) throws IOException, InterruptedException {
        /* Note, current implementation is limited in allowed data size */
        synchronized (CryptoFunctions.class) {
            try {
                byte[] key = CryptoFunctions.ExtractKeyFromCertificate(cert);
                Common.WriteToFile(data, CryptoFunctions.TEMP_DATA_FILE);
                Common.WriteToFile(key, CryptoFunctions.TEMP_KEY_FILE);

                String output = CryptoFunctions.RunCommand(String.format(
                        "openssl rsautl -encrypt -inkey %s -pubin -in %s -out %s", CryptoFunctions.TEMP_KEY_FILE,
                        CryptoFunctions.TEMP_DATA_FILE, CryptoFunctions.TEMP_CIPHER_FILE));
                System.out.println(output);
                return Common.ReadFromFile(CryptoFunctions.TEMP_CIPHER_FILE);

            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_KEY_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_CIPHER_FILE);
            }
        }
    }

    public static byte[] GenerateRandomByteArray(int numberOfBytes) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                CryptoFunctions.RunCommand(
                        String.format("openssl rand -out %s -hex %s", CryptoFunctions.TEMP_DATA_FILE, numberOfBytes));
                String output = new String(Common.ReadFromFile(CryptoFunctions.TEMP_DATA_FILE));
                byte[] bytes = Common.StringToByteArray(output);
                return bytes;
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE, false);
            }
        }
    }

    public static byte[] GenerateRandomByteArray() throws IOException, InterruptedException {
        return CryptoFunctions.GenerateRandomByteArray(128);
    }

    public static byte[] Decrypt(String skFilePath, byte[] cipher) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(cipher, CryptoFunctions.TEMP_CIPHER_FILE);
                CryptoFunctions.RunCommand(String.format("openssl rsautl -decrypt -inkey %s -in %s -out %s", skFilePath,
                        CryptoFunctions.TEMP_CIPHER_FILE, CryptoFunctions.TEMP_DATA_FILE));
                return Common.ReadFromFile(CryptoFunctions.TEMP_DATA_FILE);
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_CIPHER_FILE, false);
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE, false);
            }
        }
    }

    private Pair<byte[], byte[]> GenerateKeyPairTPM(String pkFilePath, String skFilePath)
            throws IOException, InterruptedException {
        CryptoFunctions.RunCommand(String.format("tpm2tss-genkey -a rsa -s 2048 %s", skFilePath));
        CryptoFunctions.RunCommand(
                String.format("openssl rsa -engine tpm2tss -inform engine -in %s -pubout -outform pem -out %s",
                        skFilePath, pkFilePath));
        byte[] pkBytes = Common.ReadFromFile(pkFilePath);
        byte[] skBytes = Common.ReadFromFile(skFilePath);
        return new Pair<byte[], byte[]>(pkBytes, skBytes);
    }

    private byte[] GenerateRandomByteArrayTPM(int numberOfBytes) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                CryptoFunctions.RunCommand(String.format("openssl rand -out %s -engine tpm2tss -hex %s",
                        CryptoFunctions.TEMP_DATA_FILE, numberOfBytes));
                String output = new String(Common.ReadFromFile(CryptoFunctions.TEMP_DATA_FILE));
                byte[] bytes = Common.StringToByteArray(output);
                return bytes;
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE, false);
            }
        }
    }

    private byte[] GenerateRandomByteArrayTPM() throws IOException, InterruptedException {
        return this.GenerateRandomByteArrayTPM(128);
    }

    private byte[] SignTPM(String skFilePath, byte[] data) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                byte[] digest = CryptoFunctions.GetDataDigest(data);
                Common.WriteToFile(digest, CryptoFunctions.TEMP_DATA_FILE);
                /* */
                String output = CryptoFunctions.RunCommand(
                        String.format("openssl pkeyutl -engine tpm2tss -keyform engine -inkey %s -sign -in %s -out %s",
                                skFilePath, CryptoFunctions.TEMP_DATA_FILE, CryptoFunctions.TEMP_SIGN_FILE));
                return Common.ReadFromFile(CryptoFunctions.TEMP_SIGN_FILE);
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE);
                Common.RemoveFile(CryptoFunctions.TEMP_SIGN_FILE);
            }
        }
    }

    private byte[] DecryptTPM(String skFilePath, byte[] cipher) throws IOException, InterruptedException {
        synchronized (CryptoFunctions.class) {
            try {
                Common.WriteToFile(cipher, CryptoFunctions.TEMP_CIPHER_FILE);
                CryptoFunctions.RunCommand(String.format(
                        "openssl pkeyutl -engine tpm2tss -keyform engine -inkey %s -in %s -decrypt -out %s", skFilePath,
                        CryptoFunctions.TEMP_CIPHER_FILE, CryptoFunctions.TEMP_DATA_FILE));
                return Common.ReadFromFile(CryptoFunctions.TEMP_DATA_FILE);
            } finally {
                Common.RemoveFile(CryptoFunctions.TEMP_CIPHER_FILE, false);
                Common.RemoveFile(CryptoFunctions.TEMP_DATA_FILE, false);
            }
        }
    }
}
