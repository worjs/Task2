package com.example.task2.ui.main.Tab1;

import static android.app.Activity.RESULT_OK;
import static com.example.task2.MainActivity.getContextOfApplication;
import static com.example.task2.MainActivity.getGlobalId;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.task2.R;
import com.example.task2.Retrofit.IMyService;
import com.example.task2.Retrofit.RetrofitClient;
import com.example.task2.ui.main.Tab1.adapter.ContactAdapter;
import com.example.task2.ui.main.Tab1.adapter.ContactAdapter.OnItemClickListener;
import com.example.task2.ui.main.Tab1.model.ContactList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Retrofit;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Fragment1 extends Fragment implements View.OnClickListener{
    
    static final int REQUEST_PERMISSION_KEY = 1;
    private static final int ADD_DATA_REQUEST = 2;
    private static final int EDIT_DATA_REQUEST = 3;
    private static final int RESULT_DELETED = 404;
    
    private String global_id;
    
    private RecyclerView recyclerView;
    private ContactAdapter adapter = new ContactAdapter(getContextOfApplication());
    private FloatingActionButton fabContactsAdd, fabContactsSync, fabContactsRefresh, fabContactsMenu;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IMyService iMyService;
    
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    
    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment1, container, false);
        
        global_id = getGlobalId();
        
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);
        
        fab_open = AnimationUtils.loadAnimation(getContextOfApplication(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContextOfApplication(), R.anim.fab_close);
        
        fabContactsAdd = view.findViewById(R.id.fabContactsAdd);
        fabContactsSync = view.findViewById(R.id.fabContactsSync);
        fabContactsRefresh = view.findViewById(R.id.fabContactsRefresh);
        fabContactsMenu = view.findViewById(R.id.fabContactsMenu);
        
        fabContactsAdd.setOnClickListener(this);
        fabContactsRefresh.setOnClickListener(this);
        fabContactsSync.setOnClickListener(this);
        fabContactsMenu.setOnClickListener(this);
        
        String[] PERMISSION_1 = {Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE};
        if (!hasPermissions(getContextOfApplication(), PERMISSION_1)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSION_1, REQUEST_PERMISSION_KEY);
        }
    
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                intent.putExtra("name", adapter.getItems().get(position).getName());
                intent.putExtra("phone", adapter.getItems().get(position).getPhone_number());
                intent.putExtra("email", adapter.getItems().get(position).getAddress());
                intent.putExtra("photo", adapter.getItems().get(position).getThumnailld());
                intent.putExtra("hasPhoto", adapter.getItems().get(position).hasPhoto());
                if(adapter.getItems().get(position).hasPhoto())
                    intent.putExtra("photo", adapter.getItems().get(position).getPhoto());
                intent.putExtra("position", position);
                intent.putExtra("contact_id", adapter.getItems().get(position).getContactid());
                startActivityForResult(intent, EDIT_DATA_REQUEST);
            }
        });
        
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        LoadContactsAsync lca = new LoadContactsAsync();
        lca.execute();
        return view;
    }
    
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null
            && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.fabContactsAdd: {
                anim();
                Intent intent = new Intent(getActivity(), AddContactActivity.class);
                startActivityForResult(intent, ADD_DATA_REQUEST);
                return;
            }
            
            case R.id.fabContactsRefresh: {
                anim();
                refreshContacts();
                return;
            }
            
            case R.id.fabContactsSync: {
                anim();
                syncContacts();
                refreshContacts();
                return;
            }
            
            case R.id.fabContactsMenu: {
                anim();
                return;
            }
        }
    }
    
    public void anim() {
        if(isFabOpen) {
            fabContactsAdd.startAnimation(fab_close);
            fabContactsSync.startAnimation(fab_close);
            fabContactsRefresh.startAnimation(fab_close);
            fabContactsAdd.setClickable(false);
            fabContactsSync.setClickable(false);
            fabContactsRefresh.setClickable(false);
            isFabOpen = false;
        }
        else {
            fabContactsAdd.startAnimation(fab_open);
            fabContactsSync.startAnimation(fab_open);
            fabContactsRefresh.startAnimation(fab_open);
            fabContactsAdd.setClickable(true);
            fabContactsSync.setClickable(true);
            fabContactsRefresh.setClickable(true);
            isFabOpen = true;
        }
    }
    
    class LoadContactsAsync extends AsyncTask<Void, Void, ArrayList<ContactList>> {
        ProgressDialog pd;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            pd = ProgressDialog
                .show(Fragment1.this.getContext(), "Loading Contacts", "Please Wait");
        }
        
        @Override
        protected ArrayList<ContactList> doInBackground(Void... params) {
            return refreshContacts();
        }
        
        @Override
        protected void onPostExecute(ArrayList<ContactList> contacts) {
            super.onPostExecute(contacts);
            
            pd.cancel();
            recyclerView.setAdapter(adapter);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ADD_DATA_REQUEST: {
                if(resultCode == RESULT_OK) {
                    String name, phone, email;
                    
                    name = intent.getStringExtra("name");
                    phone = intent.getStringExtra("phone");
                    email = intent.getStringExtra("email");
                    
                    addContact("", name, phone, email, "0");
                    int position = adapter.getItemCount();
                    adapter.addItem(position, new ContactList(name, phone, email, Long.getLong("0"), ""));
                    adapter.notifyItemInserted(position);
                    return;
                }
            }
            
            case EDIT_DATA_REQUEST: {
                if(resultCode == RESULT_OK) {
                    String initialName, initialPhone, initialEmail, name, phone, email, contact_id;
                    long initialPhoto, photo;
                    int position;
                    
                    initialName = intent.getStringExtra("initialName");
                    initialPhone = intent.getStringExtra("initialPhone");
                    initialEmail = intent.getStringExtra("initialEmail");
                    initialPhoto = intent.getLongExtra("initialPhoto", 0);
                    name = intent.getStringExtra("name");
                    phone = intent.getStringExtra("phone");
                    email = intent.getStringExtra("email");
                    photo = intent.getLongExtra("photo", 0);
                    contact_id = intent.getStringExtra("contact_id");
                    position = intent.getIntExtra("position", 0);
                    
                    deleteContact(contact_id, initialName, initialPhone, initialEmail);
                    addContact("", name, phone, email, String.valueOf(photo));
                    adapter.removeItem(position);
                    adapter.notifyItemRemoved(position);
                    position = adapter.getItemCount();
                    adapter.addItem(position, new ContactList(name, phone, email, Long.getLong("0"), ""));
                    adapter.notifyItemInserted(position);
                    return;
                }
                
                else if(resultCode == RESULT_DELETED) {
                    String contact_id, name, phone, email;
                    long photo;
                    int position;
                    
                    contact_id = intent.getStringExtra("contact_id");
                    name = intent.getStringExtra("name");
                    phone = intent.getStringExtra("phone");
                    email = intent.getStringExtra("email");
                    photo = intent.getLongExtra("photo", 0);
                    position = intent.getIntExtra("position", 0);
                    
                    deleteContact(contact_id, name, phone, email);
                    adapter.removeItem(position);
                    adapter.notifyItemRemoved(position);
                    return;
                }
            }
        }
    }
    
    // 서버로 Contact Entry를 추가하는 요청을 보냄
    private void addContact(String contact_id, String name, String phone, String email, String photo) {
        compositeDisposable.add(iMyService.uploadContact(global_id, contact_id, name, phone, email, photo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String response) throws Exception {
                    //
                }
            }));
    }
    
    // 서버로 지정한 Contact Entry를 삭제하는 요청을 보냄
    private void deleteContact(String contact_id, String name, String phone, String email) {
        compositeDisposable.add(iMyService.deleteContact(global_id, contact_id, name, phone, email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String response) throws Exception {
                    //Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                }
            }));
    }
    
    // 서버로 해당하는 user_id를 가진 모든 Contact Entry를 보내라는 요청을 보냄
    private void downloadContact(final ArrayList<ContactList> contacts) {
        compositeDisposable.add(iMyService.downloadContact(global_id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String response) throws Exception {
                    if(isJSONValid(response)) {
                        JSONArray jArray = new JSONArray(response);
                        for(int i=0; i<jArray.length(); i++) {
                            JSONObject jObject = jArray.getJSONObject(i);
                            String contact_id = jObject.getString("contact_id");
                            String name = jObject.getString("name");
                            String phone_number = jObject.getString("phone_number");
                            String email = jObject.getString("email");
                            long photo = Long.parseLong(jObject.getString("photo"));
                            contacts.add(new ContactList(name, phone_number, email, photo, contact_id));
                        }
                    }
                    adapter.setItems(contacts);
                    adapter.notifyDataSetChanged();
                }
            }));
    }
    
    private void syncContacts() {
        final ArrayList<ContactList> contacts = new ArrayList<>();
        Uri uri_phone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_ID,
            ContactsContract.Contacts._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        };
    
        Cursor c_phone = getActivity().getContentResolver()
            .query(uri_phone, projection, null, null, null);
        assert c_phone != null;
        while (c_phone.moveToNext()) {
            long photo_id = c_phone.getLong(2);
            long person = c_phone.getLong(3);
            String contactName = c_phone.getString(1);
            String phNumber = c_phone.getString(0);
            String contact_id = c_phone.getString(4);
    
            Uri uri_email = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            Cursor ce = getActivity().getContentResolver()
                .query(uri_email, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{contact_id}, null);
            String email = "";
            if (ce != null && ce.moveToFirst()) {
                email = ce
                    .getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                ce.close();
            }
            addContact(contact_id, contactName, phNumber, email, String.valueOf(photo_id));
        }
    }
    
    // server에 저장된 연락처를 불러와 adapter에 적용시킴
    private ArrayList<ContactList> refreshContacts() {
        ArrayList<ContactList> contacts = new ArrayList<>();
        downloadContact(contacts);
        return contacts;
    }
    
    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
