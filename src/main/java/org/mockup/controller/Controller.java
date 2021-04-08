package org.mockup.controller;

import java.io.IOException;

import org.javatuples.Pair;
import org.mockup.common.protocol.*;
import org.mockup.common.*;

public class Controller extends ProtocolContext {
    private static final String CERT_M_FILE_PATH = "store/cert_m";
    private static final String ID_FILE_PATH = "store/id";
    private static final String SK_M_FILE_PATH = "store/sk_m";
    private static final String SK_CT_FILE_PATH = "store/sk_ct";
    private static final String PK_CT_FILE_PATH = "store/pk_ct";
    private static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private final byte[] idBytes;
    private final String idString;
    private final byte[] certController;
    private final Communication communication;

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
        this.communication = new Communication(Common.GetIpAddress(), this);

    }

}
