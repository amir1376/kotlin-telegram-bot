package me.ivmg.telegram.entities

import com.google.gson.annotations.SerializedName as Name

data class InputMediaPhoto(
    override val type: String,
    override val media: String,
    override val caption: String?
) : InputMedia()