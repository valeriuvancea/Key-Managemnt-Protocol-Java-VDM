package org.mockup.controller;

import java.io.IOException;

import org.javatuples.Pair;
import org.mockup.common.*;

public class Controller {
    private static final String CERT_M_FILE_PATH = "store/cert_m";
    private static final String ID_FILE_PATH = "store/id";
    private static final String SK_M_FILE_PATH = "store/sk_m";
    private static final String SK_CT_FILE_PATH = "store/sk_ct";
    private static final String PK_CT_FILE_PATH = "store/pk_ct";
    private static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private byte[] idBytes;
    private String idString;
    private byte[] certController;

    public Controller() throws IOException, InterruptedException {
        this.idBytes = Common.ReadFromFile(Controller.ID_FILE_PATH);
        this.idString = Common.ByteArrayToString(this.idBytes);

        if (Common.FileExists(Controller.SK_M_FILE_PATH)) {
            /* Hackish way for now */
            Pair<byte[], byte[]> controllerKeys = this.GenerateKeyPairTPM(Controller.PK_CT_FILE_PATH,
                    Controller.SK_CT_FILE_PATH);
            byte[] certController = Common.GenerateCertificate(Controller.CERT_M_FILE_PATH, Controller.SK_M_FILE_PATH,
                    controllerKeys.getValue0(), this.idString);
            Common.WriteToFile(certController, Controller.CERT_CT_FILE_PATH);

            Common.RemoveFile(Controller.SK_M_FILE_PATH);
        }

        this.certController = Common.ReadFromFile(Controller.CERT_CT_FILE_PATH);
    }

    private Pair<byte[], byte[]> GenerateKeyPairTPM(String pkFilePath, String skFilePath)
            throws IOException, InterruptedException {
        Common.RunCommand(String.format("tpm2tss-genkey -a rsa -s 2048 %s", skFilePath));
        Common.RunCommand(
                String.format("openssl rsa -engine tpm2tss -inform engine -in %s -pubout -outform pem -out %s",
                        skFilePath, pkFilePath));
        byte[] pkBytes = Common.ReadFromFile(pkFilePath);
        byte[] skBytes = Common.ReadFromFile(skFilePath);
        return new Pair<byte[], byte[]>(pkBytes, skBytes);
    }

    private byte[] GenerateRandomByteArrayTPM(int numberOfBytes) throws IOException, InterruptedException {
        try {
            Common.RunCommand(String.format("openssl rand -out %s -engine tpm2tss -hex %s", Common.TEMP_DATA_FILE,
                    numberOfBytes));
            String output = new String(Common.ReadFromFile(Common.TEMP_DATA_FILE));
            byte[] bytes = Common.StringToByteArray(output);
            return bytes;
        } finally {
            Common.RemoveFile(Common.TEMP_DATA_FILE, false);
        }

    }

    private byte[] GenerateRandomByteArrayTPM() throws IOException, InterruptedException {
        return this.GenerateRandomByteArrayTPM(128);
    }

    private byte[] SignTPM(String skFilePath, byte[] data) throws IOException, InterruptedException {
        try {
            byte[] digest = Common.GetDataDigest(data);
            Common.WriteToFile(digest, Common.TEMP_DATA_FILE);
            /* */
            String output = Common.RunCommand(
                    String.format("openssl pkeyutl -engine tpm2tss -keyform engine -inkey %s -sign -in %s -out %s",
                            skFilePath, Common.TEMP_DATA_FILE, Common.TEMP_SIGN_FILE));
            return Common.ReadFromFile(Common.TEMP_SIGN_FILE);
        } finally {
            Common.RemoveFile(Common.TEMP_DATA_FILE);
            Common.RemoveFile(Common.TEMP_SIGN_FILE);
        }
    }

    private byte[] DecryptTPM(String skFilePath, byte[] cipher) throws IOException, InterruptedException {
        try {
            Common.WriteToFile(cipher, Common.TEMP_CIPHER_FILE);
            Common.RunCommand(
                    String.format("openssl pkeyutl -engine tpm2tss -keyform engine -inkey %s -in %s -decrypt -out %s",
                            skFilePath, Common.TEMP_CIPHER_FILE, Common.TEMP_DATA_FILE));
            return Common.ReadFromFile(Common.TEMP_DATA_FILE);
        } finally {
            Common.RemoveFile(Common.TEMP_CIPHER_FILE, false);
            Common.RemoveFile(Common.TEMP_DATA_FILE, false);
        }
    }
}
