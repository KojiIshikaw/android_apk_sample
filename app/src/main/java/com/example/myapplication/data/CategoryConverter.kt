package com.example.myapplication.data

import androidx.room.TypeConverter
import com.example.myapplication.Category

class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(name: String): Category {
        return Category.valueOf(name)
    }
} 