package com.familycon.invite;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by incred-dev on 6/7/18.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>{

    private List<ContactModel>  mContactModelList;
    private InviteActivity      mActivity;

    public ContactAdapter(InviteActivity activity, List<ContactModel> contactModelList) {
        this.mContactModelList = contactModelList;
        this.mActivity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ContactModel model = mContactModelList.get(position);
        if (model != null){
            if (model.getName() != null){
                holder.name.setText(model.getName());
            }

            if (model.getNumber() != null){
                final String number = model.getNumber().get(0);

                holder.butInvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.setInviteSMS(number);
                    }
                });

            }

            /*if (model.getNumber() != null){
                StringBuffer buffer = new StringBuffer();
                for (String number:model.getNumber()){
                    buffer.append(number).append("\n");
                }
                holder.number.setText(buffer);
            }*/
        }
    }

    @Override
    public int getItemCount() {
        return mContactModelList.size();
    }

    public void removeAll() {
        mContactModelList.clear();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, number;
        Button   butInvite;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvName);
            //number = itemView.findViewById(R.id.tvNumber);
            butInvite = itemView.findViewById(R.id.list_item_invite);
        }
    }
}
