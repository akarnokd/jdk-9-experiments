package hu.akarnokd.java9.rx;

import hu.akarnokd.rxjava2.interop.FlowInterop;
import hu.akarnokd.rxjava2.interop.FlowTestSubscriber;
import io.reactivex.Flowable;

/**
 * Created by akarnokd on 2016.12.05..
 */
public class SeeRx {
    public static void main(String[] args) {
        FlowTestSubscriber<Integer> ts = new FlowTestSubscriber<>();

        Flowable.just(1).to(FlowInterop.toFlow())
                .subscribe(ts);

        ts.assertResult(1);
    }
}
