package com.ping.android.presentation.view.custom

import android.content.Context
import android.support.constraint.ConstraintLayout
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
            val leftIconRes = typeArray.getDrawable(R.styleable.SettingItem_leftIcon)
            if (leftIconRes != null) {
                leftIcon.setImageDrawable(leftIconRes)
            }
            val title = typeArray.getString(R.styleable.SettingItem_title)
            tv_title.text = title
            val color = typeArray.getColor(R.styleable.SettingItem_titleColor, resources.getColor(R.color.black))
            tv_title.setTextColor(color)
            divider.visibility = if (showDivider) View.VISIBLE else View.GONE
            typeArray.recycle()
        }
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