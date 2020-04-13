package top.zenyoung.controller.listener;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 查询监听器
 *
 * @author yangyong
 * @version 1.0
 *  2020/2/7 2:05 下午
 **/
public interface QueryListener<Item extends Serializable, Ret extends Serializable> extends Function<Item, Ret>, PreHandlerListener<Void>, ExceptHandlerListener {

    /**
     * 查询数据
     *
     * @return 数据集合
     */
    List<Item> query();
}
