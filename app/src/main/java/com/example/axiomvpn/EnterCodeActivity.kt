package com.example.axiomvpn

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class EnterCodeActivity : AppCompatActivity() {

    private lateinit var codeFields: List<EditText>
    private lateinit var codeTils: List<TextInputLayout>
    private lateinit var btnConfirm: MaterialButton
    private lateinit var tvResend: TextView
    private lateinit var tvTitle: TextView
    private lateinit var baseTitleText: String


    private val correctCode = "12345"
    private val enteredCode = CharArray(5) { ' ' }

    private var resendTimer: CountDownTimer? = null
    private val resendDelay = 30_000L
    private var canResend = false


    private var hasError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_code)

        btnConfirm = findViewById(R.id.btn_confirm)
        tvResend = findViewById(R.id.tv_resend_code)
        tvTitle = findViewById(R.id.tv_title)

        codeFields = listOf(
            findViewById(R.id.et_code_1),
            findViewById(R.id.et_code_2),
            findViewById(R.id.et_code_3),
            findViewById(R.id.et_code_4),
            findViewById(R.id.et_code_5)
        )

        codeTils = listOf(
            findViewById(R.id.til_code_1),
            findViewById(R.id.til_code_2),
            findViewById(R.id.til_code_3),
            findViewById(R.id.til_code_4),
            findViewById(R.id.til_code_5)
        )

        setupOtp()
        setupTimer()
        setupTitle()

        btnConfirm.setOnClickListener { checkCode() }
    }

    // ================= OTP INPUT =================

    private fun setupOtp() {
        codeFields.forEachIndexed { index, et ->

            et.setOnClickListener {
                et.text.clear()
            }

            et.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true

                    clearAllErrors()

                    val text = s.toString()

                    when {
                        text.length > 1 -> {
                            distribute(text)
                            isUpdating = false
                            return
                        }

                        text.isNotEmpty() -> {
                            enteredCode[index] = text[0]
                            if (index < 4) {
                                codeFields[index + 1].requestFocus()
                            } else {
                                hideKeyboard()
                            }
                        }

                        else -> {
                            enteredCode[index] = ' '
                        }
                    }

                    isUpdating = false
                }
            })

            et.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (et.text.isEmpty() && index > 0) {
                        codeFields[index - 1].requestFocus()
                        codeFields[index - 1].text?.clear()
                    }
                }
                false
            }
        }
    }

    private fun distribute(code: String) {
        code.take(5).forEachIndexed { i, c ->
            enteredCode[i] = c
            codeFields[i].setText(c.toString())
        }
        if (code.length >= 5) {
            codeFields[4].requestFocus()
            hideKeyboard()
        } else {
            codeFields[code.length].requestFocus()
        }
    }

    // ================= CHECK CODE =================

    private fun checkCode() {
        val code = enteredCode.joinToString("").trim()
        if (code == correctCode) {
            showSuccess()
        } else {
            showError()
        }
    }

    // ================= ERROR STATE =================

    private fun showError() {
        hasError = true

        codeTils.forEach {
            it.isErrorEnabled = true
            it.error = " "              // нужен, чтобы включился error-state
            it.errorIconDrawable = null // ВАЖНО — убирает "!"
        }

        shakeAnimation()

        Snackbar.make(btnConfirm, "Неверный код", Snackbar.LENGTH_SHORT).show()
    }



    private fun clearAllErrors() {
        if (!hasError) return
        hasError = false

        codeTils.forEach {
            it.isErrorEnabled = false
            it.error = null
        }
    }



    // ================= SUCCESS =================

    private fun showSuccess() {
        val green = ContextCompat.getColor(this, R.color.success_green)

        codeTils.forEach {
            it.boxStrokeColor = green
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, doneRegister::class.java))
            finish()
        }, 500)
    }

    // ================= ANIMATION =================

    private fun shakeAnimation() {
        val shake = TranslateAnimation(-20f, 20f, 0f, 0f).apply {
            duration = 80
            repeatMode = TranslateAnimation.REVERSE
            repeatCount = 4
        }

        codeTils.forEach {
            it.startAnimation(shake)
        }
    }

    // ================= UI HELPERS =================

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(codeFields.firstOrNull()?.windowToken, 0)
    }

    private fun setupTitle() {
        val type = intent.getStringExtra("TYPE") ?: "EMAIL"

        baseTitleText = if (type == "PHONE")
            "Введите код, пришедший на номер"
        else
            "Введите код, пришедший на email"

        tvTitle.text = baseTitleText
    }


    // ================= TIMER =================

    private fun setupTimer() {
        tvResend.text = "Отправить код еще раз"
        tvResend.alpha = 0.5f
        tvResend.isClickable = false

        canResend = false
        startTimer()

        tvResend.setOnClickListener {
            if (!canResend) return@setOnClickListener

            // здесь вызываешь API resend
            startTimer()
        }
    }




    private fun startTimer() {
        resendTimer?.cancel()

        canResend = false
        tvResend.isClickable = false
        tvResend.alpha = 0.5f

        resendTimer = object : CountDownTimer(resendDelay, 1000) {

            override fun onTick(ms: Long) {
                val sec = ms / 1000
                tvTitle.text =
                    "$baseTitleText\nПовторно отправить код можно через $sec сек"
            }

            override fun onFinish() {
                tvTitle.text = baseTitleText

                canResend = true
                tvResend.isClickable = true
                tvResend.alpha = 1f
            }

        }.start()
    }



    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}
