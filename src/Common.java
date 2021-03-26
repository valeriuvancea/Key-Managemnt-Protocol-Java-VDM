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
import java.util.Base64;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Common {
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
            put("A", (byte) 0xA);
            put("B", (byte) 0xB);
            put("C", (byte) 0xC);
            put("D", (byte) 0xD);
            put("E", (byte) 0xE);
            put("F", (byte) 0xF);
        }
    };

    public static String RunCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        process.waitFor();

        String line = "";
        StringBuilder outputBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line);
            outputBuilder.append(System.lineSeparator());
        }

        return outputBuilder.toString();
    }

    public static void WriteToFile(byte[] buffer, String filePath) throws IOException {
        File file = new File(filePath);
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
}