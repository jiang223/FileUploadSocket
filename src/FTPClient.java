import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

// 断点续传客户端  
public class FTPClient {

    /**
     *  request:get0startIndex0 
     *  response:fileLength0fileBinaryStream 
     *
     * @param filepath
     * @throws Exception
     */
    public void Get(String filepath) throws Exception {
        Socket socket = new Socket();
        // 建立连接  
        socket.connect(new InetSocketAddress("127.0.0.1", 8888));
        // 获取网络流  
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        // 文件传输协定命令  
        byte[] cmd = "get".getBytes();
        out.write(cmd);
        out.write(0);// 分隔符  
        int startIndex = 0;
        // 要发送的文件  
        File file = new File(filepath);
        if(file.exists()){
            startIndex = (int) file.length();
        }
        System.out.println("Client startIndex : " + startIndex);
        // 文件写出流  
        RandomAccessFile access = new RandomAccessFile(file,"rw");
        // 断点  
        out.write(String.valueOf(startIndex).getBytes());
        out.write(0);
        out.flush();
        // 文件长度  
        int temp = 0;
        StringWriter sw = new StringWriter();
        while((temp = in.read()) != 0){
            sw.write(temp);
            sw.flush();
        }
        int length = Integer.parseInt(sw.toString());
        System.out.println("Client fileLength : " + length);
        // 二进制文件缓冲区  
        byte[] buffer = new byte[1024*10];
        // 剩余要读取的长度  
        int tatol = length - startIndex;
        //  
        access.skipBytes(startIndex);
        while (true) {
            // 如果剩余长度为0则结束  
            if (tatol == 0) {
                break;
            }
            // 本次要读取的长度假设为剩余长度  
            int len = tatol;
            // 如果本次要读取的长度大于缓冲区的容量  
            if (len > buffer.length) {
                // 修改本次要读取的长度为缓冲区的容量  
                len = buffer.length;
            }
            // 读取文件，返回真正读取的长度  
            int rlength = in.read(buffer, 0, len);
            // 将剩余要读取的长度减去本次已经读取的  
            tatol -= rlength;
            // 如果本次读取个数不为0则写入输出流，否则结束  
            if (rlength > 0) {
                // 将本次读取的写入输出流中  
                access.write(buffer, 0, rlength);
            } else {
                break;
            }
            System.out.println("finish : " + ((float)(length -tatol) / length) *100 + " %");
        }
        System.out.println("finished!");
        // 关闭流  
        access.close();
        out.close();
        in.close();
    }

    public static void main(String[] args) {
        FTPClient client = new FTPClient();
        try {
            client.Get("E:\\jdk8.rar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}