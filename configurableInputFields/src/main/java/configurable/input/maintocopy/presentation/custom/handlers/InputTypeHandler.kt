package configurable.input.maintocopy.presentation.custom.handlers

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.text.InputFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import configurable.input.maintocopy.R
import configurable.input.maintocopy.presentation.custom.InputField
import configurable.input.maintocopy.presentation.custom.extensions.*


abstract class InputTypeHandler(
    protected val context: Context,
    protected val editText: EditText,
    protected val endDrawableButton: ImageButton,
    protected val openUriButton: TextView,
) {
    abstract fun setupInputType(attrs: TypedArray)

    abstract fun onFocusChanged(hasFocus: Boolean)

    abstract fun onTextChanged()

    open fun getErrorHandler(): InputField.InputErrorHandler? = null

    open fun getAdditionalPadding(): Int = 0
}


class DefaultInputTypeHandler(
    context: Context,
    editText: EditText,
    endDrawableButton: ImageButton,
    openUriButton: TextView,
) : InputTypeHandler(context, editText, endDrawableButton, openUriButton) {

    override fun setupInputType(attrs: TypedArray) {
    }

    override fun onFocusChanged(hasFocus: Boolean) {
    }

    override fun onTextChanged() {
    }
}

class EmailInputTypeHandler(
    context: Context,
    editText: EditText,
    endDrawableButton: ImageButton,
    openUriButton: TextView,
) : InputTypeHandler(context, editText, endDrawableButton, openUriButton) {

    override fun setupInputType(attrs: TypedArray) {
        editText.inputType =
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
    }

    override fun onFocusChanged(hasFocus: Boolean) {
    }

    override fun onTextChanged() {
    }

    override fun getErrorHandler(): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.isNotBlank() &&
                    Patterns.EMAIL_ADDRESS.matcher(inputText).matches()
                ) {
                    null
                } else {
                    R.string.error_enter_valid_email
                }
            }
        }
    }
}


class PasswordInputTypeHandler(
    context: Context,
    editText: EditText,
    endDrawableButton: ImageButton,
    openUriButton: TextView,
) : InputTypeHandler(context, editText, endDrawableButton, openUriButton) {

    private var isPasswordVisible = false
    private var visibleDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_show)
    private var invisibleDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_hide)

    override fun setupInputType(attrs: TypedArray) {
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
        editText.transformationMethod = PasswordTransformationMethod.getInstance()

        attrs.getDrawableOrNull(R.styleable.InputField_onPasswordVisibleDrawable)?.let {
            visibleDrawable = it
        }

        attrs.getDrawableOrNull(R.styleable.InputField_onPasswordInvisibleDrawable)?.let {
            invisibleDrawable = it
        }

        endDrawableButton.visibility = View.VISIBLE
        updateToggleIcon()

        endDrawableButton.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    override fun onFocusChanged(hasFocus: Boolean) {
    }

    override fun onTextChanged() {
    }

    override fun getAdditionalPadding(): Int {
        return 24.dp
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        editText.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
        editText.setSelection(selectionStart, selectionEnd)

        updateToggleIcon()

        editText.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }

    private fun updateToggleIcon() {
        endDrawableButton.setImageDrawable(
            if (isPasswordVisible) visibleDrawable else invisibleDrawable
        )
    }
}


class UriInputTypeHandler(
    context: Context,
    editText: EditText,
    endDrawableButton: ImageButton,
    openUriButton: TextView,
) : InputTypeHandler(context, editText, endDrawableButton, openUriButton) {

    override fun setupInputType(attrs: TypedArray) {
        editText.inputType = InputType.TYPE_TEXT_VARIATION_URI or InputType.TYPE_CLASS_TEXT

        attrs.getStringOrNull(R.styleable.InputField_openUriText)?.let {
            openUriButton.text = it
        }

        openUriButton.setTextColor(
            attrs.getColorOrNull(R.styleable.InputField_openUriTextColor) ?: Color.BLUE
        )

        attrs.getPixelSizeOrNull(R.styleable.InputField_openUriTextSize)?.let {
            openUriButton.textSize = it.toFloat()
        }

        attrs.getResourceIdOrNull(R.styleable.InputField_openUriTextFontFamily)?.let {
            openUriButton.typeface = ResourcesCompat.getFont(context, it)
        }

        endDrawableButton.setImageDrawable(
            attrs.getDrawableOrNull(R.styleable.InputField_openUriEndDrawable)
                ?: AppCompatResources.getDrawable(context, R.drawable.ic_clear_search)
        )

        endDrawableButton.setOnClickListener {
            editText.setText("")
        }
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        updateButtonsVisibility(hasFocus)
    }

    override fun onTextChanged() {
        updateButtonsVisibility(editText.hasFocus())
    }

    override fun getAdditionalPadding(): Int {
        return 24.dp
    }

    private fun updateButtonsVisibility(hasFocus: Boolean) {
        val visibility = if (hasFocus) View.VISIBLE else View.GONE
        openUriButton.visibility = visibility
        endDrawableButton.visibility = if (editText.text.isNotEmpty()) visibility else View.GONE
    }
}

class CardNumberInputTypeHandler(
    context: Context,
    editText: EditText,
    endDrawableButton: ImageButton,
    openUriButton: TextView,
) : InputTypeHandler(context, editText, endDrawableButton, openUriButton) {

    private var wasCardNumberChanged = false
    private val maxCardNumberLength = 19  // 16 digits + 3 spaces

    override fun setupInputType(attrs: TypedArray) {
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.filters = arrayOf(InputFilter.LengthFilter(maxCardNumberLength))
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            editText.setSelection(editText.text.length)
        }
    }

    override fun onTextChanged() {
        formatCardNumber()
    }

    override fun getErrorHandler(): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.length < maxCardNumberLength) {
                    R.string.error_invalid_card_number
                } else {
                    null
                }
            }
        }
    }

    private fun formatCardNumber() {
        val text = editText.text.toString()
        if (text.length > 4 && !wasCardNumberChanged) {
            val formattedText = text.replace(" ", "").chunked(4).joinToString(" ")
            wasCardNumberChanged = true
            editText.setText(formattedText)
            editText.setSelection(editText.text.length)
        } else {
            wasCardNumberChanged = false
        }
    }
}

object InputTypeHandlerFactory {

    fun createHandler(
        inputType: Int,
        context: Context,
        editText: EditText,
        endDrawableButton: ImageButton,
        openUriButton: TextView,
    ): InputTypeHandler {
        return when (inputType) {
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ->
                EmailInputTypeHandler(context, editText, endDrawableButton, openUriButton)

            InputType.TYPE_TEXT_VARIATION_PASSWORD ->
                PasswordInputTypeHandler(context, editText, endDrawableButton, openUriButton)

            InputType.TYPE_TEXT_VARIATION_URI ->
                UriInputTypeHandler(context, editText, endDrawableButton, openUriButton)

            1048577 -> // Card number (1048578-1)
                CardNumberInputTypeHandler(context, editText, endDrawableButton, openUriButton)

            else ->
                DefaultInputTypeHandler(context, editText, endDrawableButton, openUriButton)
        }
    }
}