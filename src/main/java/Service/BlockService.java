package Service;

import Client.Client;
import Commands.Command;
import Model.Block;
import com.rabbitmq.client.*;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class BlockService extends ServiceInterface {
    private static final String RPC_QUEUE_NAME = "block-request";

    public void run() {

        //initialize thread pool of fixed size
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Information> [x] Awaiting RPC requests ", CharsetUtil.UTF_8));
            System.out.println(" [x] Awaiting RPC requests");

            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Information> Responding to corrID: "+ properties.getCorrelationId(), CharsetUtil.UTF_8));
                    System.out.println("Responding to corrID: "+ properties.getCorrelationId());

                    String response = "";

                    try {
                        String message = new String(body, "UTF-8");
                        Command cmd = (Command) Class.forName("commands."+getCommand(message)).newInstance();
                        HashMap<String, Object> props = new HashMap<String, Object>();
                        props.put("channel", channel);
                        props.put("properties", properties);
                        props.put("replyProps", replyProps);
                        props.put("envelope", envelope);
                        props.put("body", message);

                        cmd.init(props);
                        executor.submit(cmd);
                    } catch (RuntimeException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Runtime " + e.getMessage(), CharsetUtil.UTF_8));
                        System.out.println(" [.] " + e.toString());
                    } catch (IllegalAccessException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> IllegalAccessException " + e.getMessage(), CharsetUtil.UTF_8));
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> ParseException " + e.getMessage(), CharsetUtil.UTF_8));
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> InstantiationException " + e.getMessage(), CharsetUtil.UTF_8));
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> ClassNotFoundException " + e.getMessage(), CharsetUtil.UTF_8));
                        e.printStackTrace();
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };

            consumerTag = channel.basicConsume(RPC_QUEUE_NAME, true, consumer);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    public static String getCommand(String message) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject messageJson = (JSONObject) parser.parse(message);
        String result = messageJson.get("command").toString();
        return result;
    }

    @Override
    public void setDB(int dbCount) {
        Block.getInstance().setDB(dbCount);
    }


}
