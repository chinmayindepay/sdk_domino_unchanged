/*
 * ******************************************************************************
 *  * Copyright, INDEPAY 2019 All rights reserved.
 *  *
 *  * The copyright in this work is vested in INDEPAY and the
 *  * information contained herein is confidential.  This
 *  * work (either in whole or in part) must not be modified,
 *  * reproduced, disclosed or disseminated to others or used
 *  * for purposes other than that for which it is supplied,
 *  * without the prior written permission of INDEPAY.  If this
 *  * work or any part hereof is furnished to a third party by
 *  * virtue of a contract with that party, use of this work by
 *  * such party shall be governed by the express contractual
 *  * terms between the INDEPAY which is a party to that contract
 *  * and the said party.
 *  *
 *  * Revision History
 *  * Date           Who        Description
 *  * 06-09-2019     Mayank D   Added file header
 *  *
 *  *****************************************************************************
 */

package com.indepay.umps.spl.pinpad

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import com.indepay.umps.spl.R

class PinEntryEditText : android.support.v7.widget.AppCompatEditText, PinpadView.ViewProvider {

    private var mSpace = 24f //24 dp by default, space between the lines
    private var mCharSize: Float = 0.toFloat()
    private var mNumChars = 4f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines
    private var mMaxLength = 4

    private var mClickListener: View.OnClickListener? = null

    private var mLineStroke = 2f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null
    private var mStates = arrayOf(
            intArrayOf(android.R.attr.state_selected), // selected
            intArrayOf(android.R.attr.state_focused), // focused
            intArrayOf(-android.R.attr.state_focused))// unfocused

    private var mColors = intArrayOf(Color.GREEN, Color.BLACK, Color.GRAY)
    private var mColorStates = ColorStateList(mStates, mColors)
    internal var isTextHidden = true

    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }

    init {
        gravity = Gravity.CENTER
        textSize = 40F
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context) {
        init(context, attrs)
    }//super(context, attrs, defStyleAttr, defStyleRes);

    private fun init(context: Context, attrs: AttributeSet) {
        val multi = context.resources.displayMetrics.density
        mLineStroke *= multi
        mLineStrokeSelected *= multi
        mLinesPaint = Paint(paint)
        mLinesPaint?.strokeWidth = mLineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorControlActivated,
                    outValue, true)
            val colorActivated = outValue.data
            mColors[0] = colorActivated

            context.theme.resolveAttribute(R.attr.colorPrimaryDark,
                    outValue, true)
            val colorDark = outValue.data
            mColors[1] = colorDark

            context.theme.resolveAttribute(R.attr.colorControlHighlight,
                    outValue, true)
            val colorHighlight = outValue.data
            mColors[2] = colorHighlight
        }
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density
        mLineSpacing *= multi //convert to pixels for our density
        mMaxLength = attrs.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
        mNumChars = mMaxLength.toFloat()

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text.length)
            mClickListener?.onClick(v)
        }

    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom

        //Text Width
        val text = text
        val textLength = text.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)

        var i = 0
        while (i < mNumChars) {
            updateColorForLines(i == textLength)
            canvas.drawLine(startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint!!)

            if (getText().length > i) {
                val middle = startX + mCharSize / 2
                if(isTextHidden){
                    canvas.drawText("*", middle - textWidths[0] / 2, bottom - mLineSpacing, paint)
                }else{
                    canvas.drawText(text, i, i + 1, middle - textWidths[0] / 2, bottom - mLineSpacing, paint)
                }
            }

            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace).toInt()
            }
            i++
        }
    }


    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(states, Color.GRAY)
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean) {
        if (isActivated) {
            mLinesPaint?.strokeWidth = mLineStrokeSelected
            mLinesPaint?.color = getColorForState(android.R.attr.state_focused)
            if (next) {
                mLinesPaint?.color = getColorForState(android.R.attr.state_selected)
            }
        } else {
            mLinesPaint?.strokeWidth = mLineStroke
            mLinesPaint?.color = getColorForState(-android.R.attr.state_focused)
        }
    }


    override fun onAppendChar(char: Char) {
        setText(text.toString() + char, TextView.BufferType.EDITABLE)
    }

    override fun onDeleteChar() {
        setText(text.substring(0, text.lastIndex), TextView.BufferType.EDITABLE)
    }

    override fun onReset() {
        setText("", TextView.BufferType.EDITABLE)
    }

    override fun onToggleTextVisibility() {

    }

}