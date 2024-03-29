package com.example.task2.ui.main.Tab1.adapter;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task2.R;
import com.example.task2.ui.main.Tab1.model.ContactList;
import java.util.ArrayList;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    
    private ArrayList<ContactList> items = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListner = null;
    
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListner = listener;
    }
    
    
    public ContactAdapter(Context context) {
        this.mContext = context;
    }
    
    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int i) {
        
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        
        ContactList item = items.get(position);
        if(item.getThumnailld() != null && item.getThumnailld() != 0){
            Bitmap bm = loadContactPhoto(mContext.getContentResolver(), item.getThumnailld());
            if (mContext.getContentResolver() != null && bm != null) {
                items.get(position).setPhoto(bm);
                viewHolder.Picture.setImageBitmap(bm);
            }
        }
        viewHolder.Name.setText(item.getName());
//        viewHolder.Phone_number.setText(item.getPhone_number());
//        viewHolder.Address.setText(item.getAddress());
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void addItem(int position, ContactList contactList) { items.add(position, contactList); }
    
    public void removeItem(int position) { items.remove(position); }
    
    public Bitmap loadContactPhoto(ContentResolver cr, long photo_id) {
        
        byte[] photoBytes = null;
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photo_id);
        Cursor c = cr
            .query(photoUri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null,
                null);
        
        try {
            if (c.moveToFirst()) {
                photoBytes = c.getBlob(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        
        if (photoBytes != null) {
            return resizingBitmap(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));
        }
        
        return null;
    }
    
    public Bitmap resizingBitmap(Bitmap oBitmap) {
        if (oBitmap == null) {
            return null;
        }
        
        float width = oBitmap.getWidth();
        float height = oBitmap.getHeight();
        float resizing_size = 120;
        
        Bitmap rBitmap = null;
        if (width > resizing_size) {
            float mWidth = (float) (width / 100);
            float fScale = (float) (resizing_size / mWidth);
            width *= (fScale / 100);
            height *= (fScale / 100);
            
        } else if (height > resizing_size) {
            float mHeight = (float) (height / 100);
            float fScale = (float) (resizing_size / mHeight);
            
            width *= (fScale / 100);
            height *= (fScale / 100);
        }
        
        rBitmap = Bitmap.createScaledBitmap(oBitmap, (int) width, (int) height, true);
        return rBitmap;
    }
    
    public void setItems(ArrayList<ContactList> items) {
        this.items = items;
    }
    
    public ArrayList<ContactList> getItems() {
        return items;
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        
        ImageView Picture;
        TextView Name;
        Button callBtn;
        CardView cardView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            
            Picture = itemView.findViewById(R.id.list_item_picture);
            
            Name = itemView.findViewById(R.id.list_item_name);
            callBtn = itemView.findViewById(R.id.callBtn);
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        Uri uri = Uri.parse("tel:" + items.get(pos).getPhone_number());
                        Intent intent = new Intent(Intent.ACTION_CALL, uri);
                        mContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            });
            cardView = itemView.findViewById(R.id.cv_item_movie_parent);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        if (mListner != null) {
                            mListner.onItemClick(view, pos);
                        }
                    }
                }
            });
        }
    }
}