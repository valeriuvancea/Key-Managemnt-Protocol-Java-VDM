package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.smartcardio.CommandAPDU;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Common {
    public static final String TEMP_KEY_FILE = "temp/temp_key";
    public static final String TEMP_CSR_FILE = "temp/temp_csr";
    public static final String TEMP_CERT_FILE = "temp/temp_cert";
    public static final String TEMP_DATA_FILE = "temp/temp_data";
    public static final String TEMP_SIGN_FILE = "temp/temp_sign";

    private static final BidiMap<String, Byte> HEX_MAP = new DualHashBidiMap() {
        {
            put("0", (byte) 0x0);
            put("1", (byte) 0x1);
            put("2", (byte) 0x2);
            put("3", (byte) 0x3);
            put("4", (byte) 0x4);
            put("5", (byte) 0x5);
            put("6", (byte) 0x6);
            put("7", (byte) 0x7);
            put("8", (byte) 0x8);
            put("9", (byte) 0x9);
            put("a", (byte) 0xA);
            put("b", (byte) 0xB);
            put("c", (byte) 0xC);
            put("d", (byte) 0xD);
            put("e", (byte) 0xE);
            put("f", (byte) 0xF);
        }
    };

    public static String RunCommand(String command) throws IOException, InterruptedException {
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

    public static void WriteToFile(byte[] buffer, String filePath) throws IOException {
        File file = new File(filePath);
        File directory = new File(file.getParent());

        if (!directory.exists()) {
            directory.mkdir();
        }

        file.createNewFile();
        FileOutputStream output = new FileOutputStream(file);
        output.write(buffer);
        output.close();
    }

    public static byte[] ReadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream input = new FileInputStream(file);
        byte[] output = new byte[(int) file.length()];
        input.read(output);
        input.close();
        return output;
    }

    public static void RenameFile(String currentFile, String newFile) {
        new File(currentFile).renameTo(new File(newFile));
    }

    public static void RemoveFile(String filePath, Boolean safe) {
        try {
            new File(filePath).delete();
        } catch (Exception e) {
            if (safe) {
                throw e;
            }
        }
    }

    public static void RemoveFile(String filePath) {
        RemoveFile(filePath, true);
    }

    public static InetAddress GetIpAddress() throws SocketException {
        String interfaceName = "eth0";
        NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
        Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
        InetAddress currentAddress;
        currentAddress = inetAddress.nextElement();
        while (inetAddress.hasMoreElements()) {
            currentAddress = inetAddress.nextElement();
            if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                return currentAddress;
            }
        }

        return null;
    }

    public static String ByteArrayToString(byte[] value) {
        char[] output = new char[value.length * 2];
        for (int i = 0; i < value.length; i++) {
            output[i * 2] = HEX_MAP.getKey((byte) (value[i] >> 4 & 0x0F)).charAt(0);
            output[i * 2 + 1] = HEX_MAP.getKey((byte) (value[i] & 0x0F)).charAt(0);
        }

        return new String(output);
    }

    public static byte[] StringToByteArray(String value) {
        byte[] output = new byte[value.length() / 2];
        for (int i = 0; i < value.length() / 2; i++) {
            String first = Character.toString(value.charAt(i * 2));
            String second = Character.toString(value.charAt((i * 2) + 1));

            byte result = (byte) (HEX_MAP.get(first) << 4);
            result = (byte) (result | HEX_MAP.get(second));
            output[i] = result;
        }

        return output;
    }

    public static boolean FileExists(String filePath) {
        return new File(filePath).exists();
    }

    public static Pair<byte[], byte[]> GenerateKeyPair(String pkFilePath, String skFilePath)
            throws IOException, InterruptedException {
        Common.RunCommand(String.format("openssl genrsa -out %s 2048", skFilePath));
        Common.RunCommand(String.format("openssl rsa -in %s -pubout -out %s -outform PEM", skFilePath, pkFilePath));
        byte[] pkBytes = Common.ReadFromFile(pkFilePath);
        byte[] skBytes = Common.ReadFromFile(skFilePath);
        return new Pair<byte[], byte[]>(pkBytes, skBytes);
    }

    public static byte[] GenerateSelfSignedCertificate(String skFilePath, String certFilePath, String issuerName)
            throws IOException, InterruptedException {
        Common.RunCommand(String.format("openssl req -x509 -new -nodes -key %s -sha256 -days 1825 -subj /CN=%s -out %s",
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
        try {
            Common.WriteToFile(pkBytes, Common.TEMP_KEY_FILE);
            /* Generate dummy signing request. */
            Common.RunCommand(String.format("openssl req -new -sha256 -key %s -subj /CN=%s -out %s",
                    skCertificateAuthorityFilePath, pkOwnerName, Common.TEMP_CSR_FILE));
            /*
             * Generate certificate by forcing to use different public key than the one in
             * the dummy signing request.
             */
            Common.RunCommand(String.format(
                    "openssl x509 -req -in %s -force_pubkey %s -CA %s -CAkey %s -CAcreateserial -out %s  -days %s -sha256",
                    Common.TEMP_CSR_FILE, Common.TEMP_KEY_FILE, certCertificateAuthorityFilePath,
                    skCertificateAuthorityFilePath, Common.TEMP_CERT_FILE, daysValid));
            byte[] certBytes = Common.ReadFromFile(Common.TEMP_CERT_FILE);
            return certBytes;
        } finally {
            Common.RemoveFile(Common.TEMP_KEY_FILE, false);
            Common.RemoveFile(Common.TEMP_CSR_FILE, false);
            Common.RemoveFile(Common.TEMP_CERT_FILE, false);
        }
    }

    public static byte[] GenerateCertificate(String certCertificateAuthorityFilePath,
            String skCertificateAuthorityFilePath, byte[] pkBytes, String pkOwnerName)
            throws IOException, InterruptedException {
        return Common.GenerateCertificate(certCertificateAuthorityFilePath, skCertificateAuthorityFilePath, pkBytes,
                pkOwnerName, 365);
    }

    public static byte[] GetDataDigest(byte[] data) throws IOException, InterruptedException {
        try {
            Common.WriteToFile(data, Common.TEMP_DATA_FILE);
            Common.RunCommand(
                    String.format("openssl dgst -out %s -sha256 %s", Common.TEMP_SIGN_FILE, Common.TEMP_DATA_FILE));
            String outputText = new String(Common.ReadFromFile(Common.TEMP_SIGN_FILE));
            outputText = outputText.replace(String.format("SHA256(%s)= ", Common.TEMP_DATA_FILE), "");
            outputText = outputText.replace("\n", "");
            outputText = outputText.replace("\r", "");
            byte[] output = Common.StringToByteArray(outputText);
            return output;
        } finally {
            Common.RemoveFile(Common.TEMP_DATA_FILE);
            Common.RemoveFile(Common.TEMP_SIGN_FILE);
        }
    }

    public static byte[] SignData(String skFilePath, byte[] data) throws IOException, InterruptedException {
        try {
            byte[] digest = Common.GetDataDigest(data);
            Common.WriteToFile(digest, Common.TEMP_DATA_FILE);
            Common.RunCommand(String.format("openssl rsautl -sign -out %s -inkey %s -keyform PEM -in %s",
                    Common.TEMP_SIGN_FILE, skFilePath, Common.TEMP_DATA_FILE));
            return Common.ReadFromFile(Common.TEMP_SIGN_FILE);
        } finally {
            Common.RemoveFile(Common.TEMP_DATA_FILE);
            Common.RemoveFile(Common.TEMP_SIGN_FILE);
        }
    }

    public static byte[] ExtractKeyFromCertificate(byte[] certificate) throws IOException, InterruptedException {
        try {
            Common.WriteToFile(certificate, Common.TEMP_CERT_FILE);
            Common.RunCommand(String.format("openssl x509 -pubkey -out %s -noout -in %s", Common.TEMP_KEY_FILE,
                    Common.TEMP_CERT_FILE));
            return Common.ReadFromFile(Common.TEMP_KEY_FILE);
        } finally {
            Common.RemoveFile(Common.TEMP_KEY_FILE);
            Common.RemoveFile(Common.TEMP_CERT_FILE);
        }
    }

    public static boolean IsSignatureValid(byte[] certificate, byte[] data, byte[] signature)
            throws IOException, InterruptedException {
        /*
         * Did not find other way to verify signature generated using tpm module other
         * than the openssl command line below. Haven't figure how to provide inputs to
         * the command line through stdin either. Therefore, a hacky solution for now -
         * save required values into files, perform verification, remove files, return
         * result...
         */
        try {
            byte[] key = Common.ExtractKeyFromCertificate(certificate);
            byte[] digest = Common.GetDataDigest(data);
            Common.WriteToFile(signature, Common.TEMP_SIGN_FILE);
            Common.WriteToFile(digest, Common.TEMP_DATA_FILE);
            Common.WriteToFile(key, Common.TEMP_KEY_FILE);

            String result = Common
                    .RunCommand(String.format("openssl pkeyutl -pubin -inkey %s -verify -in %s -sigfile %s",
                            Common.TEMP_KEY_FILE, Common.TEMP_DATA_FILE, Common.TEMP_SIGN_FILE));

            if (result.contains("Signature Verified Successfully")) {
                return true;
            } else {
                return false;
            }
        } finally {
            Common.RemoveFile(Common.TEMP_SIGN_FILE);
            Common.RemoveFile(Common.TEMP_DATA_FILE);
            Common.RemoveFile(Common.TEMP_KEY_FILE);
        }
    }

    public static boolean IsCertificateValid(byte[] certToValidate, String certCAFilePath)
            throws IOException, InterruptedException {
        try {
            Common.WriteToFile(certToValidate, Common.TEMP_CERT_FILE);
            String result = Common
                    .RunCommand(String.format("openssl verify -CAfile %s %s", certCAFilePath, Common.TEMP_CERT_FILE));
            if (result.contains(String.format("%s: OK", Common.TEMP_CERT_FILE))) {
                return true;
            } else {
                return false;
            }
        } finally {
            Common.RemoveFile(Common.TEMP_CERT_FILE);
        }
    }
}