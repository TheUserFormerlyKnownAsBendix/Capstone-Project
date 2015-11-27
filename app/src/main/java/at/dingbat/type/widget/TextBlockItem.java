package at.dingbat.type.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.dingbat.type.R;
import at.dingbat.type.adapter.Adapter;
import at.dingbat.type.model.TextStyle;

/**
 * Created by Max on 11/24/2015.
 */
public class TextBlockItem extends RelativeLayout implements Editable {

    private RelativeLayout controls_container;
    private RelativeLayout text_container;
    private CardView controls;

    private EditText edit;
    private TextView text;

    private boolean editable = false;

    private LocalBroadcastManager lbcm;

    public TextBlockItem(Context context) {
        super(context);
        inflate(context, R.layout.widget_text_block_item, this);

        controls_container = (RelativeLayout) findViewById(R.id.widget_text_block_item_control_container);
        text_container = (RelativeLayout) findViewById(R.id.widget_text_block_item_text_container);
        controls = (CardView) findViewById(R.id.widget_text_block_item_control);

        edit = (EditText) findViewById(R.id.widget_text_block_item_edit);
        text = (TextView) findViewById(R.id.widget_text_block_item_text);

        edit.setVisibility(GONE);

        lbcm = LocalBroadcastManager.getInstance(context);
        lbcm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                if(action.equals("entereditmode")) {
                    setEditable(true);
                } else if(action.equals("exiteditmode")) {
                    setEditable(false);
                }
            }
        }, new IntentFilter("at.dingbat.type"));
    }

    public void setEditable(boolean editable) {
        if(this.editable && !editable) {
            text.setText(edit.getText());
            edit.setVisibility(GONE);
            text.setVisibility(VISIBLE);

            ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
            animator.setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) controls_container.getLayoutParams();
                    params.width = (int) ((float) animation.getAnimatedValue() * getResources().getDimension(R.dimen.list_item_primary_control_width));
                    params.height = (int) ((float) animation.getAnimatedValue() * getResources().getDimension(R.dimen.list_item_primary_control_width));
                    controls_container.setLayoutParams(params);
                    text_container.setMinimumHeight((int) getResources().getDimension(R.dimen.list_item_primary_control_width));
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    controls.animate().setDuration(150).setInterpolator(new AccelerateInterpolator()).scaleX(0).scaleY(0).alpha(0);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();

        } else if(!this.editable && editable) {
            edit.setVisibility(VISIBLE);
            text.setVisibility(GONE);

            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) controls_container.getLayoutParams();
                    params.width = (int) ((float) animation.getAnimatedValue() * getResources().getDimension(R.dimen.list_item_primary_control_width));
                    controls_container.setLayoutParams(params);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    controls.animate().setDuration(150).setInterpolator(new DecelerateInterpolator()).scaleX(1).scaleY(1).alpha(1);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();

        }
        this.editable = editable;
    }

    @Override
    public void onEnterEditMode() {
        setEditable(true);
    }

    @Override
    public void onExitEditMode() {
        setEditable(false);
    }

    public void setDataHolder(DataHolder holder) {
        edit.setText(holder.content);
        text.setText(holder.content);

        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, holder.style.size);
        text.setTextColor(Color.parseColor(holder.style.color));
        text.setPadding((int) (getResources().getDimension(R.dimen.margin) * holder.style.indentation), 0, 0, 0);

        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, holder.style.size);
        edit.setTextColor(Color.parseColor(holder.style.color));
        edit.setPadding((int) (getResources().getDimension(R.dimen.margin) * holder.style.indentation), 0, 0, 0);
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
