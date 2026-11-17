package com.example.assignment2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private Context context;

    public ContactAdapter(List<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact_card, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.textName.setText(contact.getName());
        holder.textNumber.setText(contact.getNumber());
        holder.imageAvatar.setImageResource(contact.getAvatarResId());

        holder.itemView.setOnClickListener(v -> {
            int visibility = holder.actionLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            holder.actionLayout.setVisibility(visibility);
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            context.startActivity(intent);
        });

        holder.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact.getNumber()));
            context.startActivity(intent);
        });

        holder.btnDial.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.getNumber()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return contacts.size(); }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textName, textNumber;
        LinearLayout actionLayout;
        ImageButton btnEdit, btnMessage, btnDial;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            textName = itemView.findViewById(R.id.text_name);
            textNumber = itemView.findViewById(R.id.text_number);
            actionLayout = itemView.findViewById(R.id.layout_actions);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnMessage = itemView.findViewById(R.id.btn_message);
            btnDial = itemView.findViewById(R.id.btn_dial);
        }
    }
}