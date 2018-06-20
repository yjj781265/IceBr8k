package app.jayang.icebr8k.Utility;

import com.google.android.gms.common.util.NumberUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserQA;

public class Compatability {
    private ArrayList<UserQA> baseList;
    private ArrayList<UserQA> user2List;
    private  Integer score =0,commonQ =0;
    private ArrayList<UserQA> commonList = new ArrayList<>();
    private ArrayList<UserQA> diffList= new ArrayList<>();
    private ArrayList<UserQA> diffList2= new ArrayList<>();

    public Compatability(ArrayList<UserQA> baseList, ArrayList<UserQA> user2List) {
        this.baseList = baseList;
        this.user2List = user2List;

    }

    public Integer getCommonQ() {
        return commonQ;
    }

    private void setCommonQ(Integer commonQ) {
        this.commonQ = commonQ;
    }

    public Integer getScore() {

        // find common questions
        ArrayList<String> baseQuestionIdList = new ArrayList<>();
        ArrayList<String> commonQuestionIdList = new ArrayList<>();

        // user1 qID
        for(int i =0; i<baseList.size();i++){
            baseQuestionIdList.add(baseList.get(i).getQuestionId());
        }
        // user2 qID
        for(int i =0; i<user2List.size();i++){
            commonQuestionIdList.add(user2List.get(i).getQuestionId());
        }
        commonQuestionIdList.retainAll(baseQuestionIdList);
        commonQ= commonQuestionIdList.size();
        setCommonQ(commonQ);



            // compare answers within same question
            for(UserQA userQA : baseList){
                if(commonQuestionIdList.contains(userQA.getQuestionId())){
                    UserQA userQA1 = userQA;


                    for(UserQA user2QA : user2List){
                        if(user2QA.getQuestionId().equals(userQA1.getQuestionId())) {
                            UserQA userQA2 = user2QA;

                            if(userQA1.equals(userQA2)){
                                commonList.add(userQA2);
                            }else{
                                diffList.add(userQA1);
                                diffList2.add(userQA2);
                            }
                        }
                    }
                }
            }
            float num = (float)commonList.size()/(float)commonQ;
            score = Math.round (num*100);





        return score;
    }

    public ArrayList<UserQA> getCommonList() {
        return commonList;
    }

    public ArrayList<UserQA> getDiffList() {
        return diffList;
    }

    public ArrayList<UserQA> getDiffList2() {
        return diffList2;
    }






    // compare algorithm secret
    /*


     public void compareWithUser2(final String user2Uid) {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if( !"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                        userQA1.add(child.getValue(UserQA.class));
                    }

                }


                mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            if(!"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                                userQA2.add(child.getValue(UserQA.class));
                            }


                        }

                        Compatability mCompatability = new Compatability(userQA1,userQA2);
                        setScoreNode(user2Uid,mCompatability.getScore().toString());



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
     */
}
