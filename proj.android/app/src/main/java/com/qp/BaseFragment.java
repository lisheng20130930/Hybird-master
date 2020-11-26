package com.qp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.ikidou.fragmentBackHandler.FragmentBackHandler;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qp.utils.ToastUtils;


/**
 * @author Listen.Li
 */
public class BaseFragment extends Fragment implements FragmentBackHandler {

    private KProgressHUD show;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        show = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("加载中....")
                .setCancellable(true);
    }

    public void showWaitDialog() {
        show.show();
    }

    public void closeWaitDialog() {
        try {
            show.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMsg(String msg) {
        ToastUtils.show(msg);
    }

    public void ToOtherActivity(Class clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
