package com.example.axiomvpn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.axiomvpn.EnterCodeActivity

class MainActivity : AppCompatActivity() {

    private val countries = mutableListOf<Country>()
    private var selectedCountry: Country? = null
    private var isPhoneMode = true // true = phone, false = email

    // View references
    private lateinit var cvLoginTypeSwitcher: com.google.android.material.card.MaterialCardView
    private lateinit var cvEmailSelector: com.google.android.material.card.MaterialCardView
    private lateinit var cvPhoneSelector: com.google.android.material.card.MaterialCardView
    private lateinit var tvEmailLabel: TextView
    private lateinit var tvPhoneLabel: TextView
    private lateinit var llCountrySelector: View
    private lateinit var tvCountryFlag: TextView
    private lateinit var tvCountryCode: TextView
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var tilPhoneNumber: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: com.google.android.material.button.MaterialButton
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var cvGoogleLogin: com.google.android.material.card.MaterialCardView
    private lateinit var cvFacebookLogin: com.google.android.material.card.MaterialCardView
    private lateinit var cvAppleLogin: com.google.android.material.card.MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TEST", "MainActivity onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupCountryData()
        setupCountrySelector()
        setupClickListeners()
        setupEmailErrorCleaner()

        // Восстанавливаем состояние
        savedInstanceState?.let {
            isPhoneMode = it.getBoolean("isPhoneMode", true)
        }
        updateLoginMode()
    }


    private fun initializeViews() {
        cvLoginTypeSwitcher = findViewById(R.id.cv_login_type_switcher)
        cvEmailSelector = findViewById(R.id.cv_email_selector)
        cvPhoneSelector = findViewById(R.id.cv_phone_selector)
        tvEmailLabel = findViewById(R.id.tv_email_label)
        tvPhoneLabel = findViewById(R.id.tv_phone_label)

        llCountrySelector = findViewById(R.id.ll_country_selector)
        tvCountryFlag = findViewById(R.id.tv_country_flag)
        tvCountryCode = findViewById(R.id.tv_country_code)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        tilPhoneNumber = findViewById(R.id.til_phone_number)
        etEmail = findViewById(R.id.et_email)
        tilEmail = findViewById(R.id.til_email)
        etPassword = findViewById(R.id.et_password)
        tilPassword = findViewById(R.id.til_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        cvGoogleLogin = findViewById(R.id.cv_google_login)
        cvFacebookLogin = findViewById(R.id.cv_facebook_login)
        cvAppleLogin = findViewById(R.id.cv_apple_login)
    }

    private fun setupCountryData() {
        val flags = resources.getStringArray(R.array.country_flags)
        val names = resources.getStringArray(R.array.country_names)
        val codes = resources.getStringArray(R.array.country_codes)

        for (i in flags.indices) {
            countries.add(Country(flags[i], names[i], codes[i]))
        }
        selectedCountry = countries[0]
        updateCountryDisplay()
    }

    private fun setupCountrySelector() {
        selectedCountry?.let {
            tvCountryFlag.text = it.flag
            tvCountryCode.text = it.code
        }
    }

    private fun validatePassword(): Boolean {
        val password = etPassword.text.toString().trim()
        return when {
            password.isEmpty() -> {
                tilPassword.error = "Введите пароль"
                false
            }
            password.length < 6 -> {
                tilPassword.error = "Минимум 6 символов"
                false
            }
            else -> {
                tilPassword.error = null
                true
            }
        }
    }

    private fun setupEmailErrorCleaner() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                // Если текущая ошибка — "Неверная почта" и email стал валидным — очищаем ошибку
                if (tilEmail.error == "Неверная почта" && isValidEmail(email)) {
                    tilEmail.error = null
                }
                // Если текущая ошибка — "Введите почту" и поле не пустое — очищаем ошибку
                if (tilEmail.error == "Введите почту" && email.isNotEmpty()) {
                    tilEmail.error = null
                }
            }
        })
    }

    private fun setupClickListeners() {
        // Switcher - клик по ВСЕЙ области
        cvLoginTypeSwitcher.setOnClickListener {
            toggleLoginMode()
        }

        // Country selector
        llCountrySelector.setOnClickListener {
            showCountryDialog()
        }

        // Login button
        btnLogin.setOnClickListener {
            if (validateInput()) {
                val loginSuccessful = if (isPhoneMode) {
                    performLogin(
                        selectedCountry?.code ?: "+7",
                        etPhoneNumber.text.toString(),
                        etPassword.text.toString()
                    )
                } else {
                    performEmailLogin(
                        etEmail.text.toString(),
                        etPassword.text.toString()
                    )
                }

                if (loginSuccessful) {
                    val intent = Intent(this, EnterCodeActivity::class.java)
                    intent.putExtra("TYPE", if (isPhoneMode) "PHONE" else "EMAIL") // ← добавляем
                    startActivity(intent)
                } else {
                    tilPassword.error = "Неверный логин или пароль"
                }
            }
        }

        tvRegister.setOnClickListener { }
        tvForgotPassword.setOnClickListener { }

        cvGoogleLogin.setOnClickListener { performSocialLogin("Google") }
        cvFacebookLogin.setOnClickListener { performSocialLogin("Facebook") }
        cvAppleLogin.setOnClickListener { performSocialLogin("Apple") }
    }

    private fun toggleLoginMode() {
        isPhoneMode = !isPhoneMode
        updateLoginMode()
    }

    private fun updateLoginMode() {
        if (isPhoneMode) {
            // --- Телефонный режим ---
            cvPhoneSelector.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
            cvEmailSelector.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
            tvPhoneLabel.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            tvEmailLabel.setTextColor(ContextCompat.getColor(this, R.color.text_hint))

            llCountrySelector.visibility = View.VISIBLE
            tilPhoneNumber.visibility = View.VISIBLE
            tilEmail.visibility = View.GONE

            // Привязываем пароль к низу селектора страны (ll_country_selector)
            val params = tilPassword.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = R.id.ll_country_selector
            params.topMargin = resources.getDimensionPixelSize(R.dimen.spacing_medium) // тот же отступ, что и в разметке
            tilPassword.layoutParams = params

        } else {
            // --- Email режим ---
            cvEmailSelector.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected))
            cvPhoneSelector.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
            tvEmailLabel.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            tvPhoneLabel.setTextColor(ContextCompat.getColor(this, R.color.text_hint))

            llCountrySelector.visibility = View.GONE
            tilPhoneNumber.visibility = View.GONE
            tilEmail.visibility = View.VISIBLE

            // Привязываем пароль к низу поля email
            val params = tilPassword.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = R.id.til_email
            params.topMargin = resources.getDimensionPixelSize(R.dimen.spacing_medium)
            tilPassword.layoutParams = params
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isPhoneMode", isPhoneMode)
    }


    private fun showCountryDialog() {
        val adapter = SimpleCountryAdapter(countries)
        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите страну")
            .setAdapter(adapter) { dialog, which ->
                selectedCountry = countries[which]
                updateCountryDisplay()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateCountryDisplay() {
        selectedCountry?.let {
            tvCountryFlag.text = it.flag
            tvCountryCode.text = it.code
        }
    }

    private fun validateInput(): Boolean {
        tilPassword.error = null
        tilEmail.error = null
        tilPhoneNumber.error = null
        val isMainValid = if (isPhoneMode) validatePhone() else validateEmail()
        val isPasswordValid = validatePassword()
        return isMainValid && isPasswordValid
    }

    private fun validatePhone(): Boolean {
        val phone = etPhoneNumber.text.toString().trim()
        return when {
            phone.isEmpty() -> {
                tilPhoneNumber.error = "Введите номер телефона"
                false
            }
            phone.length < 10 -> {
                tilPhoneNumber.error = "Минимум 10 цифр"
                false
            }
            else -> {
                tilPhoneNumber.error = null
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            tilEmail.error = "Введите почту"
            return false
        }
        if (!isValidEmail(email)) {
            tilEmail.error = "Неверная почта"
            return false
        }
        tilEmail.error = null
        return true

    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun performLogin(countryCode: String, phone: String, password: String): Boolean {
        val fullPhoneNumber = "$countryCode$phone"
        println("Login attempt: $fullPhoneNumber")
        return phone.trim() == "1234567890" && password.trim() == "qwert123"
    }

    private fun performEmailLogin(email: String, password: String): Boolean {
        println("Email login: $email")
        return email.trim() == "raz@gmail.com" && password.trim() == "qwert123"
    }

    private fun performSocialLogin(provider: String) {
        println("Social login: $provider")
    }

    data class Country(val flag: String, val name: String, val code: String) {
        override fun toString(): String = "$flag $code"
    }

    inner class SimpleCountryAdapter(private val countries: List<Country>) :
        ArrayAdapter<Country>(this@MainActivity, android.R.layout.simple_list_item_1, countries) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)

            val country = getItem(position)!!
            (view as TextView).text = country.toString()
            return view
        }
    }
}
