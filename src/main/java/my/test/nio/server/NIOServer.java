package my.test.nio.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NIOServer {

    private static final Map<String, SocketChannel> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8899));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                final SocketChannel client;
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                    client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                    String key = UUID.randomUUID().toString();

                    clients.put(key, client);
                } else if (selectionKey.isReadable()) {
                    client = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = client.read(buffer);
                    if (count > 0) {
                        buffer.flip();
                        String msg = new String(Charset.forName("utf-8").decode(buffer).array());
                        System.out.println(client + ": " + msg);
                        String key = "";
                        for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                            SocketChannel c = entry.getValue();
                            if (c == client) {
                                key = entry.getKey();
                                break;
                            }
                        }
                        for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                            writeBuffer.put((key + ": " + msg).getBytes());
                            writeBuffer.flip();
                            entry.getValue().write(writeBuffer);
                        }
                    }
                }
            }
            selectionKeys.clear();
        }
    }
}
