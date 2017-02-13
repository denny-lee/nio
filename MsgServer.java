package com.mycompany.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

public class MsgServer {

	private int port = 9991;  
    private ServerSocketChannel serverSocketChannel;  
    private Charset charset = Charset.forName("UTF-8");  
    private Selector selector = null;
    
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		MsgServer server = new MsgServer();
		server.init();
		server.service();
	}
	
	public void service() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (selector.select() > 0) {  
			Iterator iterator = selector.selectedKeys().iterator();  
			while (iterator.hasNext()) {  
                SelectionKey key = null;
                try { 
                	key = (SelectionKey) iterator.next();  
                    iterator.remove();  
                    if (key.isAcceptable()) {  
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();  
                        if(serverSocketChannel.equals(ssc)) {
                        	System.out.println("same channel!");
                        }
                        SocketChannel sc = ssc.accept();  
                        System.out.println("�ͻ��˻��ӵĵ�ַ�� "  
                                        + sc.socket().getRemoteSocketAddress()  
                                        + "  �ͻ��˻����ӵĶ˿ں��� "  
                                        + sc.socket().getLocalPort());  
                        sc.configureBlocking(false);  
                        ByteBuffer buffer = ByteBuffer.allocate(1024);  
                        sc.register(selector, SelectionKey.OP_READ , buffer);//bufferͨ��������ʽ������  
                    }  
                    if (key.isReadable()) { 
//                        reveice(key);
                    	SocketChannel sc = (SocketChannel) key.channel();  
                    	Scanner scan = new Scanner(System.in);
                        while(scan.hasNextLine())
                        {
                            String line = scan.nextLine();
                            if("quit".equals(line)) {
                            	break;
                            }
                            sc.write(charset.encode(line));//sc����дҲ�ܶ��������д
                        }
                        scan.close();
                    }  
                } catch (IOException e) {  
                    e.printStackTrace();  
                    try {  
                        if (key != null) {  
                            key.cancel();  
                            key.channel().close();  
                        }  
                    } catch (ClosedChannelException cex) {  
                        e.printStackTrace();  
                    }  
                }  
			}
		}
	}
	
	public void reveice(SelectionKey key) throws IOException {  
        if (key == null)  
            return;  
        //***��SelectionKey.attachment()��ȡ�ͻ�����Ϣ***//  
        //��ͨ��������ʽ����������  
//       ByteBuffer buff = (ByteBuffer) key.attachment();  
        // SocketChannel sc = (SocketChannel) key.channel();  
//       buff.limit(buff.capacity());  
        // buff.position(0);  
        // sc.read(buff);  
        // buff.flip();  
        // String reviceData = decode(buff);  
        // System.out.println("���գ�" + reviceData);  
  
        //***��channel.read()��ȡ�ͻ�����Ϣ***//  
        //������ʱ��Ҫ�����ֽڳ���        
        SocketChannel sc = (SocketChannel) key.channel();  
        StringBuilder content = new StringBuilder();  
        //create buffer with capacity of 48 bytes         
        ByteBuffer buf = ByteBuffer.allocate(3);//java��һ��(utf-8)����3�ֽ�,gbk����ռ2���ֽ�      
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
        System.out.println("[msg] ��" + content.toString());  
        sc.write(encode("���������"));
//        sc.write(encode("please enter msg!"));
        // sc.write(ByteBuffer.wrap(reviceData.getBytes()));  
//      try {  
//          sc.write(ByteBuffer.wrap(new String(  
//                  "���Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ��Բ���" + (++x)).getBytes()));// ����Ϣ���͸��ͻ���  
//      } catch (IOException e1) {  
//          e1.printStackTrace();  
//      }  
    }
	public void init() throws IOException {
		selector = Selector.open();  
        serverSocketChannel = ServerSocketChannel.open();  
        serverSocketChannel.socket().setReuseAddress(true);  
        serverSocketChannel.socket().bind(new InetSocketAddress(port));  
        System.out.println("����������");  
	}
	/* ������� */  
    public ByteBuffer encode(String str) {  
        return charset.encode(str);  
    }  
  
    /* ������� */  
    public String decode(ByteBuffer bb) {  
        return charset.decode(bb).toString();  
    } 
}
