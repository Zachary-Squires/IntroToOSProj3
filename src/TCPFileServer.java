import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPFileServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(3000));

        while(true){
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);
            request.flip();
            //size of byte array should match
            byte[] a = new byte[1];
            request.get(a);
            String command = new String (a);
            System.out.println("\nreceived command: "+command);
            switch(command){
                case "E"://delete
                    byte[] b = new byte[request.remaining()];
                    request.get(b);
                    String fileName = new String(b);
                    System.out.println("File to delete: "+fileName);
                    File file = new File("ServerFiles/"+fileName);
                    boolean success = false;
                    if(file.exists()){
                        success = file.delete();
                    }
                    String code;
                    if(success){
                        System.out.println("Deletion Success");
                        code = "S";
                    }else {
                        System.out.println("Deletion Failed");
                        code = "F";
                    }
                    ByteBuffer reply = ByteBuffer.wrap(code.getBytes());
                    serveChannel.write(reply);
                    serveChannel.close();
                    break;
                case "L"://list
                    File fileL = new File("ServerFiles");
                    String[] fileList = fileL.list();
                    String fileString = "";
                    assert fileList != null;
                    for(String s : fileList) {
                        fileString = fileString + s + "\n";
                    }
                    String status;
                    if(fileString.isEmpty()){
                        fileString = "F";
                    }else{
                        ByteBuffer reply2 = ByteBuffer.wrap(fileString.getBytes());
                        serveChannel.write(reply2);
                        serveChannel.close();
                    }
                    System.out.println("_______________________________");

                    break;
                case "R"://rename
                    byte[] name = new byte[request.remaining()];
                    request.get(name);
                    String filesInfo = new String(name);
                    String[] info = filesInfo.split(",");
                    String filename = info[0];
                    String fileRename = info[1];
                    File file2 = new File("ServerFiles/"+filename);
                    File rename = new File("ServerFiles/"+fileRename);
                    boolean complete = false;
                    if(file2.exists()){
                        complete = file2.renameTo(rename);
                    }
                    String successResponse;
                    if(complete){
                        System.out.println("Operation complete, file renamed");
                        successResponse = "S";
                    }else {
                        System.out.println("Operation Failed");
                        successResponse = "F";
                    }
                    ByteBuffer sendBack = ByteBuffer.wrap(successResponse.getBytes());
                    serveChannel.write(sendBack);
                    serveChannel.close();
                    System.out.println("_______________________________");
                    break;
                case "U"://upload
                    byte[] k = new byte[1];
                    request.get(k);
                    byte[] g = new byte[1];
                    request.get(g);
                    StringBuilder upName = new StringBuilder();
                    int len1 = Integer.parseInt(new String(k));
                    int len3;
                    if(isNumeric(new String(g))){
                        int len2 = Integer.parseInt(new String(g));
                        len1 = len1*10;
                        len3 = len1 + len2;
                    }
                    else{
                        len3 = len1;
                        upName.append(new String(g));
                        len3--;
                    }
                    while(len3 > 0){
                        byte[] z = new byte[1];
                        request.get(z);
                        upName.append(new String(z));
                        len3--;
                    }
                    FileOutputStream fos = new FileOutputStream("ServerFiles/"+upName.toString());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead;
                    while ((bytesRead = serveChannel.read(buffer)) != -1) {
                        buffer.flip();
                        byte[] n = new byte[bytesRead];
                        buffer.get(n);
                        fos.write(n);
                        buffer.clear();
                    }
                    fos.close();
                    String replyMessage = "S";
                    ByteBuffer replyBuffer =
                            ByteBuffer.wrap(replyMessage.getBytes());
                    serveChannel.write(replyBuffer);
                    serveChannel.close();
                    break;
                case "D":
                    byte[] downloadBytes = new byte[request.remaining()];
                    request.get(downloadBytes);
                    String downloadFileName = new String(downloadBytes);
                    File filePath = new File("ServerFiles/"+downloadFileName);
                    FileInputStream downloadFile = null;
                    String successfullyFound = " ";
                    if(filePath.exists()) {
                        downloadFile = new FileInputStream("ServerFiles/" + downloadFileName);
                        System.out.println("File Found");
                        successfullyFound = "S";
                        ByteBuffer sends = ByteBuffer.wrap(successfullyFound.getBytes());
                        serveChannel.write(sends);
                    }else{
                        System.out.println("File Not Found");
                        successfullyFound = "F";
                        ByteBuffer sends = ByteBuffer.wrap(successfullyFound.getBytes());
                        serveChannel.write(sends);
                        break;
                    }
                    byte[] data = new byte[1024];
                    int bytesReadD = 0;
                    if(downloadFile!=null) {
                        while ((bytesReadD = downloadFile.read(data)) != -1) {
                            ByteBuffer bufferD = ByteBuffer.wrap(data, 0, bytesReadD);
                            serveChannel.write(bufferD);
                        }
                    }

                    if(bytesReadD!=0){
                        System.out.println("Operation complete, file downloaded");
                    }else {
                        System.out.println("Operation Failed");
                    }
                    System.out.println("_______________________________");
                    break;
                default:
                    System.out.println("Invalid Command");
            }
        }
    }
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}