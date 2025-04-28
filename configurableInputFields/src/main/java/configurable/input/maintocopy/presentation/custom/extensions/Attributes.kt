package configurable.input.maintocopy.presentation.custom.extensions


import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.core.content.res.*

internal fun TypedArray.getDimensionOrNull(index: Int): Float? {
    return try {
        getDimensionOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getPixelSizeOrNull(index: Int): Int? {
    return try {
        getDimensionPixelSizeOrThrow(index)
    } catch (_: Exception) {
        null
    }
}


internal fun TypedArray.getResourceIdOrNull(index: Int): Int? {
    return try {
        getResourceIdOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getColorOrNull(index: Int): Int? {
    return try {
        getColorOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getIntegerOrNull(index: Int): Int? {
    return try {
        getIntegerOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getDrawableOrNull(index: Int): Drawable? {
    return try {
        getDrawableOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getBooleanOrNull(index: Int): Boolean? {
    return try {
        getBooleanOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getStringOrNull(index: Int): String? {
    return try {
        getStringOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

internal fun TypedArray.getFloatOrNull(index: Int): Float? {
    return try {
        getFloatOrThrow(index)
    } catch (_: Exception) {
        null
    }
}

fun getDimensionAttribute(attributesArray: TypedArray, index: Int): Float? =
    attributesArray.getDimensionOrNull(index)

fun getPixelAttribute(attributesArray: TypedArray, index: Int): Int? =
    attributesArray.getPixelSizeOrNull(index)

fun getResourceIdAttribute(attributesArray: TypedArray, index: Int): Int? =
    attributesArray.getResourceIdOrNull(index)

fun getColorAttribute(attributesArray: TypedArray, index: Int): Int? =
    attributesArray.getColorOrNull(index)

fun getIntegerAttribute(attributesArray: TypedArray, index: Int): Int? =
    attributesArray.getIntegerOrNull(index)

fun getDrawableAttribute(attributesArray: TypedArray, index: Int): Drawable? =
    attributesArray.getDrawableOrNull(index)

fun getBooleanAttribute(attributesArray: TypedArray, index: Int): Boolean? =
    attributesArray.getBooleanOrNull(index)

fun getStringAttribute(attributesArray: TypedArray, index: Int): String? =
    attributesArray.getStringOrNull(index)

fun getFloatAttribute(attributesArray: TypedArray, index: Int): Float? =
    attributesArray.getFloatOrNull(index)