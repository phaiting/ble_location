package com.htpu.bletracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.htpu.bletracker.data.Ble;
import com.htpu.bletracker.databinding.BleItemBinding;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.google.common.base.Preconditions.checkNotNull;


public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.BleItemViewHolder> {
    private final String TAG = RecyclerListAdapter.class.getSimpleName();
    private final List<Ble> mItems;
    private final Set<String> mIds;
    private final BleItemListener mListener;
    private final @NonNull Context mContext;

    public RecyclerListAdapter(@NonNull Context context, @NonNull BleItemListener listener) {
        mContext = context;
        mListener = listener;
        mItems = new LinkedList<>();
        mIds = new HashSet<>();
    }

    @Override
    public BleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        BleItemBinding binding = BleItemBinding.inflate(layoutInflater, parent, false);
        return new BleItemViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(BleItemViewHolder holder, int position) {
        Ble item = mItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public synchronized void addItem(@NonNull Ble item) {
        checkNotNull(item);
        if (!mIds.contains(item.getDeviceId())) {
            mItems.add(item);
            mIds.add(item.getDeviceId());
            notifyDataSetChanged();
        }

    }

    public synchronized void clear() {
        mItems.clear();
        mIds.clear();
        notifyDataSetChanged();
    }


    class BleItemViewHolder extends RecyclerView.ViewHolder {
        public final BleItemBinding binding;

        public BleItemViewHolder(BleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ble ble) {
            binding.setData(ble);
            binding.executePendingBindings();
            binding.getRoot().
                    setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int pos = getAdapterPosition();
                            if (pos != NO_POSITION) {
                                mListener.onBleItemClick(mItems.get(pos));
                            }
                        }
                    });
        }
    }
}
