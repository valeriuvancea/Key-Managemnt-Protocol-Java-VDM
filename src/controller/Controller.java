package controller;

import java.io.IOException;

import common.*;

public class Controller {
    private static final String CERT_M_FILE_PATH = "store/cert_m";
    private static final String ID_FILE_PATH = "store/id";
    private static final String SK_M_FILE_PATH = "store/sk_m";
    private static final String SK_CT_FILE_PATH = "store/sk_ct";
    private static final String PK_CT_FILE_PATH = "store/pk_ct";
    private static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private byte[] idBytes;
    private String idString;

    public Controller() throws IOException, InterruptedException {
        this.idBytes = Common.ReadFromFile(Controller.ID_FILE_PATH);
        this.idString = Common.ByteArrayToString(this.idBytes);

        if (Common.FileExists(Controller.SK_M_FILE_PATH)) {
            /* Hackish way for now */
            Pair<byte[], byte[]> controllerKeys = this.GenerateKeyPairTPM(Controller.PK_CT_FILE_PATH,
                    Controller.SK_CT_FILE_PATH);
            byte[] certController = Common.GenerateCertificate(Controller.CERT_M_FILE_PATH, Controller.SK_M_FILE_PATH,
                    controllerKeys.GetFirst(), this.idString);
            Common.WriteToFile(certController, Controller.CERT_CT_FILE_PATH);

            Common.RemoveFile(Controller.SK_M_FILE_PATH);
        }
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
}
