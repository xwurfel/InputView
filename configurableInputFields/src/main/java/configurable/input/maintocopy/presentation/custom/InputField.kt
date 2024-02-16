package configurable.input.maintocopy.presentation.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import configurable.input.maintocopy.R
import configurable.input.maintocopy.presentation.custom.extensions.dp
import configurable.input.maintocopy.presentation.custom.extensions.getBooleanAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getColorAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getDimensionAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getDrawableAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getFloatAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getIntegerAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getPixelAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getResourceIdAttribute
import configurable.input.maintocopy.presentation.custom.extensions.getStringAttribute
import kotlin.math.roundToInt


class InputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {

    private val editText: EditText by lazy {
        findViewById(R.id.configurableEditText)
    }

    private val rootLayout: ConstraintLayout by lazy {
        findViewById(R.id.llMainLayout)
    }

    private val outlinedBoxBackground: GradientDrawable by lazy {
        GradientDrawable()
    }

    private val headerTextView: TextView by lazy {
        findViewById(R.id.tvHint)
    }

    private val errorTextView: TextView by lazy {
        findViewById(R.id.tvError)
    }

    private val endDrawableButton: ImageButton by lazy {
        findViewById(R.id.ibEndDrawable)
    }

    private val openURITextButton: TextView by lazy {
        findViewById(R.id.tvOpenURI)
    }

    private var isErrorEnabled: Boolean = true

    private var errorHandler: InputErrorHandler? = null
    private var isErrorVisibleNow: Boolean = false
    private var outlinedBoxWidth = DEFAULT_BOX_STROKE_WIDTH
    private var isPasswordVisibleNow: Boolean = false
    private var inputType: Int = InputType.TYPE_CLASS_TEXT
    private var wasCardNumberChanged = 0

    init {
        inflate(context, R.layout.configurable_layout, this)
        setViewsAttributes(attrs)
    }

    fun setOnOpenURIClickListener(listener: OnClickListener?) {
        openURITextButton.setOnClickListener(listener)
    }

    fun setOnEndDrawableClickListener(listener: OnClickListener?) {
        endDrawableButton.setOnClickListener(listener)
    }

    fun isCurrentInputCorrect(): Boolean =
        errorHandler?.getErrorResourceForText(editText.text.toString()) == null

    fun setErrorHandler(errorHandler: InputErrorHandler) {
        this.errorHandler = errorHandler
    }

    private fun setOutlinedBox(attributesArray: TypedArray) {
        if (getBooleanAttribute(attributesArray, R.styleable.InputField_isOutlinedBox) == false) return

        outlinedBoxBackground.mutate()

        val cornerRadiusGeneral = getFloatAttribute(attributesArray, R.styleable.InputField_boxStrokeCornerRadius)

        if (cornerRadiusGeneral != null) {
            outlinedBoxBackground.cornerRadius = cornerRadiusGeneral
        }
        else {
            val topStartCornerRadius = getFloatAttribute(attributesArray, R.styleable.InputField_boxStrokeCornerRadiusTopStart) ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val topEndCornerRadius = getFloatAttribute(attributesArray, R.styleable.InputField_boxStrokeCornerRadiusTopEnd) ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val bottomStartCornerRadius = getFloatAttribute(attributesArray, R.styleable.InputField_boxStrokeCornerRadiusBottomStart) ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val bottomEndCornerRadius = getFloatAttribute(attributesArray, R.styleable.InputField_boxStrokeCornerRadiusBottomEnd) ?: DEFAULT_BOX_STROKE_RADIUS.dp

            outlinedBoxBackground.cornerRadii = floatArrayOf(
                topStartCornerRadius, topStartCornerRadius,
                topEndCornerRadius, topEndCornerRadius,
                bottomEndCornerRadius, bottomEndCornerRadius,
                bottomStartCornerRadius, bottomStartCornerRadius
            )
        }
        outlinedBoxWidth =
            getDimensionAttribute(attributesArray, R.styleable.InputField_boxStrokeWidth)
                ?: DEFAULT_BOX_STROKE_WIDTH.dp
        val strokeColor =
            getColorAttribute(attributesArray, R.styleable.InputField_boxStrokeColor) ?: Color.GRAY

        outlinedBoxBackground.setStroke(
            outlinedBoxWidth.roundToInt(),
            strokeColor
        )
        editText.background = outlinedBoxBackground
    }

    private fun setViewsAttributes(attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.InputField).let {
            setEditTextAttributes(it)
            setOutlinedBox(it)
            setHintView(it)
            setErrorView(it)
            initListeners(it)
            setEndDrawableData(it)
            it.recycle()
        }
    }

    private fun setEndDrawableData(attrs: TypedArray) {
        if (getBooleanAttribute(attrs, R.styleable.InputField_enableEndDrawableButton) == true)
            endDrawableButton.visibility = View.VISIBLE
        getDrawableAttribute(attrs, R.styleable.InputField_endDrawableButtonIcon)?.let {
            endDrawableButton.setImageDrawable(it)
        }
    }

    private fun initListeners(attrs: TypedArray) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (errorHandler != null) {
                if (!hasFocus) handleErrorState(attrs)
                else if (!isErrorVisibleNow) changeOutlinedBoxColor(attrs, true)
            }
            else {
                changeOutlinedBoxColor(attrs, hasFocus)
            }
            when (inputType) {
                InputType.TYPE_TEXT_VARIATION_URI -> setUriOpenButtonVisibility(hasFocus)
                INPUT_TYPE_CARD_NUMBER -> {
                    if (hasFocus){
                        editText.setSelection(editText.text.toString().length)
                    }
                }
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleErrorState(attrs)
            }
            false
        }

        val hideHeader =
            getBooleanAttribute(attrs, R.styleable.InputField_headerHideWhenEmptyText) ?: true
        editText.doOnTextChanged { _, _, _, count ->
            if (count > 1 && inputType != INPUT_TYPE_CARD_NUMBER) handleErrorState(attrs)

            when (inputType) {
                InputType.TYPE_TEXT_VARIATION_URI -> setUriOpenButtonVisibility(editText.hasFocus())
                INPUT_TYPE_CARD_NUMBER -> setWhiteSpacesWhenNeeded()
            }

            val length = editText.text.length
            if (hideHeader && headerTextView.text.toString().isNotEmpty()) {
                if (length > 0) {
                    headerTextView.visibility = View.VISIBLE
                } else {
                    headerTextView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun setWhiteSpacesWhenNeeded() {
        val text = editText.text.toString()
        if (text.length > 4 && wasCardNumberChanged == 0) {
            val textResult = text.replace(" ", "").chunked(4).joinToString(" ")
            wasCardNumberChanged = 1
            editText.setText(textResult)
            editText.setSelection(editText.text.toString().length)
        }
        else wasCardNumberChanged = 0
    }

    private fun setUriOpenButtonVisibility(hasFocus: Boolean) {
        val visibility = if (hasFocus) View.VISIBLE else View.GONE
        openURITextButton.visibility = visibility
        endDrawableButton.visibility = if (editText.text.toString().isEmpty()) View.GONE else visibility
    }

    private fun handleErrorState(attrs: TypedArray) {
        val errorResource = errorHandler?.getErrorResourceForText(editText.text.toString())
        if (errorResource == null) {
            hideErrorState()
            changeOutlinedBoxColor(attrs, false)
        }
        else {
            showErrorState(attrs, errorResource)
        }
    }

    private fun hideErrorState() {
        isErrorVisibleNow = false
        errorTextView.visibility = View.GONE
    }

    private fun showErrorState(attrs: TypedArray, errorResource: Int) {
        isErrorVisibleNow = true
        errorTextView.text = context.getString(errorResource)
        errorTextView.visibility = View.VISIBLE

        val boxStrokeErrorColor =
            getColorAttribute(attrs, R.styleable.InputField_errorBoxStrokeColor)
                ?: getColorAttribute(attrs, R.styleable.InputField_errorTextColor)
                ?: Color.RED

        outlinedBoxBackground.setStroke(outlinedBoxWidth.roundToInt(), boxStrokeErrorColor)
    }

    private fun setHintView(attrs: TypedArray) {
        val hintText = getStringAttribute(attrs, R.styleable.InputField_hint)
        val headerText = getStringAttribute(attrs, R.styleable.InputField_header)

        editText.hint = hintText

        if (headerText.isNullOrEmpty()) headerTextView.visibility = View.GONE
        else {
            headerTextView.text = headerText
            getColorAttribute(attrs, R.styleable.InputField_headerTextColor)?.let {
                headerTextView.setTextColor(it)
            }
            getPixelAttribute(attrs, R.styleable.InputField_headerTextSize)?.let {
                headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.toFloat())
            }
            getResourceIdAttribute(attrs, R.styleable.InputField_headerTextFontFamily)?.let {
                headerTextView.typeface = ResourcesCompat.getFont(context, it)
            }
            val marginStart = getDimensionAttribute(attrs, R.styleable.InputField_headerTextMarginStart) ?: DEFAULT_MARGIN_ZERO.dp
            val marginBottom = getDimensionAttribute(attrs, R.styleable.InputField_headerTextMarginBottom) ?: DEFAULT_HINT_MARGIN_BOTTOM.dp

            headerTextView.layoutParams =
                LayoutParams(headerTextView.layoutParams)
                    .apply {
                        setMargins(marginStart.roundToInt(), 0, 0, marginBottom.roundToInt())
                    }
        }
    }

    private fun setPasswordToggleDrawable(attrs: TypedArray) {
        endDrawableButton.setImageDrawable(
            if (isPasswordVisibleNow) getDrawableAttribute(attrs, R.styleable.InputField_onPasswordVisibleDrawable)
                ?: AppCompatResources.getDrawable(context, R.drawable.ic_show)
            else getDrawableAttribute(attrs, R.styleable.InputField_onPasswordInvisibleDrawable)
                ?: AppCompatResources.getDrawable(context, R.drawable.ic_hide)
            )
    }

    private fun setupPasswordTypeListeners(attrs: TypedArray) {
        endDrawableButton.visibility = View.VISIBLE

        setPasswordToggleDrawable(attrs)

        endDrawableButton.setOnClickListener {
            isPasswordVisibleNow = isPasswordVisibleNow == false
            val cursorSelectionStart = editText.selectionStart
            val cursorSelectionEnd = editText.selectionEnd
            editText.transformationMethod = if (isPasswordVisibleNow) null
            else PasswordTransformationMethod()
            editText.setSelection(cursorSelectionStart, cursorSelectionEnd)
            setPasswordToggleDrawable(attrs)

            editText.requestFocus()
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(editText, 0)
            changeOutlinedBoxColor(attrs, true)
        }
    }

    private fun setEditTextAttributes(attributesArray: TypedArray) {
        getDimensionAttribute(attributesArray, R.styleable.InputField_editTextHeight)?.let {
            editText.layoutParams =
                FrameLayout.LayoutParams(editText.layoutParams)
                    .apply {
                        height = it.roundToInt()
                    }
        }

        getIntegerAttribute(attributesArray, R.styleable.InputField_maxLength)?.let {
            val filterArray = arrayOf<InputFilter>(LengthFilter(it))
            editText.filters = filterArray
        }

        val startDrawable = getDrawableAttribute(attributesArray, R.styleable.InputField_editTextStartIconDrawable)
        val endDrawable =  getDrawableAttribute(attributesArray, R.styleable.InputField_editTextEndIconDrawable)
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, endDrawable, null)

        if (startDrawable != null || endDrawable != null) {
            editText.compoundDrawablePadding =
                getDimensionAttribute(attributesArray, R.styleable.InputField_editTextDrawablePadding)?.roundToInt()
                    ?: DEFAULT_DRAWABLE_PADDING.dp
        }

        getStringAttribute(attributesArray, R.styleable.InputField_text)?.let {
            editText.setText(it)
        }

        getColorAttribute(attributesArray, R.styleable.InputField_editTextColor)?.let {
            editText.setTextColor(it)
        }
        getResourceIdAttribute(attributesArray, R.styleable.InputField_editTextFontFamily)?.let {
            editText.typeface = ResourcesCompat.getFont(context, it)
        }
        getPixelAttribute(attributesArray, R.styleable.InputField_editTextSize)?.let {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.toFloat())
        }

        var endPadding =
            getDimensionAttribute(attributesArray, R.styleable.InputField_editTextPaddingEnd)?.roundToInt()
                ?: DEFAULT_EDIT_TEXT_PADDING_END.dp

        getIntegerAttribute(attributesArray, R.styleable.InputField_inputType)?.let {

            inputType = it-1

            when (it-1) {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                    errorHandler = emailErrorHandler
                }
                InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                    setupPasswordTypeListeners(attributesArray)
                    endPadding += endDrawableButton.width + configurable.input.maintocopy.presentation.custom.InputField.Companion.DEFAULT_EDIT_TEXT_PADDING_END_WITH_END_DRAWABLE.dp
                }
                InputType.TYPE_TEXT_VARIATION_URI -> {
                    setupUriTypeData(attributesArray)
                    endPadding += endDrawableButton.width + configurable.input.maintocopy.presentation.custom.InputField.Companion.DEFAULT_EDIT_TEXT_PADDING_END_WITH_END_DRAWABLE.dp
                }
                INPUT_TYPE_CARD_NUMBER -> {
                    errorHandler = cardNumberErrorHandler
                    editText.filters = arrayOf(LengthFilter(MAX_LENGTH_CARD_NUMBER))
                }
            }
            editText.inputType = it
        }
        getIntegerAttribute(attributesArray, R.styleable.InputField_maxLines)?.let {
            editText.maxLines = it
        }

        val startPadding =
            getDimensionAttribute(attributesArray, R.styleable.InputField_editTextPaddingStart)?.roundToInt()
                ?: DEFAULT_EDIT_TEXT_PADDING_START.dp

        editText.setPadding(
            startPadding,
            editText.paddingTop,
            endPadding,
            editText.paddingBottom
        )


        getColorAttribute(attributesArray, R.styleable.InputField_editTextHintTextColor)?.let {
            editText.setHintTextColor(it)
        }

    }

    private fun setupUriTypeData(attributesArray: TypedArray) {
        getStringAttribute(attributesArray, R.styleable.InputField_openUriText)?.let {
            openURITextButton.text = it
        }
        openURITextButton.setTextColor(
            getColorAttribute(attributesArray, R.styleable.InputField_openUriTextColor)
                ?: Color.BLUE
        )
        getPixelAttribute(attributesArray, R.styleable.InputField_openUriTextSize)?.let {
            openURITextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.toFloat())
        }
        getResourceIdAttribute(attributesArray, R.styleable.InputField_openUriTextFontFamily)?.let {
            openURITextButton.typeface = ResourcesCompat.getFont(context, it)
        }

        endDrawableButton.setImageDrawable(
            getDrawableAttribute(attributesArray, R.styleable.InputField_openUriEndDrawable)
            ?: AppCompatResources.getDrawable(context, R.drawable.ic_clear_search)
        )

        endDrawableButton.setOnClickListener {
            editText.setText("")
        }
    }

    private fun setErrorView(attrs: TypedArray) {
        isErrorEnabled = getBooleanAttribute(attrs, R.styleable.InputField_isErrorEnabled) ?: true
        if (!isErrorEnabled) {
            errorTextView.visibility = View.GONE
            return
        }

        val errorText = getStringAttribute(attrs, R.styleable.InputField_errorText)
        errorTextView.text = errorText

        errorTextView.setTextColor(
            getColorAttribute(attrs, R.styleable.InputField_errorTextColor)
                ?: Color.RED
        )
        getPixelAttribute(attrs, R.styleable.InputField_errorTextSize)?.let {
            errorTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.toFloat())
        }
        getResourceIdAttribute(attrs, R.styleable.InputField_errorTextFontFamily)?.let {
            errorTextView.typeface = ResourcesCompat.getFont(context, it)
        }

        errorTextView.compoundDrawablePadding =
            getDimensionAttribute(attrs, R.styleable.InputField_errorDrawablePadding)?.roundToInt()
                ?: DEFAULT_DRAWABLE_PADDING.dp
        val iconDrawable = getDrawableAttribute(attrs, R.styleable.InputField_errorIconDrawable)
        errorTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(iconDrawable, null, null, null)

        val marginStart = getDimensionAttribute(attrs, R.styleable.InputField_errorTextMarginStart) ?: DEFAULT_MARGIN_ZERO.dp
        val marginTop = getDimensionAttribute(attrs, R.styleable.InputField_errorTextMarginTop) ?: DEFAULT_ERROR_MARGIN_TOP.dp

        errorTextView.layoutParams =
            LayoutParams(errorTextView.layoutParams)
                .apply {
                    setMargins(marginStart.roundToInt(), marginTop.roundToInt(), 0, 0)
                }
    }
    private fun changeOutlinedBoxColor(attributesArray: TypedArray, hasFocus: Boolean) {
        val strokeColor =
            if (!hasFocus) getColorAttribute(attributesArray, R.styleable.InputField_boxStrokeColor) ?: Color.GRAY
            else getColorAttribute(attributesArray, R.styleable.InputField_boxStrokeFocusedColor) ?: Color.BLUE

        outlinedBoxBackground.setStroke(
            outlinedBoxWidth.roundToInt(),
            strokeColor
        )
    }

    interface InputErrorHandler {

        @StringRes
        fun getErrorResourceForText(inputText: String): Int?

    }

    private val emailErrorHandler get() =
        object : InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.isNotBlank() &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(inputText).matches()) null
                else R.string.error_enter_valid_email
            }
        }

    private val cardNumberErrorHandler get() =
        object : InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.length < MAX_LENGTH_CARD_NUMBER) R.string.error_invalid_card_number
                else null
            }
        }

    /**
     * Custom SaveInstance realization
     */
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
            putSparseParcelableArray(SPARSE_STATE_KEY, saveChildViewStates())
        }
    }

    private fun ViewGroup.saveChildViewStates(): SparseArray<Parcelable> {
        val childViewStates = SparseArray<Parcelable>()
        children.forEach { child -> child.saveHierarchyState(childViewStates) }
        return childViewStates
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (newState is Bundle) {
            val childrenState = newState.getSparseParcelableArray<Parcelable>(SPARSE_STATE_KEY)
            childrenState?.let { restoreChildViewStates(it) }
            newState = newState.getParcelable(SUPER_STATE_KEY)
        }
        super.onRestoreInstanceState(newState)
    }

    private fun restoreChildViewStates(childViewStates: SparseArray<Parcelable>) {
        children.forEach { child -> child.restoreHierarchyState(childViewStates) }
    }

    companion object {
        private const val DEFAULT_BOX_STROKE_RADIUS = 12f
        private const val DEFAULT_BOX_STROKE_WIDTH = 1f
        private const val DEFAULT_DRAWABLE_PADDING = 10
        private const val DEFAULT_EDIT_TEXT_PADDING_START = 16
        private const val DEFAULT_EDIT_TEXT_PADDING_END = 16
        private const val DEFAULT_EDIT_TEXT_PADDING_END_WITH_END_DRAWABLE = 24
        private const val DEFAULT_MARGIN_ZERO = 0f
        private const val DEFAULT_HINT_MARGIN_BOTTOM = 6f
        private const val DEFAULT_ERROR_MARGIN_TOP = 6f

        private const val INPUT_TYPE_CARD_NUMBER = 1048578-1
        private const val MAX_LENGTH_CARD_NUMBER = 19


        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"
        private const val SPARSE_STATE_KEY = "SPARSE_STATE_KEY"
    }
}
