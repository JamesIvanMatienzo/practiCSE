package com.jigen.practicse.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

	private val gson = Gson()
	private val listType = object : TypeToken<List<String>>() {}.type

	@TypeConverter
	fun fromOptions(options: List<String>?): String {
		return gson.toJson(options.orEmpty())
	}

	@TypeConverter
	fun toOptions(serializedOptions: String?): List<String> {
		if (serializedOptions.isNullOrBlank()) {
			return emptyList()
		}

		return runCatching {
			gson.fromJson<List<String>>(serializedOptions, listType).orEmpty()
		}.getOrDefault(emptyList())
	}
}
