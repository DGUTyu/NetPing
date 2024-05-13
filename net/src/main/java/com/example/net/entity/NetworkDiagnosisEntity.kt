package com.example.net.entity

import com.chad.library.adapter.base.entity.MultiItemEntity

data class NetworkDiagnosisEntity(
    val title: String,
    var content: String,
    var isShowArrow: Boolean = false
) : java.io.Serializable, MultiItemEntity {


    companion object {
        const val ITEM_TYPE = 0
    }


    override fun getItemType() = ITEM_TYPE
}
