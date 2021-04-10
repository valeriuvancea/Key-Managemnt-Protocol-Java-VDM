package org.mockup.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import javax.xml.bind.DatatypeConverter;

public class Common {
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

    public static String GetIpAddress() throws SocketException {
        String interfaceName = "eth0";
        return Common.GetIpAddress(interfaceName);
    }

    public static String GetIpAddress(String interfaceName) throws SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
        Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
        InetAddress currentAddress;

        while (inetAddress.hasMoreElements()) {
            currentAddress = inetAddress.nextElement();
            if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                return currentAddress.getHostAddress();
            }
        }

        return "";
    }

    public static String ByteArrayToString(byte[] value) {
        return DatatypeConverter.printHexBinary(value).toLowerCase();
    }

    public static byte[] StringToByteArray(String value) {
        return DatatypeConverter.parseHexBinary(value.toUpperCase());
    }

    public static boolean FileExists(String filePath) {
        return new File(filePath).exists();
    }
}