package com.example.petdiary.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petdiary.R;
import com.example.petdiary.ItemTouchHelperCallback;
import com.example.petdiary.Person;
import com.example.petdiary.PersonAdapter2;
import com.example.petdiary.ui.friends.FriendsListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatListFragment extends Fragment {

    private FirebaseDatabase database;
    private static final String TAG = "ChatListFragment";
    ArrayList<Person> personArrayList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        galleryViewModel =
//                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat, container, false);



        final RecyclerView recyclerView = root.findViewById(R.id.chat_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        final PersonAdapter2 adapter = new PersonAdapter2(getContext());
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        personArrayList = new ArrayList<>();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                String s = dataSnapshot.getKey();

                    if(s.indexOf(user.getUid()+"&") != -1) {
                        final String uid = s.replace(user.getUid()+"&","");
                        System.out.println(uid);

                    db.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            if (document.getId().equals(uid)) {

                                                Person person = dataSnapshot.getValue(Person.class);
                                                personArrayList.add(person);

                                                adapter.addItem(new Person(document.get("nickName").toString(), document.getId()));
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });
                }
            }
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

//        DatabaseReference ref = database.getReference("friend").child(user.getUid());

        DatabaseReference ref = database.getReference("chat");
        ref.addChildEventListener(childEventListener);


        recyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(recyclerView);

        return root;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Button fb = getActivity().findViewById(R.id.FB);
        final Button cb = getActivity().findViewById(R.id.CB);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fb.setBackgroundResource(R.drawable.button_on);
                cb.setBackgroundResource(R.drawable.button_off);
                NavHostFragment.findNavController(ChatListFragment.this).navigate(R.id.chat_to_friends);


            }
        });
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}