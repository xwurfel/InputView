package configurable.input.maintocopy.presentation.custom.validation

import android.text.TextUtils
import android.util.Patterns
import configurable.input.maintocopy.R
import configurable.input.maintocopy.presentation.custom.InputField

object InputValidator {

    /**
     * Creates a validator for email addresses
     * @return InputErrorHandler that validates email format
     */
    fun emailValidator(): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.isNotBlank() &&
                    Patterns.EMAIL_ADDRESS.matcher(inputText).matches()) {
                    null
                } else {
                    R.string.error_enter_valid_email
                }
            }
        }
    }

    /**
     * Creates a validator for card numbers
     * @param minLength Minimum acceptable length
     * @param errorResource Resource ID for error message
     * @return InputErrorHandler that validates card number length
     */
    fun cardNumberValidator(
        minLength: Int = 19,
        errorResource: Int = R.string.error_invalid_card_number
    ): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.length < minLength) {
                    errorResource
                } else {
                    null
                }
            }
        }
    }

    /**
     * Creates a validator that checks minimum text length
     * @param minLength Minimum acceptable length
     * @param errorResource Resource ID for error message
     * @return InputErrorHandler that validates minimum text length
     */
    fun minLengthValidator(
        minLength: Int,
        errorResource: Int
    ): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.length < minLength) {
                    errorResource
                } else {
                    null
                }
            }
        }
    }

    /**
     * Creates a validator that checks if text is not empty
     * @param errorResource Resource ID for error message
     * @return InputErrorHandler that validates text is not empty
     */
    fun notEmptyValidator(errorResource: Int): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (TextUtils.isEmpty(inputText)) {
                    errorResource
                } else {
                    null
                }
            }
        }
    }

    /**
     * Creates a validator that applies a regular expression pattern
     * @param pattern Regex pattern to match
     * @param errorResource Resource ID for error message
     * @return InputErrorHandler that validates text against a regex pattern
     */
    fun patternValidator(
        pattern: Regex,
        errorResource: Int
    ): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                return if (inputText.matches(pattern)) {
                    null
                } else {
                    errorResource
                }
            }
        }
    }

    /**
     * Creates a validator that combines multiple validators
     * @param validators List of validators to apply
     * @return InputErrorHandler that applies all validators in sequence
     */
    fun compositeValidator(
        vararg validators: InputField.InputErrorHandler
    ): InputField.InputErrorHandler {
        return object : InputField.InputErrorHandler {
            override fun getErrorResourceForText(inputText: String): Int? {
                validators.forEach { validator ->
                    val error = validator.getErrorResourceForText(inputText)
                    if (error != null) {
                        return error
                    }
                }
                return null
            }
        }
    }
}