package controller;

import java.io.IOException;

import javax.smartcardio.CommandAPDU;

import common.Common;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Controller running");
        Controller controller = new Controller();
    }
}
