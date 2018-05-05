package Model;

import Client.Client;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Block {
    static ArangoDB arangoDB;
    static Block instance = new Block();
    static String dbName = "scalable";
    static String collectionName = "block";

    private Block(){
        arangoDB = new ArangoDB.Builder().build();
    }

    public static Block getInstance(){
        return Block.instance;
    }

    public void setDB(int i){
        arangoDB = new ArangoDB.Builder().maxConnections(i).build();
    }
    //Gets channels' info of blocked IDs by user ID
    public static String getBlockByID(int id) {

        JSONObject subscriptionObjectM = new JSONObject();
        JSONArray subscriptionArray = new JSONArray();
        String subs = "";
        //Read Document

        try {
            System.out.println("in try of get " + id);
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            System.out.println("document: " + myDocument);
            ArrayList<Integer> ids = new ArrayList<>();
            ids = (ArrayList) myDocument.getAttribute("Block_ID");
            System.out.println("ids " + ids);
            for (int i = 0; i < ids.size(); i++) {
                try {
                    BaseDocument myDocument2 = arangoDB.db(dbName).collection("channel").getDocument("" + ids.get(i),
                            BaseDocument.class);
                    JSONObject subscriptionObject = new JSONObject();

                    subscriptionObject.put("channel_id",id);
                    subscriptionObject.put("info",myDocument2.getAttribute("info"));
                    subscriptionObject.put("subscriptions",myDocument2.getAttribute("subscriptions"));
                    subscriptionObject.put("watched_videos",myDocument2.getAttribute("watched_videos"));
                    subscriptionObject.put("blocked_channels",myDocument2.getAttribute("blocked_channels"));
                    subscriptionObject.put("notifications",myDocument2.getAttribute("notifications"));

                    subscriptionArray.add(subscriptionObject);
                }
                catch (ArangoDBException e) {
                    Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
                    System.err.println("Failed to get document: myKey; " + e.getMessage());
                }
            }
            subscriptionObjectM.put(id,subscriptionArray);
        } catch (ArangoDBException e) {
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        System.out.println(subscriptionObjectM.toString());
        return subscriptionObjectM.toString();

    }

    //Add a new channel blocked by requester ID of blocked ID (add to array of IDs)
    public static String postBlockByID(int id, int blockID){
        System.out.println("in post");
        ArrayList<Integer> ids = new ArrayList<>();
        //Create Document
        if(arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class) == null) {
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(id+"");
            ids.add(blockID);
            myObject.addAttribute("Block_ID", ids);
            try {
                arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
                System.out.println("Document created");
            } catch (ArangoDBException e) {
                Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to create document " + e.getMessage(), CharsetUtil.UTF_8));
                System.err.println("Failed to create document. " + e.getMessage());
            }
            return true+"";
        }
        else{
            BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            System.out.println("myDoc 2: " + myDocument2);
            ids = (ArrayList) myDocument2.getAttribute("Block_ID");
            ids.add(blockID);
            myDocument2.updateAttribute("Block_ID",ids);
            arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
            return true+"";
        }
    }

    public static String deleteBlockByID(int id, int blockID){
        ArrayList<Long> ids = new ArrayList<>();
        //Delete sub from Document
        //Case 1: not the only subscription
        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class);
        ids.addAll((ArrayList<Long>)myDocument2.getAttribute("Block_ID"));
        ids.remove(Long.valueOf(blockID));
        myDocument2.updateAttribute("Block_ID",ids);
        arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
        arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);

        return true+"";
    }
}
