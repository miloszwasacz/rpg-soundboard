package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder

@BindingAdapter("srcCompat")
fun setImageFromResource(view: AppCompatImageView, @DrawableRes id: Int) {
    view.setImageResource(id)
}

@BindingAdapter(value = ["android:text", "fallbackText"], requireAll = false)
fun setTextFromPlaceholder(view: AppCompatTextView, placeholder: Placeholder?, fallbackText: String?) {
    placeholder?.run {
        text?.also {
            view.setText(it)
        } ?: fallbackText?.also {
            view.text = it
        }
    } ?: view.apply { text = "" }
}

@BindingAdapter("android:visibility")
fun setVisibilityOnCondition(view: View, condition: Boolean) {
    view.visibility = if (condition) View.VISIBLE else View.GONE
}
