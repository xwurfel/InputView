package configurable.input.fields.presentation.custom.extensions

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.core.content.res.getBooleanOrThrow
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntegerOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow
import java.lang.Exception


fun getDimensionAttribute(attributesArray: TypedArray, index: Int): Float? {
    return try {
        attributesArray.getDimensionOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getPixelAttribute(attributesArray: TypedArray, index: Int): Int? {
    return try {
        attributesArray.getDimensionPixelSizeOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getResourceIdAttribute(attributesArray: TypedArray, index: Int): Int? {
    return try {
        attributesArray.getResourceIdOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getColorAttribute(attributesArray: TypedArray, index: Int): Int? {
    return try {
        attributesArray.getColorOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getIntegerAttribute(attributesArray: TypedArray, index: Int): Int? {
    return try {
        attributesArray.getIntegerOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getDrawableAttribute(attributesArray: TypedArray, index: Int): Drawable? {
    return try {
        attributesArray.getDrawableOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getBooleanAttribute(attributesArray: TypedArray, index: Int): Boolean? {
    return try {
        attributesArray.getBooleanOrThrow(index)
    } catch (e: Exception) {
        null
    }
}

fun getStringAttribute(attributesArray: TypedArray, index: Int): String? {
    return try {
        attributesArray.getStringOrThrow(index)
    } catch (E: Exception) {
        null
    }
}

fun getFloatAttribute(attributesArray: TypedArray, index: Int): Float? {
    return try {
        attributesArray.getFloatOrThrow(index)
    } catch (E: Exception) {
        null
    }
}