package com.example.axiomvpn

import android.R.attr.repeatCount
import android.R.attr.repeatMode
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat

class DashboardActivity : ComponentActivity() {

    // Добавь это прямо внутри класса DashboardActivity, перед onCreate

    data class MenuItem(
        val title: String,
        val subtitle: String? = null,
        val iconRes: Int? = null
    )

    private val menuItems = listOf(
        MenuItem("Геолокация VPN", "Выбор сервера", android.R.drawable.ic_menu_myplaces),
        MenuItem("Язык приложения", "Русский", android.R.drawable.ic_menu_sort_by_size),
        MenuItem("Версия Axiom VPN", "1.0.0", android.R.drawable.ic_menu_info_details),
        MenuItem("Прозрачность Glass", null),
        MenuItem("Наш Telegram", "@axiomvpn", android.R.drawable.ic_menu_send),
        MenuItem("Наш E-mail", "support@axiom.com"),
        MenuItem("Telegram бот", "@AxiomBot"),
        MenuItem("История покупок"),
        MenuItem("О нас"),
        MenuItem("Юридическая информация"),
        MenuItem("Обработка данных"),
        MenuItem("Premium", "Активировать подписку", android.R.drawable.btn_star_big_on),
        MenuItem("Реферальная система"),
        MenuItem("Подарки")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // пример — в onCreate после setContentView
        findViewById<View>(R.id.btnMenu).setOnClickListener { view ->
            showMenuPopup(view)
        }


        // Находим View свечения
        val glowView = findViewById<View>(R.id.glow_view)

        // Анимация увеличения/уменьшения и прозрачности
        val scaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 1f, 1.2f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 1f, 1.2f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val alpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.6f, 0.5f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, alpha)
        animatorSet.start()

        // Поля класса (например, в MainActivity)
        var isEnabled = false

// В методе onCreate или подобном
        val anim1fon = findViewById<View>(R.id.glow_view)
        val anim1elem1 = findViewById<View>(R.id.greenBorderCircle)
        val anim1elem2 = findViewById<ImageView>(R.id.shit_knop)

        anim1fon.setOnClickListener {
            if (isEnabled) {
                // Состояние "выключено"
                anim1elem1.setBackgroundResource(R.drawable.green_border_circle)  // Возвращаем зелёную обводку
                anim1elem2.setImageResource(R.drawable.shit_knop)                // Обычный щит
            } else {
                // Состояние "включено"
                anim1elem1.setBackgroundResource(R.drawable.grai_border_circle)  // Серая обводка
                anim1elem2.setImageResource(R.drawable.shit_knop_off)            // Щит выключен
            }
            isEnabled = !isEnabled  // Переключаем флаг
        }



    }

    private fun showMenuPopup(anchor: View) {
        val adapter = object : ArrayAdapter<MenuItem>(this, 0, menuItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_menu_popup, parent, false)

                val item = getItem(position)!!

                val icon = view.findViewById<ImageView>(R.id.icon)
                val title = view.findViewById<TextView>(R.id.title)
                val subtitle = view.findViewById<TextView>(R.id.subtitle)

                title.text = item.title

                if (item.iconRes != null) {
                    icon.setImageResource(item.iconRes)
                    icon.visibility = View.VISIBLE
                } else {
                    icon.visibility = View.GONE
                }

                if (!item.subtitle.isNullOrBlank()) {
                    subtitle.text = item.subtitle
                    subtitle.visibility = View.VISIBLE
                } else {
                    subtitle.visibility = View.GONE
                }

                return view
            }
        }

        ListPopupWindow(this).apply {
            setAnchorView(anchor)
            setAdapter(adapter)
            width = 900   // ← временно хардкод, потом сделаешь через dimens
            isModal = true

            // Исправляем background — используем this вместо context
            setBackgroundDrawable(ContextCompat.getDrawable(this@DashboardActivity, R.drawable.popup_background))

            setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> { /* Геолокация */ }
                    5 -> { /* email — пока просто Toast */ }
                    6 -> { /* telegram — пока просто Toast */ }
                    // добавляй свои действия
                    else -> { }
                }
                dismiss()
            }

            show()
        }
    }
}
