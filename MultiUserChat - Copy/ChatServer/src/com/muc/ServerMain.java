package com.muc;

public class ServerMain {
//    get port number from myPort.java
    myPort port = new myPort();
    public int portNumber = port.getValue();

    ServerWorker my_chat = new ServerWorker(portNumber);

    void runServer() {
        Thread myChat = new Thread(my_chat);
        myChat.start();
    }

    public static void main(String[] args) {
        new ServerMain().runServer();
    }
}
