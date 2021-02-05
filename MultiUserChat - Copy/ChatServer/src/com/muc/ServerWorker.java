package com.muc;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWorker implements Runnable{

    //List to hold references of objects created
    public ArrayList<Thread> clientList = new ArrayList<>();
    private ArrayList<PrintWriter> clientOutputStreams = new ArrayList();
    private final ArrayList<Messenger> message = new ArrayList<>();
    public ArrayList<String> username_list = new ArrayList<>();

//    get port number from myPort.java and use in setter
    myPort port = new myPort();
    public int portNumber = port.getValue();
    public ServerWorker(int portNumber){
        this.portNumber = portNumber;
    }

    public void run() {
        //Create ServerSocket and Client Socket
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server Running on port number: " + portNumber);
            while(true) {
                Socket socket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                Messenger txt = new Messenger(socket, writer);
                Thread client = new Thread(txt);
                client.start();
                System.out.println("Thread Name: " + client.getName() + " connected on " + portNumber);

                // Add objects to list
                clientOutputStreams.add(writer);
                clientList.add(client);
            }
        } catch (IOException e) {
            System.out.println("Server error... " + e.getMessage());
        }
    }

    public void broadCast(String txt) {
        for (PrintWriter writer : clientOutputStreams) {
            writer.println(txt);
            writer.flush();
        }
    }

    public class Messenger extends Thread {
        //Inner class variables
        private Socket socket;
        BufferedReader input;
        PrintWriter output;
        private boolean checkConnection = true;

        public Messenger(Socket socket, PrintWriter writer) {
            this.output = writer;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                //Making connection streams
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //Get username
                String userName = getUserName();
                broadCast(userName + " has joined the chatroom in port: " + portNumber);

                //Set the name of the thread to user selected name
                Thread.currentThread().setName(userName);
                String inputStr = "";
                while (inputStr != null) {
                    if (inputStr.equalsIgnoreCase("exit") || inputStr.equalsIgnoreCase("quit") || inputStr.equalsIgnoreCase("logoff")) {
                        break;
                    }
                    inputStr = input.readLine();
                    broadCast(Thread.currentThread().getName() + ": " + inputStr);
                }
                broadCast(Thread.currentThread().getName() + " left the chatroom");
                this.checkConnection = false;

            } catch (IOException e) {
                broadCast("User " + Thread.currentThread().getName() + " left the chatroom");
//                remove username from username list when user leaves the chatroom
                username_list.remove(Thread.currentThread().getName());
                this.checkConnection = false;
            } finally {
                try {
                    socket.close();
                    input.close();
                    output.close();
                } catch (IOException e) { }
            }
        }

        public String getUserName() throws IOException {
            String userName = "";
            while(userName.isEmpty()){
                output.println("Please Enter a unique Username");
                userName = input.readLine();
//                check if username is available
                while(true){
                    if(isNameFree(userName)) {
                        output.println("username created successfully!");
                        break;
                    } else{
                        output.println("Please try another username");
                        userName = input.readLine();
                    }
                }
            }
            return userName;
        }
//      check username arraylist for availability
        public boolean isNameFree(String username) {
            if (!(username_list.contains(username))) {
                username_list.add(username);
                return true;
            }
            return false;
        }
    }
}
