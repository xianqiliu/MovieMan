package fr.isep.ii3510.movieman.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MovieCollections {
    public static boolean ifCollected = false;
    public static HashMap<String, Object> toSeeMap = new HashMap<>();
    public static HashMap<String, Object> haveSeenMap = new HashMap<>();
    // Reverse keys and values of toSeeMap.
    public static HashMap<String, String> toSeeMapRe = new HashMap<>();
    public static HashMap<String, String> haveSeenMapRe = new HashMap<>();

    /*
        Collect toSee and haveSeen data from database
     */
    public static void GetCollections (){
        if(ifCollected){
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference docRef = db.collection(user.getUid());
        docRef.document("toSee").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        toSeeMap = (HashMap<String, Object>) document.getData();
                        docRef.document("haveSeen").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        haveSeenMap = (HashMap<String, Object>) document.getData();
                                        ifCollected = true;
                                        ReverseMap(toSeeMap, toSeeMapRe);
                                        ReverseMap(haveSeenMap, haveSeenMapRe);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    private static void ReverseMap(HashMap<String, Object> hm, HashMap<String, String> hmRe) {
        for(String i : hm.keySet()){
            if(!i.equals("num")) {
                hmRe.put(String.valueOf(hm.get(i)), i);
            }
        }
    }

}
