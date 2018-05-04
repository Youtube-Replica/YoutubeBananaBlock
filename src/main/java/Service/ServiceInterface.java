package Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ServiceInterface {

    protected ThreadPoolExecutor executor;
    protected Consumer consumer;
    protected String consumerTag;
    protected Channel channel;
    String RPC_QUEUE_NAME = "block-request";
    public abstract void run();

    public void setThread(int thread){
        executor.setMaximumPoolSize(thread);
    }

    public abstract void setDB(int dbCount);

    public static void addCommand(String readFile, String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(readFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter("target/classes/Commands/"+path));
            String line = "";
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
            reader.close();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateCommand(String path, String readFile, String writeFile){
        try {
            Files.deleteIfExists(Paths.get("target/classes/Commands/"+path));
            BufferedReader reader = new BufferedReader(new FileReader(readFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter("target/classes/Commands/"+writeFile));
            String line = "";
            while ((line = reader.readLine()) != null) {

                writer.write(line + '\n');
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteCommand(String path){
        try {
            Files.deleteIfExists(Paths.get("target/classes/Commands/"+path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void freeze(){
        try {
            channel.basicCancel(consumerTag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void resume(){
        try {
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void error(){

    }

}
