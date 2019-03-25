package my.test.nio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8899));

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (selectionKey.isConnectable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    if (client.isConnectionPending()) {
                        client.finishConnect();

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put((LocalTime.now() + " --- 建立连接").getBytes());
                        buffer.flip();
                        client.write(buffer);

                        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (true) {
                                        InputStreamReader reader = new InputStreamReader(System.in);
                                        BufferedReader bufferedReader = new BufferedReader(reader);
                                        String msg = bufferedReader.readLine();
                                        buffer.clear();
                                        buffer.put(msg.getBytes());
                                        buffer.flip();
                                        client.write(buffer);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = client.read(buffer);
                    if (count > 0) {
                        String msg = new String(buffer.array(), 0, count);
                        System.out.println(msg);

                    }
                }
                selectionKeys.clear();
            }
        }
    }
}
