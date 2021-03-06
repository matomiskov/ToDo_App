package com.matomiskov.todoapp;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {

    private List<Todo> todoList;
    private List<Todo> todoListFull;
    private RecyclerViewAdapter.ClickListener clickListener;
    private Context context;

    public RecyclerViewAdapter(ClickListener clickListener, Context context) {
        this.clickListener = clickListener;
        todoList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_layout, parent, false);
        RecyclerViewAdapter.ViewHolder viewHolder = new RecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.txtName.setText(todo.name);
        holder.txtDesc.setText(todo.description);
        String sCategory = "";
        switch (todo.category){
            case "1": sCategory = context.getString(R.string.household);
                break;
            case "2": sCategory = context.getString(R.string.work);
                break;
            case "3": sCategory = context.getString(R.string.other);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + todo.category);
        }
        holder.txtCategory.setText(sCategory);
        holder.txtDate.setText(todo.date);
        holder.txtTime.setText(todo.time);

    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Todo> filteredList = new ArrayList<>();

            if(charSequence == null || charSequence.length() == 0){
                filteredList.addAll(todoListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Todo todo : todoListFull){
                    if(todo.name.toLowerCase().contains(filterPattern)){
                        filteredList.add(todo);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            todoList.clear();
            todoList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public void updateTodoList(List<Todo> data) {
        todoList.clear();
        todoList.addAll(data);
        todoListFull = data;
        notifyDataSetChanged();
    }

    public void addRow(Todo data) {
        todoList.add(data);
        todoListFull.add(data);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName;
        public TextView txtDesc;
        public TextView txtCategory;
        public TextView txtDate;
        public TextView txtTime;
        public CardView cardView;

        public ViewHolder(View view) {
            super(view);

            txtName = view.findViewById(R.id.txtName);
            txtDesc = view.findViewById(R.id.txtDesc);
            txtCategory = view.findViewById(R.id.txtCategory);
            txtDate = view.findViewById(R.id.txtDate);
            txtTime = view.findViewById(R.id.txtTime);
            cardView = view.findViewById(R.id.cardView);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.launchIntent(todoList.get(getAdapterPosition()).todo_id);
                }
            });
        }
    }

    public Todo getTodo(int i){
        return todoList.get(i);
    }

    public void removeRow(Todo data) {
        todoList.remove(data);
        todoListFull.remove(data);
        notifyDataSetChanged();
    }

    public void removeAllRows(){
        todoList.clear();
        todoListFull.clear();
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void launchIntent(int id);
    }
}