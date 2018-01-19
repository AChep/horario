package com.artemchep.horario.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.ui.fragments.FragmentDocument

/**
 * @author Artem Chepurnoy
 */
class SimpleNumberPicker : LinearLayout {

    private lateinit var plusView: View
    private lateinit var minusView: View
    private lateinit var numberTextView: TextView

    var minValue: Int = 1
        set(v) {
            field = v

            if (value.value < v) {
                value.value = v
            }
        }
    var maxValue: Int = 10
        set(v) {
            field = v

            if (value.value > v) {
                value.value = v
            }
        }
    var value = FragmentDocument.Observable(1)
        private set

    private val listener: OnClickListener = OnClickListener { v ->
        when {
            v === plusView -> if (value.value < maxValue) value.value++
            v === minusView -> if (value.value > minValue) value.value--
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()

        plusView = findViewById<View>(R.id.np_plus).withOnClick(listener)
        minusView = findViewById<View>(R.id.np_minus).withOnClick(listener)
        numberTextView = findViewById(R.id.np_value)

        value.follow { value ->
            numberTextView.text = value.toString()
            plusView.isEnabled = value < maxValue
            minusView.isEnabled = value > minValue
        }
    }

}