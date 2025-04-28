package configurable.input.maintocopy.presentation.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import configurable.input.maintocopy.R
import configurable.input.maintocopy.presentation.custom.extensions.*
import configurable.input.maintocopy.presentation.custom.handlers.InputTypeHandler
import configurable.input.maintocopy.presentation.custom.handlers.InputTypeHandlerFactory
import kotlin.math.roundToInt

@Suppress("Unused")
class InputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val editText: EditText by lazy { findViewById(R.id.configurableEditText) }
    private val headerTextView: TextView by lazy { findViewById(R.id.tvHint) }
    private val errorTextView: TextView by lazy { findViewById(R.id.tvError) }
    private val endDrawableButton: ImageButton by lazy { findViewById(R.id.ibEndDrawable) }
    private val openURITextButton: TextView by lazy { findViewById(R.id.tvOpenURI) }

    private val outlinedBoxBackground = GradientDrawable()

    private var isErrorEnabled = true
    private var errorHandler: InputErrorHandler? = null
    private var isErrorVisibleNow = false
    private var outlinedBoxWidth = DEFAULT_BOX_STROKE_WIDTH.dp
    private var inputType = InputType.TYPE_CLASS_TEXT
    private var inputTypeHandler: InputTypeHandler? = null

    init {
        inflate(context, R.layout.configurable_layout, this)
        attrs?.let { initializeAttributes(it) }
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

    fun getText(): String = editText.text.toString()

    fun setText(text: String) {
        editText.setText(text)
    }

    private fun initializeAttributes(attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.InputField).let { typedArray ->
            setupEditText(typedArray)
            setupOutlinedBox(typedArray)
            setupHeaderView(typedArray)
            setupErrorView(typedArray)

            setupInputTypeHandler(typedArray)

            setupCommonListeners(typedArray)

            if (getBooleanAttribute(
                    typedArray,
                    R.styleable.InputField_enableEndDrawableButton
                ) == true
            ) {
                endDrawableButton.visibility = VISIBLE
                getDrawableAttribute(
                    typedArray,
                    R.styleable.InputField_endDrawableButtonIcon
                )?.let {
                    endDrawableButton.setImageDrawable(it)
                }
            }
            typedArray.recycle()
        }
    }

    private fun setupInputTypeHandler(typedArray: TypedArray) {
        val inputTypeValue = getIntegerAttribute(typedArray, R.styleable.InputField_inputType)
        if (inputTypeValue != null) {
            inputType = inputTypeValue - 1

            inputTypeHandler = InputTypeHandlerFactory.createHandler(
                inputType,
                context,
                editText,
                endDrawableButton,
                openURITextButton
            )

            inputTypeHandler?.setupInputType(typedArray)

            inputTypeHandler?.getErrorHandler()?.let {
                errorHandler = it
            }
        }
    }

    private fun setupCommonListeners(typedArray: TypedArray) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            inputTypeHandler?.onFocusChanged(hasFocus)

            if (errorHandler != null) {
                if (!hasFocus) validateInput(typedArray)
                else if (!isErrorVisibleNow) updateOutlinedBoxColor(typedArray, hasFocus)
            } else {
                updateOutlinedBoxColor(typedArray, hasFocus)
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateInput(typedArray)
            }
            false
        }

        val hideHeader =
            getBooleanAttribute(typedArray, R.styleable.InputField_headerHideWhenEmptyText) != false
        editText.doOnTextChanged { _, _, _, count ->
            inputTypeHandler?.onTextChanged()

            if (count > 1) validateInput(typedArray)

            if (hideHeader && headerTextView.text.isNotEmpty()) {
                headerTextView.visibility = if (editText.text.isNotEmpty()) VISIBLE else INVISIBLE
            }
        }
    }

    private fun validateInput(attrs: TypedArray) {
        val errorResource = errorHandler?.getErrorResourceForText(editText.text.toString())
        if (errorResource == null) {
            hideError()
            updateOutlinedBoxColor(attrs, false)
        } else {
            showError(attrs, errorResource)
        }
    }

    private fun hideError() {
        isErrorVisibleNow = false
        errorTextView.visibility = GONE
    }

    private fun showError(attrs: TypedArray, errorResource: Int) {
        isErrorVisibleNow = true
        errorTextView.text = context.getString(errorResource)
        errorTextView.visibility = VISIBLE

        val errorColor = getColorAttribute(attrs, R.styleable.InputField_errorBoxStrokeColor)
            ?: getColorAttribute(attrs, R.styleable.InputField_errorTextColor)
            ?: Color.RED

        outlinedBoxBackground.setStroke(outlinedBoxWidth.roundToInt(), errorColor)
    }

    private fun setupOutlinedBox(attrs: TypedArray) {
        if (getBooleanAttribute(attrs, R.styleable.InputField_isOutlinedBox) != true) return

        outlinedBoxBackground.mutate()

        val cornerRadiusGeneral =
            getFloatAttribute(attrs, R.styleable.InputField_boxStrokeCornerRadius)
        if (cornerRadiusGeneral != null) {
            outlinedBoxBackground.cornerRadius = cornerRadiusGeneral
        } else {
            val topStart =
                getFloatAttribute(attrs, R.styleable.InputField_boxStrokeCornerRadiusTopStart)
                    ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val topEnd =
                getFloatAttribute(attrs, R.styleable.InputField_boxStrokeCornerRadiusTopEnd)
                    ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val bottomStart =
                getFloatAttribute(attrs, R.styleable.InputField_boxStrokeCornerRadiusBottomStart)
                    ?: DEFAULT_BOX_STROKE_RADIUS.dp
            val bottomEnd =
                getFloatAttribute(attrs, R.styleable.InputField_boxStrokeCornerRadiusBottomEnd)
                    ?: DEFAULT_BOX_STROKE_RADIUS.dp

            outlinedBoxBackground.cornerRadii = floatArrayOf(
                topStart, topStart,
                topEnd, topEnd,
                bottomEnd, bottomEnd,
                bottomStart, bottomStart
            )
        }

        outlinedBoxWidth = getDimensionAttribute(attrs, R.styleable.InputField_boxStrokeWidth)
            ?: DEFAULT_BOX_STROKE_WIDTH.dp
        val strokeColor =
            getColorAttribute(attrs, R.styleable.InputField_boxStrokeColor) ?: Color.GRAY

        outlinedBoxBackground.setStroke(outlinedBoxWidth.roundToInt(), strokeColor)
        editText.background = outlinedBoxBackground
    }

    private fun setupHeaderView(attrs: TypedArray) {
        val headerText = getStringAttribute(attrs, R.styleable.InputField_header)

        if (headerText.isNullOrEmpty()) {
            headerTextView.visibility = GONE
            return
        }

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

        val marginStart = getDimensionAttribute(attrs, R.styleable.InputField_headerTextMarginStart)
            ?: DEFAULT_MARGIN_ZERO.dp
        val marginBottom =
            getDimensionAttribute(attrs, R.styleable.InputField_headerTextMarginBottom)
                ?: DEFAULT_HINT_MARGIN_BOTTOM.dp

        headerTextView.layoutParams = LayoutParams(headerTextView.layoutParams).apply {
            setMargins(marginStart.roundToInt(), 0, 0, marginBottom.roundToInt())
        }
    }

    private fun setupEditText(attrs: TypedArray) {
        getDimensionAttribute(attrs, R.styleable.InputField_editTextHeight)?.let {
            editText.layoutParams = FrameLayout.LayoutParams(editText.layoutParams).apply {
                height = it.roundToInt()
            }
        }

        getIntegerAttribute(attrs, R.styleable.InputField_maxLength)?.let {
            editText.filters = arrayOf(InputFilter.LengthFilter(it))
        }

        val startDrawable =
            getDrawableAttribute(attrs, R.styleable.InputField_editTextStartIconDrawable)
        val endDrawable =
            getDrawableAttribute(attrs, R.styleable.InputField_editTextEndIconDrawable)
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, endDrawable, null)

        if (startDrawable != null || endDrawable != null) {
            editText.compoundDrawablePadding = getDimensionAttribute(
                attrs,
                R.styleable.InputField_editTextDrawablePadding
            )?.roundToInt()
                ?: DEFAULT_DRAWABLE_PADDING.dp
        }

        getStringAttribute(attrs, R.styleable.InputField_text)?.let {
            editText.setText(it)
        }

        getStringAttribute(attrs, R.styleable.InputField_hint)?.let {
            editText.hint = it
        }

        getColorAttribute(attrs, R.styleable.InputField_editTextColor)?.let {
            editText.setTextColor(it)
        }

        getResourceIdAttribute(attrs, R.styleable.InputField_editTextFontFamily)?.let {
            editText.typeface = ResourcesCompat.getFont(context, it)
        }

        getPixelAttribute(attrs, R.styleable.InputField_editTextSize)?.let {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.toFloat())
        }

        val startPadding =
            getDimensionAttribute(attrs, R.styleable.InputField_editTextPaddingStart)?.roundToInt()
                ?: DEFAULT_EDIT_TEXT_PADDING_START.dp

        var endPadding =
            getDimensionAttribute(attrs, R.styleable.InputField_editTextPaddingEnd)?.roundToInt()
                ?: DEFAULT_EDIT_TEXT_PADDING_END.dp

        endPadding += inputTypeHandler?.getAdditionalPadding() ?: 0

        editText.setPadding(
            startPadding,
            editText.paddingTop,
            endPadding,
            editText.paddingBottom
        )

        getColorAttribute(attrs, R.styleable.InputField_editTextHintTextColor)?.let {
            editText.setHintTextColor(it)
        }

        getIntegerAttribute(attrs, R.styleable.InputField_maxLines)?.let {
            editText.maxLines = it
        }
    }

    private fun setupErrorView(attrs: TypedArray) {
        isErrorEnabled = getBooleanAttribute(attrs, R.styleable.InputField_isErrorEnabled) != false

        if (!isErrorEnabled) {
            errorTextView.visibility = GONE
            return
        }

        val errorText = getStringAttribute(attrs, R.styleable.InputField_errorText)
        errorTextView.text = errorText

        errorTextView.setTextColor(
            getColorAttribute(attrs, R.styleable.InputField_errorTextColor) ?: Color.RED
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

        errorTextView.layoutParams = LayoutParams(errorTextView.layoutParams).apply {
            setMargins(marginStart.roundToInt(), marginTop.roundToInt(), 0, 0)
        }
    }

    private fun updateOutlinedBoxColor(attrs: TypedArray, hasFocus: Boolean) {
        val strokeColor = when {
            !hasFocus -> getColorAttribute(attrs, R.styleable.InputField_boxStrokeColor)
                ?: Color.GRAY

            else -> getColorAttribute(attrs, R.styleable.InputField_boxStrokeFocusedColor)
                ?: Color.BLUE
        }

        outlinedBoxBackground.setStroke(outlinedBoxWidth.roundToInt(), strokeColor)
    }


    interface InputErrorHandler {
        @StringRes
        fun getErrorResourceForText(inputText: String): Int?
    }


    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
            putSparseParcelableArray(SPARSE_STATE_KEY, saveChildViewStates())
        }
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

    private fun ViewGroup.saveChildViewStates(): SparseArray<Parcelable> {
        val childViewStates = SparseArray<Parcelable>()
        children.forEach { child -> child.saveHierarchyState(childViewStates) }
        return childViewStates
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
        private const val DEFAULT_MARGIN_ZERO = 0f
        private const val DEFAULT_HINT_MARGIN_BOTTOM = 6f
        private const val DEFAULT_ERROR_MARGIN_TOP = 6f

        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"
        private const val SPARSE_STATE_KEY = "SPARSE_STATE_KEY"
    }
}