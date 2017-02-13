package com.mycompany.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

public class Client {

	private static Charset charset = Charset.forName("UTF-8"); 
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String hostIp = "127.0.0.1";
		int hostListenningPort = 9991; 
		SocketChannel channel = null;
        Selector selector = null;
        selector = Selector.open();  
    	channel = SocketChannel.open();  
        channel.configureBlocking(false);  
        channel.connect(new InetSocketAddress(hostIp, hostListenningPort));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        boolean flag = false;
		try {  
            // 轮询访问selector  
            while (true) {  
            	selector.select();
                Iterator ite = selector.selectedKeys().iterator();  
                while (ite.hasNext()) {  
                	selector.select();//如果队列有新的Channel加入，那么Selector.select()会被唤醒  
                    SelectionKey key = (SelectionKey) ite.next(); 
                    ite.remove();  
                    SocketChannel sc = (SocketChannel) key.channel();  
                    if (key.isConnectable()) { 
                    	if(sc.finishConnect()){
                            key.interestOps(SelectionKey.OP_READ);
                            sc.write(encode("hi~server!"));
                        } else {
                            key.cancel();
                        }
                    } else if (key.isReadable()) {
                    	StringBuilder content = new StringBuilder();  
                        ByteBuffer buf = ByteBuffer.allocate(3);//java里一个(utf-8)中文3字节,gbk中文占2个字节      
                        int bytesRead = sc.read(buf); //read into buffer.  
                        while (bytesRead >0) {  
                          buf.flip();  //make buffer ready for read  
                          while(buf.hasRemaining()){                        
                              buf.get(new byte[buf.limit()]); // read 1 byte at a time    
                              content.append(new String(buf.array(), 0, bytesRead, "UTF-8"));  
                          }                   
                          buf.clear(); //make buffer ready for writing        
                          bytesRead = sc.read(buf);   
                        }  
                        System.out.println("[from server] ：" + content.toString());
                    	/*Scanner scan = new Scanner(System.in);
                        while(scan.hasNextLine())
                        {
                            String line = scan.nextLine();
                            if("quit".equals(line)) {
                            	break;
                            }
                            sc.write(charset.encode(line));//sc既能写也能读，这边是写
                        }
                        scan.close();
                        flag = true;
                        if(sc != null){
                            try {
                            	sc.close();
                            } catch (IOException e) {                      
                                e.printStackTrace();
                            }                  
                        }*/
                    } 
                }  
                if(flag) {
                	break;
                }
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally{
            if(channel != null){
                try {
                    channel.close();
                } catch (IOException e) {                      
                    e.printStackTrace();
                }                  
            }
             
            if(selector != null){
                try {
                	selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	/* 编码过程 */  
    public static ByteBuffer encode(String str) {  
        return charset.encode(str);  
    }  
  
    /* 解码过程 */  
    public static String decode(ByteBuffer bb) {  
        return charset.decode(bb).toString();  
    } 
}
