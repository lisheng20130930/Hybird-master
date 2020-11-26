package com.qp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Listen.Li
 */
public abstract class EsActivity extends BaseActivity implements BaseView {
    protected Activity mActivity;
    private Unbinder mUnBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(getLayoutId() == 0)) {
            setContentView(getLayoutId());
        }
        mUnBinder = ButterKnife.bind(this);
        mActivity = this;
        initEventAndData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }

    protected abstract int getLayoutId();
    protected abstract void initEventAndData();
}
