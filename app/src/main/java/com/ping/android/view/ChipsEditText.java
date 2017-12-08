package com.ping.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.ultility.Callback;
import com.ping.android.utils.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tuanluong on 12/7/17.
 */

public class ChipsEditText extends android.support.v7.widget.AppCompatEditText {
    private static final int DELAY = 300;

    private ChipsListener listener;
    private Timer timer;
    private int delayTime = DELAY; // milliseconds
    private TextWatcher textWatcher;

    public ChipsEditText(Context context) {
        super(context);
        init();
    }

    public ChipsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private String textDeleted = "";
    private ArrayList<ImageSpan> spansToBeRemove = new ArrayList<>();

    private void init() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i1 > 0) {
                    int end = i + i1;
                    Editable editable = getEditableText();
                    ImageSpan[] spans = editable.getSpans(i, end, ImageSpan.class);
                    for (ImageSpan span : spans) {
                        int spanStart = editable.getSpanStart(span);
                        int spanEnd = editable.getSpanEnd(span);
                        if (spanStart < end && spanEnd > i) {
                            spansToBeRemove.add(span);
                        }
                    }
                    // Text may deleted
                    textDeleted = charSequence.subSequence(i, i + i1).toString();
                } else {
                    textDeleted = "";
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 >= 1) {
                    if (charSequence.charAt(i) == ',') {
                        removeTextChangedListener(textWatcher);
                        setChips(); // generate chips
                        addTextChangedListener(textWatcher);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (timer != null) {
                    timer.cancel();
                }
                if (spansToBeRemove.size() > 0) {
                    for (ImageSpan span : spansToBeRemove) {
                        if (listener != null) {
                            listener.onDeleteChip(span.getSource());
                        }
                    }
                    spansToBeRemove.clear();
                    return;
                }

                if (",".equals(textDeleted)) return;

                timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        // Trigger task after delaying
                        if (listener != null) {
                            String[] text = getText().toString().split(",");
                            if (text.length > 0) {
                                listener.onSearchText(text[text.length - 1]);
                            }
                        }
                    }
                };
                timer.schedule(task, delayTime);
            }
        };
        addTextChangedListener(textWatcher);
    }

    public void setListener(ChipsListener callback, int debounceDelay) {
        this.listener = callback;
        this.delayTime = debounceDelay;
    }

    public void appendNewChip(String text) {
        String currentText = getText().toString();
        StringBuilder finalText = new StringBuilder();
        boolean shouldRemoveLast = !currentText.endsWith(",");
        if (currentText.contains(",")) {
            String[] array = currentText.split(",");
            int size = array.length - (shouldRemoveLast ? 1 : 0);
            for (int i = 0; i < size; i++) {
                String contact = array[i];
                finalText.append(contact + ",");
            }
        }
        finalText.append(text + ",");
        removeTextChangedListener(textWatcher);
        setText(finalText.toString());
        setChips();
        addTextChangedListener(textWatcher);
    }

    public void updateText(String s) {
        removeTextChangedListener(textWatcher);
        setText(s);
        setChips();
        addTextChangedListener(textWatcher);
    }

    public void setChips() {
        if (getText().toString().contains(",")) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
            // split string with comma
            String chips[] = getText().toString().trim().split(",");
            int x = 0;
            // loop will generate ImageSpan for every country name separated by comma
            for (String c : chips) {
                // inflate chips_edittext layout
                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                TextView textView = (TextView) lf.inflate(R.layout.chips_edittext, null);
                textView.setText(c); // set text
                // capture bitmap of generated textview
                int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                textView.measure(spec, spec);
                textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
                Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                canvas.translate(-textView.getScrollX(), -textView.getScrollY());
                textView.draw(canvas);
                textView.setDrawingCacheEnabled(true);
                Bitmap cacheBmp = textView.getDrawingCache();
                Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
                textView.destroyDrawingCache();  // destory drawable
                // create bitmap drawable for imagespan
                BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
                bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
                // create and set imagespan
                ImageSpan imageSpan = new ImageSpan(bmpDrawable, c);
                ssb.setSpan(imageSpan, x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                x = x + c.length() + 1;
            }
            // set chips span
            setText(ssb);
            // move cursor to last
            setSelection(getText().length());
        }
    }

    public interface ChipsListener {
        void onSearchText(String text);
        void onDeleteChip(String text);
    }
}