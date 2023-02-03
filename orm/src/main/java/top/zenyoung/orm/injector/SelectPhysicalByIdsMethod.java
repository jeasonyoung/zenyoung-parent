package top.zenyoung.orm.injector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 根据ID集合查询数据(包括被逻辑删除的)
 *
 * @author young
 */
public class SelectPhysicalByIdsMethod extends AbstractMethod {

    public SelectPhysicalByIdsMethod(){
        super("selectPhysicalByIds");
    }

    @Override
    public MappedStatement injectMappedStatement(final Class<?> mapperClass, final Class<?> modelClass, final TableInfo tableInfo) {
        //sql模板
        final String sql = SqlMethod.SELECT_BATCH_BY_IDS.getSql();
        //查询字段
        final String selCols = sqlSelectColumns(tableInfo, false);
        //查询条件
        final String whereIn = SqlScriptUtils.convertForeach("#{item}", COLLECTION, null, "item", COMMA);
        //
        final SqlSource sqlSource = languageDriver.createSqlSource(configuration,
                String.format(sql, selCols, tableInfo.getTableName(), tableInfo.getKeyColumn(), whereIn, ""),
                Object.class);
        //
        return addSelectMappedStatementForTable(mapperClass, sqlSource, tableInfo);
    }
}
