package commands;

import Model.Block;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class DeleteBlock extends Command {
    public static int id = 0;
    public static int blockID = 0;
    public void execute() {
        HashMap<String, Object> props = parameters;
        System.out.println(parameters);
        Channel channel = (Channel) props.get("channel");
        JSONParser parser = new JSONParser();
        id = 0;
        blockID = 0;
        try {
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            System.out.println("body: " + body.toString());
            JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
            System.out.println("params" + params);
            id = Integer.parseInt(params.get("requester_id").toString());
            System.out.println("id" + id);
            blockID = Integer.parseInt(params.get("blocked_id").toString());
            System.out.println("sub id"+blockID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
        String response = Block.deleteBlockByID(id,blockID); //Gets channels subscribed by id
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
