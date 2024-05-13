package com.example.net.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.net.R
import com.example.net.entity.NetworkDiagnosisEntity
import com.example.net.interfaces.OnNetworkDiagnosisItemClickListener


class NetworkDiagnosisAdapter(
        val mContext: Context,
        val mList: ArrayList<NetworkDiagnosisEntity>
) : BaseMultiItemQuickAdapter<NetworkDiagnosisEntity, BaseViewHolder>(mList) {

    init {
        addItemType(NetworkDiagnosisEntity.ITEM_TYPE, R.layout.item_network_diagnosis)
    }


    private var mOnNetworkDiagnosisItemClickListener: OnNetworkDiagnosisItemClickListener? = null

    fun setOnNetworkDiagnosisItemClickListener(listener: OnNetworkDiagnosisItemClickListener?) {
        mOnNetworkDiagnosisItemClickListener = listener
    }


    override fun onBindViewHolder(
            holder: BaseViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads != null && payloads.size > 0) {
            holder.run {
                setText(R.id.id_tv_content, mList[position].content)
            }
        }
    }


    override fun convert(helper: BaseViewHolder, item: NetworkDiagnosisEntity?) {
        helper.run {
            item?.run {
                if (itemViewType == NetworkDiagnosisEntity.ITEM_TYPE) {
                    if (isShowArrow) {
                        itemView.findViewById<ImageView>(R.id.id_img_arrow).visibility =
                                View.VISIBLE
                    }
                    setText(R.id.id_tv_title, title)
                    setText(R.id.id_tv_content, content)
                    itemView.setOnClickListener {
                        mOnNetworkDiagnosisItemClickListener?.run {
                            onItemClick(position)
                        }
                    }
                }
            }
        }
    }
}