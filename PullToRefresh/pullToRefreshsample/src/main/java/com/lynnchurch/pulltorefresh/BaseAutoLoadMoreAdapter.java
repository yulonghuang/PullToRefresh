package com.lynnchurch.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jingchen.pulltorefresh.WrapRecyclerView;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.ArrayList;

/**
 * 自动加载更多适配器
 */
public abstract class BaseAutoLoadMoreAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final String TAG = BaseAutoLoadMoreAdapter.class.getSimpleName();
    protected Context mContext;
    protected ArrayList<T> mData;
    private OnItemClickListener mOnItemClickListener;
    private OnLoadmoreListener mOnLoadmoreListener;
    private int mLastPosition; // 正常项最后一项的位置
    private View mParentView;
    private int mParentHeight; // item父视图的高度
    private int mItemsHeight; // 所有item的总高度
    private TextView tv_hint;
    private CircleProgressBar progressBar;


    public BaseAutoLoadMoreAdapter(Context context, WrapRecyclerView recyclerView, ArrayList<T> data)
    {
        mContext = context;
        initFooter(recyclerView);
        mData = data;
        mLastPosition = mData.size() - 1;
    }

    private void initFooter(WrapRecyclerView recyclerView)
    {
        View footer = LayoutInflater.from(mContext).inflate(R.layout.loadmore, null);
        tv_hint = (TextView) footer.findViewById(R.id.tv_hint);
        progressBar = (CircleProgressBar) footer.findViewById(R.id.progressBar);
        recyclerView.addFootView(footer);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        mParentView = parent;
        mParentHeight = parent.getHeight();
        return onCreateNormalViewHolder(parent, viewType);
    }

    public abstract NormalViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        holder.itemView.measure(0, 0);
        mItemsHeight += holder.itemView.getMeasuredHeight();
        if (holder instanceof NormalViewHolder)
        {
            final NormalViewHolder viewHolder = (NormalViewHolder) holder;
            if (null != mOnItemClickListener)
            {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mOnItemClickListener.onItemClick(viewHolder.itemView, position);
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        mOnItemClickListener.onItemLongClick(viewHolder.itemView, position);
                        return true;
                    }
                });
            }
            onBindNormalViewHolder(viewHolder, position);
        } else
        {
            if (!isOverParent())
            {
                showLoading(false);
            }
        }

        if (position + 1 > mLastPosition && null != mOnLoadmoreListener && isOverParent())
        {
            showLoading(true);
            mOnLoadmoreListener.onLoadmore();
            mLastPosition = mData.size();
        }
    }

    public abstract void onBindNormalViewHolder(BaseAutoLoadMoreAdapter.NormalViewHolder holder, final int position);

    /**
     * item是否撑满父视图
     *
     * @return
     */
    public boolean isOverParent()
    {
        return mParentHeight < mItemsHeight ? true : false;
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }


    public static class NormalViewHolder extends RecyclerView.ViewHolder
    {
        View itemView;

        public NormalViewHolder(View v)
        {
            super(v);
            itemView = v;
        }
    }

    /**
     * 当加载失败
     */
    public void onFailed()
    {
        progressBar.setVisibility(View.GONE);
        tv_hint.setVisibility(View.VISIBLE);
        tv_hint.setText("点击重试");
        tv_hint.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mOnLoadmoreListener)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    tv_hint.setVisibility(View.GONE);
                    mOnLoadmoreListener.onLoadmore();
                }
            }
        });
    }


    /**
     * 当没有更多
     */
    public void onNothing()
    {
        progressBar.setVisibility(View.GONE);
        tv_hint.setVisibility(View.VISIBLE);
        tv_hint.setText("没有更多");
        tv_hint.setOnClickListener(null);
    }

    /**
     * 显示加载视图
     *
     * @param show
     */
    private void showLoading(boolean show)
    {
        if (show)
        {
            progressBar.setVisibility(View.VISIBLE);
        } else
        {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 当数据改变时调用,替换notifyDataSetChanged()
     */
    public void notifyDataChanged()
    {
        notifyDataSetChanged();
        mParentView.requestLayout();
    }

    /**
     * 设置Item监听器
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * Item点击监听
     */
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    /**
     * 设置加载更多监听
     *
     * @param listener
     */
    public void setOnLoadmoreListener(OnLoadmoreListener listener)
    {
        mOnLoadmoreListener = listener;
    }

    /**
     * 加载更多监听
     */
    public interface OnLoadmoreListener
    {
        void onLoadmore();
    }


}