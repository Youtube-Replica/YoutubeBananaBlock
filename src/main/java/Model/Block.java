package Model;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Block {

    //Gets channels' info of blocked IDs by user ID
    public static String getBlockByID(int id) {
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "Block";
        JSONObject subscriptionObjectM = new JSONObject();
        JSONArray subscriptionArray = new JSONArray();
        String subs = "";
        //Read Document
        //haygeely id harod b list ids
        try {
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            ArrayList<Integer> ids = new ArrayList<>();
            ids = (ArrayList) myDocument.getAttribute("Block_ID");

            for (int i = 0; i < ids.size(); i++) {
                try {
                    BaseDocument myDocument2 = arangoDB.db(dbName).collection("Channels").getDocument("" + ids.get(i),
                            BaseDocument.class);
                    JSONObject subscriptionObject = new JSONObject();
                    subscriptionObject.put("Name",myDocument2.getAttribute("Name"));
                    subscriptionObject.put("Category",myDocument2.getAttribute("Category"));
                    subscriptionObject.put("Profile Picture",myDocument2.getAttribute("ProfilePicture"));
                    subscriptionArray.add(subscriptionObject);
                }
                catch (ArangoDBException e) {
                    System.err.println("Failed to get document: myKey; " + e.getMessage());
                }
            }
            subscriptionObjectM.put(id,subscriptionArray);
                } catch (ArangoDBException e) {
                    System.err.println("Failed to get document: myKey; " + e.getMessage());
                }
                System.out.println(subscriptionObjectM.toString());
                return subscriptionObjectM.toString();


    }
    //Add a new channel blocked by requester ID of blocked ID (add to array of IDs)
    public static String postBlockByID(int id, int blockID){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "Block";

        ArrayList<Integer> ids = new ArrayList<>();
        //Create Document
        if(arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class) == null) {
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(id+"");
            myObject.addAttribute("Block_ID", blockID);
            try {
                arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
                System.out.println("Document created");
            } catch (ArangoDBException e) {
                System.err.println("Failed to create document. " + e.getMessage());
            }
            return true+"";
        }
        else{
            BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            ids = (ArrayList) myDocument2.getAttribute("Block_ID");
            ids.add(blockID);
            myDocument2.updateAttribute("Block_ID",ids);
            arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
            return true+"";
        }
    }

    public static String deleteBlockByID(int id, int blockID){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "Block";

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
