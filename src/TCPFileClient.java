import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPFileClient {
    public static void main(String[] args) throws IOException {

        ByteBuffer request;
        SocketChannel channel = SocketChannel.open();
        ByteBuffer reply;

        if(args.length != 2) {
            System.out.println("Please specify server IP and server port");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);
        String command;
        do{
            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine().toUpperCase();
            switch(command){
                case "E"://delete
                    System.out.println("Please enter a file name:");
                    String filename = keyboard.nextLine();
                    request = ByteBuffer.wrap((command+filename).getBytes());
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput();
                    reply = ByteBuffer.allocate(1);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    byte[] a = new byte[1];
                    String code = new String(a);
                    if(code.equals("S")){
                        System.out.println("File successfully deleted");
                    }else if(code.equals("F")){
                        System.out.println("Failed to delete file");
                    }else{
                        System.out.println("Invalid server code received");
                    }
                    break;
                case "L":
                    request = ByteBuffer.wrap((command).getBytes());
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput();
                    reply = ByteBuffer.allocate(1024);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    String sList = new String(reply.array());
                    if(sList.equals("F")){
                        System.out.println("Failed to get list");
                    }else{
                        System.out.println("Operation Successful\n" +
                                "____________________\n"+sList);
                    }
                    break;
                case "R":
                    System.out.println("Please enter the file name: ");
                    String nameOfFile = keyboard.nextLine();
                    System.out.println("Enter the name you wish to name the file: ");
                    String rename = keyboard.nextLine();
                    ByteBuffer renameRequest = ByteBuffer.wrap((command+nameOfFile+","+rename).getBytes());
                    SocketChannel channel2 = SocketChannel.open();
                    channel2.connect(new InetSocketAddress(args[0],serverPort));
                    channel2.write(renameRequest);
                    channel2.shutdownOutput();
                    ByteBuffer reply2 = ByteBuffer.allocate(1);
                    channel2.read(reply2);
                    channel2.close();
                    reply2.flip();
                    byte[] b = new byte[1];
                    reply2.get(b);
                    String code2 = new String(b);
                    if(code2.equals("S")){
                        System.out.println("File successfully renamed");
                    }else if(code2.equals("F")){
                        System.out.println("Failed to rename file");
                    }else{
                        System.out.println("Invalid server code received");
                    }
                    break;
                case "U":
                    SocketChannel channel1 = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    System.out.println("Please enter a file name to upload: ");
                    String nameOfFiletoUpload = keyboard.nextLine();
                    FileInputStream fis = new FileInputStream("ClientFiles/"+nameOfFiletoUpload);
                    byte[] data = new byte[1024];
                    int bytesRead = 0;

                    int nameLength = nameOfFiletoUpload.length();

                    ByteBuffer buffer1 = ByteBuffer.wrap((command + nameLength + nameOfFiletoUpload).getBytes());
                    channel1.connect(new InetSocketAddress(args[0], 3001));
                    channel.write(buffer1);
                    while((bytesRead=fis.read(data)) != -1) {
                        ByteBuffer buffer = ByteBuffer.wrap(data, 0, bytesRead);
                        channel1.write(buffer);
                    }
                    fis.close();
                    channel.shutdownOutput();
                    ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
                    bytesRead = channel.read(replyBuffer);
                    replyBuffer.flip();
                    byte[] h = new byte[bytesRead];
                    replyBuffer.get(h);
                    String status = new String(h);
                    if(status.equals("S")){
                        System.out.println("File successfully uploaded");
                    }else if(status.equals("F")){
                        System.out.println("Failed to upload file");
                    }else{
                        System.out.println("Invalid server code received");
                    }
                    channel.close();
                    channel1.close();
                    break;
                case "D":
                    System.out.println("Please enter the file name: ");
                    String downloadFile = keyboard.nextLine();
                    ByteBuffer downloadRequest = ByteBuffer.wrap((command+downloadFile).getBytes());
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0],serverPort));
                    channel.write(downloadRequest);
                    channel.shutdownOutput();

                    ByteBuffer statusBuffer = ByteBuffer.allocate(1);
                    channel.read(statusBuffer);
                    statusBuffer.flip();
                    byte[] found = new byte[1];
                    statusBuffer.get(found);
                    statusBuffer.clear();
                    String fileStatus = new String(found);
                    if(fileStatus.equals("S")){
                        System.out.print("File Found");
                        FileOutputStream downloadedFile = new FileOutputStream("ClientFiles/"+downloadFile);
                        ByteBuffer downloadedFileReply = ByteBuffer.allocate(1024);
                        int bytesRead2;
                        while((bytesRead2 = channel.read(downloadedFileReply)) != -1) {
                            if (downloadedFileReply.position() < downloadedFileReply.limit()) {
                                downloadedFileReply.flip();
                                byte[] n = new byte[bytesRead2];
                                downloadedFileReply.get(n);
                                downloadedFile.write(n);
                                downloadedFileReply.clear();
                                break;
                            }
                            downloadedFileReply.flip();
                            byte[] n = new byte[bytesRead2];
                            downloadedFileReply.get(n);
                            downloadedFile.write(n);
                            downloadedFileReply.clear();
                        }
                        File filePath = new File("ClientFiles/"+downloadFile);
                        if(filePath.exists()){
                            System.out.println("Downloaded Successfully");
                        }else{
                            System.out.println("Download Failed");
                        }
                    }else{
                        System.out.println("File not found");
                    }
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }while(!command.equals("Q"));
    }

}
