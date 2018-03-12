import Commands.Command;
import Commands.Delete.DeleteBlock;
import Commands.Get.GetBlock;
import Commands.Post.PostBlock;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class BlockService {
    private static final String RPC_QUEUE_NAME = "block-request";

    public static void main(String [] argv) {

        //initialize thread pool of fixed size
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    System.out.println("Responding to corrID: "+ properties.getCorrelationId());

                    String response = "";

                    try {
                        String message = new String(body, "UTF-8");
                        Command cmd = new GetBlock();
//                        Command cmd1 = new DeleteBlock();
                        //Command cmd1 = new PostBlock();
                        HashMap<String, Object> props = new HashMap<String, Object>();
                        props.put("channel", channel);
                        props.put("properties", properties);
                        props.put("replyProps", replyProps);
                        props.put("envelope", envelope);
                        props.put("body", message);

                        cmd.init(props);
//                        cmd1.init(props);
                        executor.submit(cmd);
//                        executor.submit(cmd1);
                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
