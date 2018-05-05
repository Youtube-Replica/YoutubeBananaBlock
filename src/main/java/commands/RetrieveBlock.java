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

public class RetrieveBlock extends Command {
   public static int id = 0;
   public void execute() {
       HashMap<String, Object> props = parameters;

       Channel channel = (Channel) props.get("channel");
       JSONParser parser = new JSONParser();
       id = 0;
       try {
           System.out.println(props);
           JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
           System.out.println(body.toString());
           JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
           id = Integer.parseInt(params.get("requester_id").toString());
       } catch (ParseException e) {
           e.printStackTrace();
       }
       AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
       AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
       Envelope envelope = (Envelope) props.get("envelope");
       String response = Block.getBlockByID(id); //Gets channels subscribed by id
       try {
           channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
