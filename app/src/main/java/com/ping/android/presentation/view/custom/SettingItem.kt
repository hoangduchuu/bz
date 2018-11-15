package com.ping.android.presentation.view.custom

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import kotlinx.android.synthetic.main.view_setting_item.view.*

class SettingItem : ConstraintLayout {
    private var showDivider = true

    constructor(context: Context) : super(context) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView(attrs)
    }

    private fun initView(attrs: AttributeSet? = null) {
        inflate(R.layout.view_setting_item, true)
        attrs.let {
            val typeArray = context.obtainStyledAttributes(it, R.styleable.SettingItem, 0, 0)
            showDivider = typeArray.getBoolean(R.styleable.SettingItem_showDivider, showDivider)
            if (typeArray.hasValue(R.styleable.SettingItem_leftIcon)) {
                val leftIconRes = typeArray.getDrawable(R.styleable.SettingItem_leftIcon)
                if (leftIconRes != null) {
                    leftIcon.setImageDrawable(leftIconRes)
                }
            } else {
               leftIcon.visibility = View.GONE
            }
            if (typeArray.hasValue(R.styleable.SettingItem_leftIconTint)) {
                val leftIconTintColor = typeArray.getColor(R.styleable.SettingItem_leftIconTint, ContextCompat.getColor(context, R.color.orange))
                leftIcon.setColorFilter(leftIconTintColor, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            val isUseHTML = typeArray.getBoolean(R.styleable.SettingItem_useHtml,false)
            if (typeArray.hasValue(R.styleable.SettingItem_useHtml) || isUseHTML){
                val title = typeArray.getString(R.styleable.SettingItem_titleText)
                tv_title.setText(Html.fromHtml(title));
                tv_title.setMovementMethod(LinkMovementMethod.getInstance());

            }else{
                val title = typeArray.getString(R.styleable.SettingItem_titleText)
                tv_title.text = title
            }

            val color = typeArray.getColor(R.styleable.SettingItem_titleColor, ContextCompat.getColor(context, R.color.black))
            tv_title.setTextColor(color)
            divider.visibility = if (showDivider) View.VISIBLE else View.GONE
            typeArray.recycle()
        }
    }

    fun setTitle(title: String) {
        tv_title.text = title
    }

    fun setTitleColor(colorInt: Int){
        tv_title.setTextColor(colorInt)
    }

    fun setDividerVisibility(visible: Boolean) {
        divider.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child != null && params != null) {
            if (child.id == R.id.leftIcon || child.id == R.id.tv_title
                    || child.id == R.id.divider || child.id == R.id.viewContainer) {
                super.addView(child, index, params)
            } else {
                viewContainer.addView(child, params)
            }
        } else {
            super.addView(child, index, params)
        }
    }
}