package com.example.petdiary.ui.friends;

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

import com.example.petdiary.Chat;
import com.example.petdiary.ItemTouchHelperCallback;
import com.example.petdiary.Person;
import com.example.petdiary.PersonAdapter;
import com.example.petdiary.R;
import com.example.petdiary.activity.ChatActivity;
import com.example.petdiary.ui.chat.ChatListFragment;
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

public class FriendsListFragment extends Fragment {

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private static final String TAG = "FriendsListFragment";
    ArrayList<Person> personArrayList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.fri_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        final PersonAdapter adapter = new PersonAdapter(getContext());


        database = FirebaseDatabase.getInstance();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        personArrayList = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        if(document.getId().equals(dataSnapshot.getKey())){

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
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            public void onCancelled(DatabaseError databaseError) {}
        };

        DatabaseReference ref = database.getReference("friend").child(user.getUid());
        ref.addChildEventListener(childEventListener);

        recyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(recyclerView);

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button cb = getActivity().findViewById(R.id.CB);
        final Button fb = getActivity().findViewById(R.id.FB);

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cb.setBackgroundResource(R.drawable.button_on);
                fb.setBackgroundResource(R.drawable.button_off);
                NavHostFragment.findNavController(FriendsListFragment.this).navigate(R.id.friends_to_chat);
            }
        });
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


}