package configurable.input.fields

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import configurable.input.fields.presentation.custom.InputField

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputTextView.setErrorHandler(inputTextErrorHandler)
        inputUriTextView.setOnOpenURIClickListener {}

    }

    private val inputTextView: InputField by lazy {
        findViewById(R.id.inputTextField)
    }
    private val inputUriTextView: InputField by lazy {
        findViewById(R.id.uriTextField)
    }

    private val inputTextErrorHandler = object : InputField.InputErrorHandler {
        override fun getErrorResourceForText(inputText: String): Int? {
            return if (inputText.length < 4) R.string.error1
            else if (inputText.length > 10) R.string.error2
            else if (inputText.length == 6) R.string.error3
            else null
        }
    }
}