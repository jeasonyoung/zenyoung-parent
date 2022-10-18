package top.zenyoung.orm.injector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 物理删除
 *
 * @author young
 */
@SuppressWarnings({"all"})
public class PhysicalDeleteMethod extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(final Class<?> mapperClass, final Class<?> modelClass, final TableInfo tableInfo) {
        final SqlMethod sqlMethod = SqlMethod.DELETE_BATCH_BY_IDS;
        final String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(),
                SqlScriptUtils.convertForeach(
                        SqlScriptUtils.convertChoose("@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(item.getClass())",
                                "#{item}", "#{item." + tableInfo.getKeyProperty() + "}"),
                        COLL, null, "item", COMMA));
        final SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);
        return this.addDeleteMappedStatement(mapperClass, "physicalDelete", sqlSource);
    }
}
