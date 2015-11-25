package at.dingbat.type.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.editor.TextStyle;

/**
 * Created by Max on 11/24/2015.
 */
public class TextBlockItem extends RelativeLayout implements Editable {

    private EditText edit;
    private TextView text;

    private boolean editable = false;

    public TextBlockItem(Context context) {
        super(context);
        inflate(context, R.layout.widget_text_block_item, this);

        edit = (EditText) findViewById(R.id.widget_text_block_item_edit);
        text = (TextView) findViewById(R.id.widget_text_block_item_text);

        edit.setVisibility(GONE);
    }

    public void setEditable(boolean editable) {
        if(this.editable && !editable) {
            this.edit.animate().setDuration(100).setInterpolator(new AccelerateInterpolator()).alpha(0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    edit.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            this.text.animate().setDuration(100).setInterpolator(new DecelerateInterpolator()).alpha(1f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    text.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else if(!this.editable && editable) {
            this.edit.animate().setDuration(100).setInterpolator(new DecelerateInterpolator()).alpha(1f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    edit.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            this.text.animate().setDuration(100).setInterpolator(new AccelerateInterpolator()).alpha(0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    edit.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
    }

    @Override
    public void onEnterEditMode() {

    }

    @Override
    public void onExitEditMode() {

    }

    public void setDataHolder(DataHolder holder) {
        edit.setText(holder.content);
        text.setText(holder.content);

        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, holder.style.size);
        text.setTextColor(Color.parseColor(holder.style.color));

        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, holder.style.size);
        edit.setTextColor(Color.parseColor(holder.style.color));
    }

    public static class DataHolder extends Adapter.DataHolder {
        public String uuid;
        public String content;
        public TextStyle style;
        public static DataHolder create(String uuid, String content, TextStyle style) {
            DataHolder holder = new DataHolder();
            holder.uuid = uuid;
            holder.content = content;
            holder.style = style;
            return holder;
        }
    }

}
