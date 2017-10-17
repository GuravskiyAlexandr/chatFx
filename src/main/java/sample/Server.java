package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by Alexsandr on 24.08.2017.
 * Первым делом запускаем сервер!!!!!!!!!!!!!!!
 */

public class Server {
    private ServerSocket serverSocket;
    private Socket socket, sock;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Thread thread, thread2, thread3, threadReadName;
    private HashMap<String, Socket> map;
    private final int port = 34334;
    private String nameClient;
    //private  Communication message = new Communication();
    private List<String> listName = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        Server server = new Server();

    }

    public Server() throws IOException {
        map = new HashMap<>();
        try{
            System.out.println("Starting");
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't listen to port "+port);
        }
        thread = new ServerThread();
        thread2 = new Check();
        thread.start();
        thread2.start();



    }

    private class ServerThread extends Thread {
        @Override
        public void run(){

            while (true){
                System.out.println("Try Connect");
                try {
                    socket  = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Can not accept");
                }
                try {
                    ServerLisner(socket);
                    System.out.println(" Connected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            }
        }

    private  void ServerLisner(Socket socket) throws IOException {
         inputStream = new ObjectInputStream(socket.getInputStream());
         outputStream = new ObjectOutputStream(socket.getOutputStream());
         threadReadName = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    nameClient = (String) inputStream.readObject();
                    System.out.println("Received a name " +nameClient);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!map.containsKey(nameClient)) {
                        map.put(nameClient, socket);
                        listName.add(nameClient);

                        try {
                            outputStream.writeObject("Successfully");

                            System.out.println("Successfully recorded name");
                            System.out.println(" Our list of names");
                            for (Map.Entry entry : map.entrySet()) {
                                System.out.println(entry.getKey());
                            }

                            thread3 = new messageHandler(socket);
                            thread3.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                } else {
                        try {
                            outputStream.writeObject("Not Successfully");
                            System.out.println("Not Successfully ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        });
        threadReadName.start();
    }

    private class  messageHandler extends Thread   {
        String incomingName;
        String incomingSMS;
        Socket socketHandler;
        Socket socketSend;
        Object obj;
        Communication message;

        public messageHandler(Socket socketHandler) {
            this.socketHandler = socketHandler;
        }

        @Override
        public void run(){
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream = new ObjectInputStream(socketHandler.getInputStream());
                    obj = inputStream.readObject();
                    if (obj instanceof Communication) {
                        message = (Communication) obj;

                        incomingSMS = message.getMessage();
                        incomingName = message.getName();
                        socketSend = map.get(incomingName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                System.out.println(socketSend);
                System.out.println(incomingName);
                System.out.println(incomingSMS);

                try {
                    outputStream = new ObjectOutputStream(socketSend.getOutputStream());
                    outputStream.writeObject(message);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }

        }

    private class Check extends Thread {

        @Override
        public void run(){

            while (true){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if(map.size()>0){

                    for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();){
                        Map.Entry entry = (Map.Entry) iterator.next();
                        try {
                            sock = (Socket) entry.getValue();
                            nameClient = (String) entry.getKey();
                            System.out.println(sock.isClosed());
                            outputStream = new ObjectOutputStream(sock.getOutputStream());
                            outputStream.writeObject(listName);
                            outputStream.flush();
                            System.out.println(listName+"  output");
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("fakkkkkkkkkkkkkkkkkkk");
                            for (Map.Entry entry1 : map.entrySet()){
                                System.out.println(entry1.getValue()+ "  "+entry1.getKey());
                            }
                            System.out.println(nameClient);
                            listName.remove(nameClient);
                            iterator.remove();
                        }
                    }

                }
            }
        }
    }
}

