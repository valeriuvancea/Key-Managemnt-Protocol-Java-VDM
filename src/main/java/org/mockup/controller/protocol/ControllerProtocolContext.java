package org.mockup.controller.protocol;

import java.io.IOException;

import org.javatuples.Pair;
import org.mockup.common.Common;
import org.mockup.common.communication.Sender;
import org.mockup.common.crypto.Crypto;
import org.mockup.common.protocol.IContextTerminatedCallback;
import org.mockup.common.protocol.ProtocolContext;

public class ControllerProtocolContext extends ProtocolContext {
    public static final String CERT_M_FILE_PATH = "store/cert_m";
    public static final String ID_FILE_PATH = "store/id";
    public static final String SK_M_FILE_PATH = "store/sk_m";
    public static final String SK_CT_FILE_PATH = "store/sk_ct";
    public static final String PK_CT_FILE_PATH = "store/pk_ct";
    public static final String CERT_CT_FILE_PATH = "store/cert_ct";

    private final byte[] certController;

    public ControllerProtocolContext(Sender sender, IContextTerminatedCallback terminatedCallback)
            throws IOException, InterruptedException {
        super(Common.ReadFromFile(ControllerProtocolContext.ID_FILE_PATH), sender, terminatedCallback);

        if (Common.FileExists(ControllerProtocolContext.SK_M_FILE_PATH)) {
            /* Hackish way for now */
            Pair<byte[], byte[]> controllerKeys = Crypto.GenerateKeyPairTPM(ControllerProtocolContext.PK_CT_FILE_PATH,
                    ControllerProtocolContext.SK_CT_FILE_PATH);
            byte[] certControllerProtocolContext = Crypto.GenerateCertificate(
                    ControllerProtocolContext.CERT_M_FILE_PATH, ControllerProtocolContext.SK_M_FILE_PATH,
                    controllerKeys.getValue0(), this.associatedIdString);
            Common.WriteToFile(certControllerProtocolContext, ControllerProtocolContext.CERT_CT_FILE_PATH);

            Common.RemoveFile(ControllerProtocolContext.SK_M_FILE_PATH);
        }

        this.certController = Common.ReadFromFile(ControllerProtocolContext.CERT_CT_FILE_PATH);
    }

    public byte[] GetCertController() {
        return this.certController;
    }
}
