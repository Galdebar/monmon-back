package lt.galdebar.monmonmvc.service.adapters;

import java.util.List;

public interface IsObjectAdapter<A,B> {
    A bToA(B b);
    List<A> bToA(List<B> bList);
    B aToB(A a);
    List<B> aToB(List<A> aList);
}
